package com.sensorplex.sdk;

public class StatusPacket extends PdiPacket {
    public enum ChargingState {
        Discharged,
        Charging,
        Charged
    }

    public StatusPacket(byte[] _bytes) {
        super(PDI_CMD_STATUS, _bytes);

        model = data.get();
        chargerState = data.get();
        int rawBatt = data.getShort() & 0xFFFF;
        vBatt = 1.0f * rawBatt / 100;
    }

    byte model;
    byte chargerState;
    float vBatt;

    public byte getModel() {
        return model;
    }

    public ChargingState getChargerState() {
        switch (chargerState) {
            case 0:
                return ChargingState.Discharged;
            case 1:
                return ChargingState.Charging;
            default:
                return ChargingState.Charged;
        }
    }

    public float getVBatt() {
        return vBatt;
    }
}
