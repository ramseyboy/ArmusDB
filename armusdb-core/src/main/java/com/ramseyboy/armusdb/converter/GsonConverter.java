package com.ramseyboy.armusdb.converter;


import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Use GSON to serialize classes to a bytes.
 * <p>
 * Note: This will only work when concrete classes are specified for {@code T}. If you want to specify an interface for
 * {@code T} then you need to also include the concrete class name in the serialized byte array so that you can
 * deserialize to the appropriate type.
 */
public class GsonConverter<T> implements Converter<T> {

    private final Gson gson;
    private final Class<T> type;

    public GsonConverter(Gson gson, Class<T> type) {
        this.gson = gson;
        this.type = type;
    }

    @Override
    public T deserialize(byte[] bytes) throws IOException {
        Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes));
        return gson.fromJson(reader, type);
    }

    @Override
    public byte[] serialize(T o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        Writer writer = new OutputStreamWriter(baos);
        gson.toJson(o, writer);
        writer.close();
        return baos.toByteArray();
    }
}