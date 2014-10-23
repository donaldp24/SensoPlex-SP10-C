package com.sensorplex.sdk;

public class StreamingPacket extends PdiPacket {
    public static final int SCALE = 100000;
    private int sensors;

    RawDate date;
    int timestamp;
    byte rssi;
    byte battery;
    int bleState;
    long pressure;
    int temperature;
    float[] gyros; // X,Y,Z
    float[] accels; // X,Y,Z
    float[] quaternion; // W,X,Y,Z
    float[] compass; //X,Y,Z
    float[] linearAccel; //X,Y,Z
    float[] eulerAccel; //X,Y,Z
    float[] rotMatrix; //X1, X2, X3, Y1, Y2, Y3, Z1, Z2, Z3
    int heading;
    long userData;

    private static final int LOGDATA_TIMEDATE = 0x0001;
    private static final int LOGDATA_TIMESTAMP = 0x0002; // 2
    private static final int LOGDATA_BATTERYVOLTS = 0x0004; // 3
    private static final int LOGDATA_BLESTATE = 0x0008; // 4
    private static final int LOGDATA_GYROS = 0x0010; // 5
    private static final int LOGDATA_ACCELS = 0x0020; // 6
    private static final int LOGDATA_QUATERNION = 0x0040; // 7
    private static final int LOGDATA_COMPASS = 0x0080; // 8
    private static final int LOGDATA_PRESSURE = 0x0100; // 9
    private static final int LOGDATA_TEMPERATURE = 0x0200; // 10
    private static final int LOGDATA_LINEARACCEL = 0x0400; // 11
    private static final int LOGDATA_EULER = 0x0800; // 12
    private static final int LOGDATA_RSSI = 0x1000; // 13
    private static final int LOGDATA_ROTMATRIX = 0x2000; // 14
    private static final int LOGDATA_HEADING = 0x4000; // 15
    private static final int LOGDATA_USERDATA = 0x8000; // 16

    public StreamingPacket(byte[] bytes) {
        super(PDI_CMD_STREAMRECORD, bytes);

        sensors = data.getShort();

        if (hasTimeDate()) {
            date = new RawDate();
            date.second = data.get();
            date.minute = data.get();
            date.hour = data.get();
            date.month = data.get();
            date.day = data.get();
            date.year = data.get();
        }

        if (hasTimestamp())
            timestamp = data.getInt();
        if (hasBatteryVolts())
            battery = data.get();
        if (hasBleState())
            bleState = data.get() & 0xFF;
        if (hasGyros())
            gyros = getFloatArray(3, SCALE);
        if (hasAccels())
            accels = getFloatArray(3, SCALE);
        if (hasQuaternion())
            quaternion = getFloatArray(4, SCALE * 10000);
        if (hasCompass())
            compass = getFloatArray(3, SCALE);
        if (hasPressure()) {
            pressure = data.getInt();
            pressure &= 0xFFFFFFFF;
        }
        if (hasTemperature())
            temperature = data.getShort() & 0xFFFF;
        if (hasLinearAccel())
            linearAccel = getFloatArray(3, SCALE);
        if (hasEuler())
            eulerAccel = getFloatArray(3, SCALE);
        if (hasRssi())
            rssi = data.get();
        if (hasRotMatrix())
            rotMatrix = getFloatArray(9, SCALE);
        if (hasHeading())
            heading = data.getInt();
        if (hasUserData()) {
            userData = data.getInt();
            userData &= 0xFFFFFFFF;
        }
    }

    private float[] getFloatArray(int count, long scale) {
        float[] result = new float[count];
        for (int index = 0; index < count; ++index) {
            result[index] = (float) data.getInt() / scale;
        }
        return result;
    }

    private boolean hasFlag(int flag) {
        return 0 != (sensors & flag);
    }

    public boolean hasTimeDate() {
        return hasFlag(LOGDATA_TIMEDATE);
    }

    public boolean hasTimestamp() {
        return hasFlag(LOGDATA_TIMESTAMP);
    }

    public boolean hasBatteryVolts() {
        return hasFlag(LOGDATA_BATTERYVOLTS);
    }

    public boolean hasBleState() {
        return hasFlag(LOGDATA_BLESTATE);
    }

