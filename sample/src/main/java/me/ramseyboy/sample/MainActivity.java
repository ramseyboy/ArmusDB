package me.ramseyboy.sample;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.google.gson.Gson;
import com.ramseyboy.armusdb.ext.leveldb.DB;
import com.ramseyboy.armusdb.converter.Converter;
import com.ramseyboy.armusdb.converter.GsonConverter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File dbLoc = new File(getFilesDir(), "leveldbtest.db");
        DB db = new DB(dbLoc);

        Converter<Record> recordConverter = new GsonConverter<>(new Gson(), Record.class);

        db.open();
        try {
            db.put("the first key".getBytes(), recordConverter.serialize(new Record("walker", "25")));
            Record val = recordConverter.deserialize(db.get("the first key".getBytes()));
            Toast.makeText(this, val.name + " -> " + val.age, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            db.close();
        }
    }

    static class Record implements Serializable, Parcelable {
        public String name, age;

        Record(String name, String age) {
            this.name = name;
            this.age = age;
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.name);
            dest.writeString(this.age);
        }

        private Record(Parcel in) {
            this.name = in.readString();
            this.age = in.readString();
        }

        public static final Parcelable.Creator<Record> CREATOR = new Parcelable.Creator<Record>() {
            public Record createFromParcel(Parcel source) {
                return new Record(source);
            }

            public Record[] newArray(int size) {
                return new Record[size];
            }
        };
    }
}
