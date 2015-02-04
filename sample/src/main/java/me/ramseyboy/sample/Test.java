package me.ramseyboy.sample;

import com.google.gson.Gson;
import com.ramseyboy.armusdb.database.Database;
import com.ramseyboy.armusdb.database.FileDB;
import com.ramseyboy.armusdb.converter.Converter;
import com.ramseyboy.armusdb.converter.GsonConverter;

import java.io.File;


public class Test {

    public static void main(String... args) throws Exception {
        Converter<Record> recordConverter = new GsonConverter<>(new Gson(), Record.class);

        final String KEY = "baz";

        File path = new File("testDatabase.db");
        Database fileDB = new FileDB(path);
        fileDB.open();
        try {
            fileDB.put(KEY, recordConverter.serialize(new Record("walker", "26")));

            System.out.println(recordConverter.deserialize(fileDB.get(KEY)).toString());
//            System.out.println(recordConverter.deserialize(fileDB.get("bar")).toString());
//            System.out.println(recordConverter.deserialize(fileDB.get("foo")).toString());
        } finally {
            fileDB.close();
        }
    }
}
