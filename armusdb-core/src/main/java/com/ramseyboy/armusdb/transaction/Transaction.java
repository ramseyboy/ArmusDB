package com.ramseyboy.armusdb.transaction;

public interface Transaction {

    public byte[] get(byte[] key);

    public void put(byte[] key, byte[] value);

    public byte[] delete(byte[] key);

}
