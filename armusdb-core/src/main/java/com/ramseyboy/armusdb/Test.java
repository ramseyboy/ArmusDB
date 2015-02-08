package com.ramseyboy.armusdb;

import com.google.gson.Gson;
import com.ramseyboy.armusdb.converter.Converter;
import com.ramseyboy.armusdb.converter.GsonConverter;
import com.ramseyboy.armusdb.database.Database;
import com.ramseyboy.armusdb.database.FileDB;

import java.io.File;
import java.io.IOException;


public class Test {

    public void doWork() {
        Converter<Record> recordConverter = new GsonConverter<>(new Gson(), Record.class);

        final int KEY = 0;

        File path = new File("testDatabase.db");
        Database fileDB = new FileDB(path);
        try {
            fileDB.open();
            for (int i = KEY; i < 10; i++) {
                fileDB.put(String.valueOf(i), recordConverter.serialize(new Record("walker", "26")));
            }

            for (int i = KEY; i < 10; i++) {
                System.out.println(recordConverter.deserialize(fileDB.get(String.valueOf(i))).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileDB.close();
            } catch (IOException e) {
                //ignore closeable error
            }

        }
    }

    public static void main(String... args) {
        new Test().doWork();
    }
}
