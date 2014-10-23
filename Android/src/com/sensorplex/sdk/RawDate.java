package com.sensorplex.sdk;

public class RawDate {
    public RawDate() {
    }

    public byte year;
    public byte month;
    public byte day;
    public byte hour;
    public byte minute;
    public byte second;

    public byte[] getAllFields() {
        return new byte[]{second, minute, hour, month, day, year};
    }
}
