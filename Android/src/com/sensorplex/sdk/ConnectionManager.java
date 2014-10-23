package com.sensorplex.sdk;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.UUID;

public class ConnectionManager {
    private BluetoothGatt mBluetoothGatt;

    private final static String TAG = ConnectionManager.class.getSimpleName();

    public final static UUID UUID_SP_SERVICE = UUID.fromString("01000000-0000-0000-0000-000000000080");
    public final static UUID UUID_SP_WRITE_CHARACTERISTIC = UUID.fromString("04000000-0000-0000-0000-000000000080");

    public ConnectionManager(BluetoothGatt bluetoothGatt) {
        mBluetoothGatt = bluetoothGatt;
    }

    public void controlLed(boolean isChecked, boolean isGreenColor) {
        // 0x01 = green, 0x02 = red, 0x80 = restore internal control
        byte argument = isChecked ? (isGreenColor ? (byte) 0x1 : (byte) 0x2) : (byte) 0x80;
        sendCommand(PdiPacket.BLE_CMD_SETLED, argument);
    }

    public void askVersion() {
        sendCommand(PdiPacket.PDI_CMD_VERSION, (byte) 0x0);
    }


    public void askStatus() {
        sendCommand(PdiPacket.PDI_CMD_STATUS, (byte) 0x0);
    }

    public void controlStreaming(boolean on) {
        BluetoothGattService service = getService();
        if (null == service) {
            Log.e(TAG, "Problem starting streaming: no GATT service");
            return;
        }

        for (BluetoothGattCharacteristic ch : getService().getCharacteristics()) {
            if (!ch.getUuid().equals(UUID_SP_WRITE_CHARACTERISTIC)) {
                mBluetoothGatt.setCharacteristicNotification(ch, on);
            }
        }

        sendCommand(PdiPacket.BLE_CMD_STREAMENABLE, on ? (byte) 0x01 : (byte) 0x00);
    }

    private void sendCommand(byte command, byte color) {
        BluetoothGattCharacteristic writeCh = getCharacteristic(UUID_SP_WRITE_CHARACTERISTIC);

        if (null == writeCh) {
            Log.e(TAG, "Problem sending command: no characteristic");
            return;
        }

        writeCh.setValue(new byte[]{command, color});
        mBluetoothGatt.writeCharacteristic(writeCh);
    }

    private BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        BluetoothGattService spService = getService();
        if (spService == null) return null;

        return spService.getCharacteristic(uuid);
    }

    private BluetoothGattService getService() {
        if (null == mBluetoothGatt) {
            Log.e(TAG, "BluetoothGatt");
            return null;
        }

        BluetoothGattService spService = mBluetoothGatt.getService(UUID_SP_SERVICE);
        if (null == spService) {
            Log.e(TAG, "no service");
            return null;
        }
        return spService;
    }

}