    public boolean hasGyros() {
        return hasFlag(LOGDATA_GYROS);
    }

    public boolean hasAccels() {
        return hasFlag(LOGDATA_ACCELS);
    }

    public boolean hasQuaternion() {
        return hasFlag(LOGDATA_QUATERNION);
    }

    public boolean hasCompass() {
        return hasFlag(LOGDATA_COMPASS);
    }

    public boolean hasPressure() {
        return hasFlag(LOGDATA_PRESSURE);
    }

    public boolean hasTemperature() {
        return hasFlag(LOGDATA_TEMPERATURE);
    }

    public boolean hasLinearAccel() {
        return hasFlag(LOGDATA_LINEARACCEL);
    }

    public boolean hasEuler() {
        return hasFlag(LOGDATA_EULER);
    }

    public boolean hasRssi() {
        return hasFlag(LOGDATA_RSSI);
    }

    public boolean hasRotMatrix() {
        return hasFlag(LOGDATA_ROTMATRIX);
    }

    public boolean hasHeading() {
        return hasFlag(LOGDATA_HEADING);
    }

    public boolean hasUserData() {
        return hasFlag(LOGDATA_USERDATA);
    }

    public RawDate getDate() {
        return date;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getRssi() {
        return rssi;
    }

    public float getBattery() {
        return battery / 10;
    }

    public int getBleState() {
        return bleState;
    }

    public long getPressure() {
        return pressure;
    }

    public float getTemperature() {
        return temperature / 10;
    }

    public float[] getGyros() {
        return gyros;
    }

    public float[] getAccels() {
        return accels;
    }

    public float[] getQuaternion() {
        return quaternion;
    }

    public float[] getCompass() {
        return compass;
    }

    public float[] getLinearAccel() {
        return linearAccel;
    }

    public float[] getEulerAccel() {
        return eulerAccel;
    }

    public float[] getRotMatrix() {
        return rotMatrix;
    }

    public int getHeading() {
        return heading;
    }

    public long getUserData() {
        return userData;
    }

    private void addOrSkip(StringBuilder sb, float[] data, boolean hasData, int count) {
        for (int i = 0; i < count; ++i) {
            if (hasData) sb.append(data[i]);
            sb.append(",");
        }
    }

    private void addOrSkip(StringBuilder sb, byte[] data, boolean hasData, int count) {
        for (int i = 0; i < count; ++i) {
            if (hasData) sb.append(data[i]);
            sb.append(",");
        }
    }

    private void addOrSkip(StringBuilder sb, float data, boolean hasData) {
        if (hasData) sb.append(data);
        sb.append(",");
    }

    private void addOrSkip(StringBuilder sb, int data, boolean hasData) {
        if (hasData) sb.append(data);
        sb.append(",");
    }

    private void addOrSkip(StringBuilder sb, byte data, boolean hasData) {
        if (hasData) sb.append(data);
        sb.append(",");
    }

    private void addOrSkip(StringBuilder sb, long data, boolean hasData) {
        if (hasData) sb.append(data);
        sb.append(",");
    }

    public String toCsvLine() {
        StringBuilder builder = new StringBuilder();

        if (hasTimeDate())
            addOrSkip(builder, getDate().getAllFields(), hasTimeDate(), 6);

        addOrSkip(builder, getTimestamp(), hasTimestamp());
        addOrSkip(builder, getBattery(), hasBatteryVolts());
        addOrSkip(builder, getBleState(), hasBleState());
        addOrSkip(builder, getGyros(), hasGyros(), 3);
        addOrSkip(builder, getAccels(), hasAccels(), 3);
        addOrSkip(builder, getQuaternion(), hasQuaternion(), 4);
        addOrSkip(builder, getCompass(), hasCompass(), 3);
        addOrSkip(builder, getPressure(), hasPressure());
        addOrSkip(builder, getTemperature(), hasTemperature());
        addOrSkip(builder, getLinearAccel(), hasLinearAccel(), 3);
        addOrSkip(builder, getEulerAccel(), hasEuler(), 3);
        addOrSkip(builder, getRotMatrix(), hasRotMatrix(), 9);
        addOrSkip(builder, getHeading(), hasHeading());

        return builder.toString();
    }
}
