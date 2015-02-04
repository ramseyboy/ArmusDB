package com.ramseyboy.armusdb.database;


import java.io.Closeable;
import java.io.IOException;

public interface Database extends Closeable {

    public abstract void open() throws IOException;

    public abstract byte[] get(String key) throws IOException;

    public abstract void put(String key, byte[] value) throws IOException;

    public abstract void delete(String key) throws IOException;

}
