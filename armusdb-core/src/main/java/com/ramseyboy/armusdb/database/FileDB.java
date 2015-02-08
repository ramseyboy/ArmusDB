package com.ramseyboy.armusdb.database;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileDB implements Database {

    /**
     * HashMap which holds the in-memory index. For efficiency, the entire index
     * is cached in memory. The map holds a key of type String to a RecordHeader.
     */
    protected ConcurrentHashMap<String, RecordHeader> memIndex;

    //The file location
    private final File path;
    // The database file.
    private RandomAccessFile file;
    // Current file pointer to the start of the record data.
    protected long dataStartPtr;
    // Total length in bytes of the global database headers.
    protected static final int FILE_HEADERS_REGION_LENGTH = 16;
    // Number of bytes in the record header.
    protected static final int RECORD_HEADER_LENGTH = 16;
    // The length of a key in the index.
    protected static final int MAX_KEY_LENGTH = 64;
    // The total length of one index entry - the key length plus the record header length.
    protected static final int INDEX_ENTRY_LENGTH = MAX_KEY_LENGTH + RECORD_HEADER_LENGTH;
    // File pointer to the num records header.
    protected static final long NUM_RECORDS_HEADER_LOCATION = 0;
    // File pointer to the data start pointer header.
    protected static final long DATA_START_HEADER_LOCATION = 4;

    //default index capacity
    private static final int DEFAULT_INDEX_CAPACITY = 64;

    private static final String READ_WRITE = "rw";
    private static final String READ = "r";

    public FileDB(File dbPath) {
        path = dbPath;
    }

    @Override
    public synchronized void open() throws IOException {
        if (null == path) {
            throw new RuntimeException("Path to database cannot be null");
        }

        if (path.exists()) {
            file = new RandomAccessFile(path, READ_WRITE);
            dataStartPtr = readDataStartHeader();

            int numRecords = readNumRecordsHeader();
            memIndex = new ConcurrentHashMap<>(numRecords);
            for (int i = 0; i < numRecords; i++) {
                String key = readKeyFromIndex(i);
                RecordHeader header = readRecordHeaderFromIndex(i);
                header.setIndexPosition(i);
                memIndex.put(key, header);
            }
        } else {
            file = new RandomAccessFile(path, READ_WRITE);
            dataStartPtr = indexPositionToKeyFp(DEFAULT_INDEX_CAPACITY);
            setFileLength(dataStartPtr);
            writeNumRecordsHeader(0);
            writeDataStartPtrHeader(dataStartPtr);

            memIndex = new ConcurrentHashMap<>(DEFAULT_INDEX_CAPACITY);
        }
    }

    private ConcurrentHashMap<String, RecordHeader> getMemIndex() {
        checkIndex();
        return memIndex;
    }

    private void checkIndex() {
        if (memIndex == null) {
            throw new NullPointerException("Please call open() before making any operations");
        }
    }

    /**
     * Closes the file.
     */
    @Override
    public synchronized void close() throws IOException {
        try {
            file.close();
        } finally {
            file = null;
            getMemIndex().clear();
            memIndex = null;
        }
    }

    /**
     * Returns the current number of records in the database.
     */
    public synchronized int getNumRecords() {
        return memIndex.size();
    }

    /**
     * Checks if there is a record belonging to the given key.
     */
    public synchronized boolean recordExists(String key) {
        return getMemIndex().containsKey(key);
    }

    /**
     * Maps a key to a record header by looking it up in the in-memory index.
     */
    protected RecordHeader keyToRecordHeader(String key) throws IOException {
        RecordHeader h = getMemIndex().get(key);
        if (h==null) {
            throw new IOException("Key not found: " + key);
        }
        return h;
    }

    /**
     * Returns the record to which the target file pointer belongs - meaning the specified location
     * in the file is part of the record data of the RecordHeader which is returned.  Returns null if
     * the location is not part of a record. (O(n) mem accesses)
     */
    protected RecordHeader getRecordAt(long targetFp) throws IOException {
        for (RecordHeader next : getMemIndex().values()) {
            if (targetFp >= next.dataPointer &&
                    targetFp < next.dataPointer + (long)next.dataCapacity) {
                return next;
            }
        }
        return null;
    }


    protected long getFileLength() throws IOException {
        return file.length();
    }


    protected void setFileLength(long l) throws IOException {
        file.setLength(l);
    }

    /**
     * Reads the number of records header from the file.
     */
    protected int readNumRecordsHeader() throws IOException {
        file.seek(NUM_RECORDS_HEADER_LOCATION);
        return file.readInt();
    }


    /**
     * Writes the number of records header to the file.
     */
    protected void writeNumRecordsHeader(int numRecords) throws IOException {
        file.seek(NUM_RECORDS_HEADER_LOCATION);
        file.writeInt(numRecords);
    }


    /**
     * Reads the data start pointer header from the file.
     */
    protected long readDataStartHeader() throws IOException {
        file.seek(DATA_START_HEADER_LOCATION);
        return file.readLong();
    }


    /**
     * Writes the data start pointer header to the file.
     */
    protected void writeDataStartPtrHeader(long dataStartPtr) throws IOException {
        file.seek(DATA_START_HEADER_LOCATION);
        file.writeLong(dataStartPtr);
    }


    /**
     * Returns a file pointer in the index pointing to the first byte
     * in the key located at the given index position.
     */
    protected long indexPositionToKeyFp(int pos) {
        return FILE_HEADERS_REGION_LENGTH + (INDEX_ENTRY_LENGTH * pos);
    }


    /**
     * Returns a file pointer in the index pointing to the first byte
     * in the record pointer located at the given index position.
     */
    long indexPositionToRecordHeaderFp(int pos) {
        return indexPositionToKeyFp(pos) + MAX_KEY_LENGTH;
    }


    /**
     * Reads the ith key from the index.
     */
    String readKeyFromIndex(int position) throws IOException {
        file.seek(indexPositionToKeyFp(position));
        return file.readUTF();
    }


    /**
     * Reads the ith record header from the index.
     */
    RecordHeader readRecordHeaderFromIndex(int position) throws IOException {
        file.seek(indexPositionToRecordHeaderFp(position));
        return RecordHeader.readHeader(file);
    }


    /**
     * Writes the ith record header to the index.
     */
    protected void writeRecordHeaderToIndex(RecordHeader header) throws IOException {
        file.seek(indexPositionToRecordHeaderFp(header.indexPosition));
        header.write(file);
    }


    @Override
    public synchronized void delete(String key) throws IOException {
        checkIndex();
        RecordHeader delRec = keyToRecordHeader(key);
        int currentNumRecords = getNumRecords();
        if (getFileLength() == delRec.dataPointer + delRec.dataCapacity) {
            // shrink file since this is the last record in the file
            setFileLength(delRec.dataPointer);
        } else {
            RecordHeader previous = getRecordAt(delRec.dataPointer -1);
            if (previous != null) {
                // append space of deleted record onto previous record
                previous.dataCapacity += delRec.dataCapacity;
                writeRecordHeaderToIndex(previous);
            } else {
                // target record is first in the file and is deleted by adding its space to
                // the second record.
                RecordHeader secondRecord = getRecordAt(delRec.dataPointer + (long)delRec.dataCapacity);
                byte[] data = readRecordData(secondRecord);
                secondRecord.dataPointer = delRec.dataPointer;
                secondRecord.dataCapacity += delRec.dataCapacity;
                writeRecordData(secondRecord, data);
                writeRecordHeaderToIndex(secondRecord);
            }
        }
        deleteEntryFromIndex(key, delRec, currentNumRecords);
    }

    /**
     * Removes the record from the index. Replaces the target with the entry at the
     * end of the index.
     */
    protected void deleteEntryFromIndex(String key, RecordHeader header, int currentNumRecords) throws IOException {
        if (header.indexPosition != currentNumRecords -1) {
            String lastKey = readKeyFromIndex(currentNumRecords-1);
            RecordHeader last  = keyToRecordHeader(lastKey);
            last.setIndexPosition(header.indexPosition);
            file.seek(indexPositionToKeyFp(last.indexPosition));
            file.writeUTF(lastKey);
            file.seek(indexPositionToRecordHeaderFp(last.indexPosition));
            last.write(file);
        }
        writeNumRecordsHeader(currentNumRecords-1);

        getMemIndex().remove(key);
    }

    @Override
    public synchronized byte[] get(String key) throws IOException {
        checkIndex();
        byte[] data = readRecordData(key);
        RecordReader reader = new RecordReader(key, data);
        try {
            return (byte[]) reader.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to convert object to byte[]");
        }
    }

    /**
     * Reads the data for the record with the given key.
     */
    protected byte[] readRecordData(String key) throws IOException {
        return readRecordData(keyToRecordHeader(key));
    }


    /**
     * Reads the record data for the given record header.
     */
    protected byte[] readRecordData(RecordHeader header) throws IOException {
        byte[] buf = new byte[header.dataCount];
        file.seek(header.dataPointer);
        file.readFully(buf);
        return buf;
    }

    @Override
    public synchronized void put(String key, byte[] value) throws IOException {
        checkIndex();
        RecordWriter rw = new RecordWriter(key);
        rw.writeObject(value);
        if (recordExists(key)) {
            RecordHeader header = keyToRecordHeader(rw.getKey());
            if (rw.getDataLength() > header.dataCapacity) {
                delete(rw.getKey());
                put(rw.getKey(), value);
            } else {
                writeRecordData(header, rw);
                writeRecordHeaderToIndex(header);
            }
        } else {
            insureIndexSpace(getNumRecords() + 1);
            RecordHeader newRecord = allocateRecord(key, rw.getDataLength());
            writeRecordData(newRecord, rw);
            addEntryToIndex(key, newRecord, getNumRecords());
        }
    }

    /**
     * This method searches the file for free space and then returns a RecordHeader
     * which uses the space. (O(n) memory accesses)
     */
    protected RecordHeader allocateRecord(String key, int dataLength) throws IOException {
        // search for empty space
        RecordHeader newRecord = null;

        for (RecordHeader next : getMemIndex().values()) {
            int free = next.getFreeSpace();
            if (dataLength <= next.getFreeSpace()) {
                newRecord = next.split();
                writeRecordHeaderToIndex(next);
                break;
            }
        }
        if (newRecord == null) {
            // append record to end of file - grows file to allocate space
            long fp = getFileLength();
            setFileLength(fp + dataLength);
            newRecord = new RecordHeader(fp, dataLength);
        }
        return newRecord;
    }

    /**
     * Appends an entry to end of index. Assumes that insureIndexSpace() has already been called.
     */
    protected void addEntryToIndex(String key, RecordHeader newRecord, int currentNumRecords) throws IOException {
        DbByteArrayOutputStream temp = new DbByteArrayOutputStream(MAX_KEY_LENGTH);
        (new DataOutputStream(temp)).writeUTF(key);
        if (temp.size() > MAX_KEY_LENGTH) {
            throw new IOException("Key is larger than permitted size of " + MAX_KEY_LENGTH + " bytes");
        }
        file.seek(indexPositionToKeyFp(currentNumRecords));
        temp.writeTo(file);
        file.seek(indexPositionToRecordHeaderFp(currentNumRecords));
        newRecord.write(file);
        newRecord.setIndexPosition(currentNumRecords);
        writeNumRecordsHeader(currentNumRecords+1);

        getMemIndex().put(key, newRecord);
    }

    /**
     * Updates the contents of the given record. A RecordsFileException is thrown if the new data does not
     * fit in the space allocated to the record. The header's data count is updated, but not
     * written to the file.
     */
    protected void writeRecordData(RecordHeader header, RecordWriter rw) throws IOException {
        if (rw.getDataLength() > header.dataCapacity) {
            throw new IOException ("Record data does not fit");
        }
        header.dataCount = rw.getDataLength();
        file.seek(header.dataPointer);
        rw.writeTo(file);
    }


    /**
     * Updates the contents of the given record. A RecordsFileException is thrown if the new data does not
     * fit in the space allocated to the record. The header's data count is updated, but not
     * written to the file.
     */
    protected void writeRecordData(RecordHeader header, byte[] data) throws IOException {
        if (data.length > header.dataCapacity) {
            throw new IOException ("Record data does not fit");
        }
        header.dataCount = data.length;
        file.seek(header.dataPointer);
        file.write(data, 0, data.length);
    }


    // Checks to see if there is space for and additional index entry. If
    // not, space is created by moving records to the end of the file.
    protected void insureIndexSpace(int requiredNumRecords) throws IOException {
        int currentNumRecords = getNumRecords();
        long endIndexPtr = indexPositionToKeyFp(requiredNumRecords);
        if (endIndexPtr > getFileLength() && currentNumRecords == 0) {
            setFileLength(endIndexPtr);
            dataStartPtr = endIndexPtr;
            writeDataStartPtrHeader(dataStartPtr);
            return;
        }
        while (endIndexPtr > dataStartPtr) {
            RecordHeader first = getRecordAt(dataStartPtr);
            byte[] data = readRecordData(first);
            first.dataPointer = getFileLength();
            first.dataCapacity = data.length;
            setFileLength(first.dataPointer + data.length);
            writeRecordData(first, data);
            writeRecordHeaderToIndex(first);
            dataStartPtr += first.dataCapacity;
            writeDataStartPtrHeader(dataStartPtr);
        }
    }
}