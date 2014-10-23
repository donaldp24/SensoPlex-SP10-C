package com.sensorplex.sdk;

public class VersionPacket extends PdiPacket {
    public VersionPacket(byte[] bytes) {
        super(PDI_CMD_VERSION, bytes);

        version = data.get();
        revision = data.get();
        subrevision = data.get();
        month = data.get();
        day = data.get();
        year = data.get();
        model = data.get();
    }

    private byte version;
    private byte revision;
    private byte subrevision;
    private byte month;
    private byte day;
    private byte year;
    private byte model;

    public byte getYear() {
        return year;
    }

    public byte getVersion() {
        return version;
    }

    public byte getRevision() {
        return revision;
    }

    public byte getSubrevision() {
        return subrevision;
    }

    public byte getMonth() {
        return month;
    }

    public byte getDay() {
        return day;
    }

    public byte getModel() {
        return model;
    }

    @Override
    public String toString() {
        return String.format("v%d.%d.%d %02d/%d/%d", version, revision, subrevision, month, day, year);
    }
}
