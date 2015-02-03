package com.ramseyboy.armusdb.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class SerializedConverter<T extends Serializable> implements Converter<T> {

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(byte[] bytes) throws IOException {
        if (bytes == null) {
            throw new IllegalArgumentException("The byte[] must not be null");
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        if (inputStream == null) {
            throw new IllegalArgumentException("The InputStream must not be null");
        }
        ObjectInputStream in = null;
        try {
            // stream closed in the finally
            in = new ObjectInputStream(inputStream);
            return (T) in.readObject();

        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    @Override
    public byte[] serialize(T o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        serialize(o, baos);
        return baos.toByteArray();
    }

    private void serialize(Serializable obj, OutputStream outputStream) throws IOException {
        if (outputStream == null) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        }
        ObjectOutputStream out = null;
        try {
            // stream closed in the finally
            out = new ObjectOutputStream(outputStream);
            out.writeObject(obj);

        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }
}
