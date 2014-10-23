package com.sensorplex.sdk;

import android.util.Log;

import java.io.ByteArrayOutputStream;

public class PdiParser {
    private final static String TAG = PdiParser.class.getSimpleName();

    private enum States {IDLE, CMD, STF, DATA}

    private final byte PDI_SOP = (byte) 0xD1;
    private final byte PDI_EOP = (byte) 0xDF;
    private final byte PDI_STF = (byte) 0xDE;

    private ByteQueue queue = null;
    private int errorCount = 0;

    public int getErrorCount() {
        return errorCount;
    }

    public PdiParser(ByteQueue queue) {
        this.queue = queue;
    }

    private void test() throws InterruptedException {
        byte[] sample = new byte[]{(byte) 0xD1, (byte) 0x60, (byte) 0x12, (byte) 0xFE, (byte) 0x7A, (byte) 0x63, (byte) 0x55,
                (byte) 0x00, (byte) 0x83, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x06, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
                (byte) 0x35, (byte) 0x0C, (byte) 0x00, (byte) 0x00, (byte) 0x32, (byte) 0x01, (byte) 0x1C, (byte) 0x06, (byte) 0x00,
                (byte) 0x00, (byte) 0xB2, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xEC, (byte) 0xFC, (byte) 0xFF, (byte) 0xFF,
                (byte) 0xFB, (byte) 0x96, (byte) 0x56, (byte) 0xFF, (byte) 0x32, (byte) 0xCA, (byte) 0xC4, (byte) 0xFF, (byte) 0x13,
                (byte) 0x96, (byte) 0x81, (byte) 0x00, (byte) 0xC5, (byte) 0xB0, (byte) 0x03, (byte) 0x17, (byte) 0x0D, (byte) 0x60,
                (byte) 0x74, (byte) 0x84, (byte) 0xCF, (byte) 0x58, (byte) 0xC3, (byte) 0xAC, (byte) 0x27, (byte) 0xA0, (byte) 0x4A,
                (byte) 0x50, (byte) 0xE0, (byte) 0xB0, (byte) 0x4C, (byte) 0xE9, (byte) 0xD7, (byte) 0x40, (byte) 0x48, (byte) 0x77,
                (byte) 0xD9, (byte) 0x28, (byte) 0xF9, (byte) 0x0A, (byte) 0x36, (byte) 0xC0, (byte) 0x04, (byte) 0x3D, (byte) 0xF4,
                (byte) 0xF0, (byte) 0x07, (byte) 0xCC, (byte) 0xDE, (byte) 0xDF, (byte) 0xED, (byte) 0x69, (byte) 0xE6, (byte) 0x00,
                (byte) 0x83, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xF1, (byte) 0xDF};

        queue.put(sample);
    }

    public PdiPacket next() throws InterruptedException {
        byte checksum = 0x0;
        byte command = 0x0;
        ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
        States state = States.IDLE;

        while (true) {
            byte c = queue.next();

            switch (state) {

                case IDLE:
                    if (c == PDI_SOP) {
                        state = States.CMD;
                    }
                    break;
                case CMD:
                    command = c;
                    checksum = c;
                    state = States.DATA;
                    break;
                case STF:
                    checksum += c;
                    dataStream.write(c);
                    state = States.DATA;
                    break;
                case DATA:
                    switch (c) {
                        case PDI_STF:
                            state = States.STF;
                            checksum += c;
                            break;
                        case PDI_EOP:
                            state = States.IDLE;
                            if (checksum == 0)
                                return PdiPacket.create((byte) command, dataStream.toByteArray());
                            else {
                                Log.e(TAG, "Bad packet found, command: " + command);
                                ++errorCount;
                                checksum = 0;
                                dataStream = new ByteArrayOutputStream();
                            }
                            break;
                        default:
                            dataStream.write(c);
                            checksum += c;
                            break;
                    }
                    break;
            }
        }
    }
}
