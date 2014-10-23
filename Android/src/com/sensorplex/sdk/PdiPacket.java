package com.sensorplex.sdk;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A simple class to handle PDI packets
 */
public class PdiPacket {
    private final static String TAG = PdiPacket.class.getSimpleName();

    // PDI commands
    public static final byte PDI_CMD_STREAMRECORD = (byte) 0x60;
    public static final byte PDI_CMD_STATUS = (byte) 0x30;
    public static final byte PDI_CMD_VERSION = (byte) 0x34;
    public static final byte PDI_CMD_CONFIG = (byte) 0x35;

    // BLE commands
    public static final byte BLE_CMD_SETLED = (byte) 0x80;
    public static final byte BLE_CMD_STREAMENABLE = (byte) 0x63;

    public static PdiPacket create(byte command, byte[] data) {
        try {
            switch (command) {
                case PDI_CMD_STREAMRECORD:
                    return new StreamingPacket(data);
                case PDI_CMD_VERSION:
                    return new VersionPacket(data);
                case PDI_CMD_STATUS:
                    return new StatusPacket(data);
                default:
                    return new PdiPacket(command, data);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception parsing packet", e);
            return null;
        }
    }

    protected PdiPacket(byte command, byte[] _data) {
        Command = command;
        bytes = _data;
        data = ByteBuffer.wrap(bytes);
        data.order(ByteOrder.LITTLE_ENDIAN);
    }

    public byte Command;

    private byte[] bytes;
    protected ByteBuffer data;
}
