package com.ramseyboy.armusdb.converter;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;

public class ParcelableConverter<T extends Parcelable> implements Converter<T> {

    private final Parcelable.Creator<T> creator;

    public ParcelableConverter(Parcelable.Creator<T> creator) {
        this.creator = creator;
    }

    @Override
    public T deserialize(byte[] bytes) throws IOException {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // this is extremely important!
        return creator.createFromParcel(parcel);
    }

    @Override
    public byte[] serialize(T o) throws IOException {
        Parcel parcel = Parcel.obtain();
        o.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }
}