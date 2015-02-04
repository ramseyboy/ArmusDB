package me.ramseyboy.sample;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Record implements Serializable, Parcelable {
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

    @Override
    public String toString() {
        return "Record{" +
                "name='" + name + '\'' +
                ", age='" + age + '\'' +
                '}';
    }
}