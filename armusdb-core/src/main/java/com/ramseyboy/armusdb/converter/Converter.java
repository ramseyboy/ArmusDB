package com.ramseyboy.armusdb.converter;

import java.io.IOException;

/**
 * Convert a byte stream to and from a concrete type.
 *
 * @param <T> Object type.
 */
public interface Converter<T> {

    /** Converts bytes to an object. */
    T deserialize(byte[] bytes) throws IOException;

    /** Converts o to bytes written to the specified stream. */
    byte[] serialize(T o) throws IOException;
}
