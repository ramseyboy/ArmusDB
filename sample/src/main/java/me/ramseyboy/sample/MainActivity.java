package me.ramseyboy.sample;

import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//
//        File dbLoc = new File(getFilesDir(), "leveldbtest.db");
//        DB db = new DB(dbLoc);
//
//        Converter<Record> recordConverter = new GsonConverter<>(new Gson(), Record.class);
//
//        db.open();
//        try {
//            db.put("the first key", recordConverter.serialize(new Record("walker", "25")));
//            Record val = recordConverter.deserialize(db.get("the first key"));
//            Toast.makeText(this, val.name + " -> " + val.age, Toast.LENGTH_SHORT).show();
//        } catch (IOException e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//        } finally {
//            db.close();
//        }
    }
}
