package com.ramseyboy.armusdb;

import java.io.Serializable;

public class Record implements Serializable{
    public String name, age;

    Record(String name, String age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Record{" +
                "name='" + name + '\'' +
                ", age='" + age + '\'' +
                '}';
    }
}