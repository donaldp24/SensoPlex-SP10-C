/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sensorplex.example;

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.sensorplex.sdk.*;

import java.text.DecimalFormat;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {

    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    // Controls
    private TextView _ConnectionState;

    private TextView txtErrorCount;
    private TextView txtFirmware;
    private TextView txtBatteryVolts;
    private TextView txtChargingState;

    private TextView txtDateTime;
    private TextView txtTimestamp;
    //private TextView txtRssi;
    private TextView txtBattery;
    private TextView txtPressure;
    private TextView txtTemperature;

    private TextView[] gyroControls;
    private TextView[] accelControls;
    private TextView[] quatControls;
    private TextView[] compassControls;
    private TextView[] linAccelControls;
    private TextView[] eulerAngleControls;

    private RadioGroup rgLedColors;
    private RadioButton rbLedGreen;
    private String _DeviceAddress;

    private Switch switchStreaming;
    private Switch switchLogging;

    private Thread pdiPacketProcessor = new PdiPacketProcessor();
    private Thread pdiErrorMonitor = new PdiErrorMonitor();

    private LoggingManager loggingManager = new LoggingManager(this);


    // BLE Service
    private BluetoothLeService _BluetoothLeService;

    // Data
    ByteQueue queue;
    PdiParser pdiParser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_control_screen);

        queue = new ByteQueue();
        pdiParser = new PdiParser(queue);

        final Intent intent = getIntent();
        String _DeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        _DeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        _ConnectionState = (TextView) findViewById(R.id.connection_state);

        // Bind the BLE service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, _ServiceConnection, BIND_AUTO_CREATE);

        // Sets up UI references
        getActionBar().setTitle(_DeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        ((TextView) findViewById(R.id.device_address)).setText(_DeviceAddress);
        Switch swLed = (Switch) findViewById(R.id.led_control_switch);
        rgLedColors = (RadioGroup) findViewById(R.id.rg_led_color);
        rbLedGreen = (RadioButton) findViewById(R.id.rb_green);
        RadioButton rbLedRed = (RadioButton) findViewById(R.id.rb_red);
        txtErrorCount = (TextView) findViewById(R.id.txtErrorCount);
        txtFirmware = (TextView) findViewById(R.id.txtFirmware);
        txtBatteryVolts = (TextView) findViewById(R.id.txtBatteryVolts);
        txtChargingState = (TextView) findViewById(R.id.txtChargeState);
        txtDateTime = (TextView) findViewById(R.id.txtDateTime);

        switchStreaming = (Switch) findViewById(R.id.switchStreaming);
        switchLogging = (Switch) findViewById(R.id.switchLogging);

        gyroControls = findTextViews(new int[]{R.id.txtGyrosX, R.id.txtGyrosY, R.id.txtGyrosZ});
        accelControls = findTextViews(new int[]{R.id.txtAccelsX, R.id.txtAccelsY, R.id.txtAccelsZ});
        quatControls = findTextViews(new int[]{R.id.txtQuatW, R.id.txtQuatX, R.id.txtQuatY, R.id.txtQuatZ});
        compassControls = findTextViews(new int[]{R.id.txtCompX, R.id.txtCompY, R.id.txtCompZ});
        linAccelControls = findTextViews(new int[]{R.id.txtLinAccX, R.id.txtLinAccY, R.id.txtLinAccZ});
        eulerAngleControls = findTextViews(new int[]{R.id.txtEulerX, R.id.txtEulerY, R.id.txtEulerZ});

        txtTimestamp = (TextView) findViewById(R.id.txtTimestamp);
        //txtRssi = (TextView) findViewById(R.id.txtRssi);
        txtBattery = (TextView) findViewById(R.id.txtBattery);
        txtPressure = (TextView) findViewById(R.id.txtPressure);
        txtTemperature = (TextView) findViewById(R.id.txtTemperature);

        // Set up event handlers
        swLed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i = 0; i < rgLedColors.getChildCount(); ++i)
                    rgLedColors.getChildAt(i).setEnabled(isChecked);

                _BluetoothLeService.controlLed(isChecked, rbLedGreen.isChecked());
            }
        });

        rbLedGreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _BluetoothLeService.controlLed(true, true);
            }
        });

        rbLedRed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _BluetoothLeService.controlLed(true, false);
            }
        });

        switchStreaming.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchLogging.setEnabled(isChecked);

                _BluetoothLeService.controlStreaming(isChecked);
            }
        });

        switchLogging.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                loggingManager.enable(isChecked);
            }
        });

        pdiPacketProcessor.start();
        pdiErrorMonitor.start();

        loggingManager.init();
    }

    public void btnGetFirmware_onclick(View view) {
        _BluetoothLeService.askVersion();
    }

    public void btnGetStatus_click(View view) {
        _BluetoothLeService.askStatus();
    }

    class PdiPacketProcessor extends Thread {
        /**
         * Main packet handling loop goes here. Separate thread waits for bytes to arrive, collects into packets and
         * pumps to UI thread for display.
         */
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    final PdiPacket packet = pdiParser.next();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FillData(packet);
                        }
                    });
                    Log.i(TAG, "Data received");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class PdiErrorMonitor extends Thread {
        @Override
        public void run() {
            try {
                int lastErrorCount = 0;
                while (!Thread.interrupted()) {
                    final int newErrorCount = pdiParser.getErrorCount();
                    if (newErrorCount > lastErrorCount)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                txtErrorCount.setText("" + newErrorCount);
                            }
                        });
                    lastErrorCount = newErrorCount;
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {

            }
        }
    }

    /// A block of aux UI handling methods - batch manipulation with controls.
    private void setFields(TextView[] fields, byte[] values, boolean hasData) {
        for (int i = 0; i < fields.length; ++i) {
            fields[i].setText(hasData ? "" + values[i] : "");
        }
    }

    private void setFields(TextView[] fields, float[] values, boolean hasData) {
        final DecimalFormat format = new DecimalFormat("#.####");
        for (int i = 0; i < fields.length; ++i) {
            fields[i].setText(hasData ? format.format(values[i]) : "");
        }
    }

    private TextView[] findTextViews(int[] ids) {
        TextView[] result = new TextView[ids.length];
        for (int index = 0; index < ids.length; ++index) {
            result[index] = (TextView) findViewById(ids[index]);
        }
        return result;
    }

    /**
     * Handy method ro update or clear TextView
     *
     * @param tv      - TextView to process
     * @param value   - value to assign
     * @param hasData - whether to assign value or to clear
     */
    private void updateTextView(TextView tv, String value, boolean hasData) {
        tv.setText(hasData ? value : "");
    }

    private void FillData(PdiPacket packet) {
        if (null == packet)
            return;

        if (packet instanceof StreamingPacket)
            FillStreamingData((StreamingPacket) packet);
        else if (packet instanceof VersionPacket)
            FillVersionData((VersionPacket) packet);
        else if (packet instanceof StatusPacket)
            FillStatusData((StatusPacket) packet);
    }

    private void FillStatusData(StatusPacket packet) {
        final DecimalFormat format = new DecimalFormat("#.##");
        txtBatteryVolts.setText(format.format(packet.getVBatt()) + "V");
        txtChargingState.setText(packet.getChargerState().toString());
    }

    private void FillVersionData(VersionPacket packet) {
        txtFirmware.setText(packet.toString());
    }

    void FillStreamingData(StreamingPacket packet) {
        // special handling for date
        if (packet.hasTimeDate()) {
            final String format = "%02d:%02d:%02d %02d/%02d/%02d";
            RawDate date = packet.getDate();
            txtDateTime.setText(String.format(format, date.hour, date.minute, date.second, date.month, date.day, date.year));
        }

        // perform update and cleanup for simple fields
        updateTextView(txtTimestamp, "" + packet.getTimestamp(), packet.hasTimestamp());
        //updateTextView(txtRssi, "" + packet.getRssi(), packet.hasRssi());
        updateTextView(txtBattery, "" + packet.getBattery(), packet.hasBatteryVolts());
        updateTextView(txtPressure, "" + packet.getPressure(), packet.hasPressure());
        updateTextView(txtTemperature, "" + packet.getTemperature(), packet.hasTemperature());

        // perform update and cleanup for collections
        setFields(gyroControls, packet.getGyros(), packet.hasGyros());
        setFields(accelControls, packet.getAccels(), packet.hasAccels());
        setFields(quatControls, packet.getQuaternion(), packet.hasQuaternion());
        setFields(compassControls, packet.getCompass(), packet.hasCompass());
        setFields(linAccelControls, packet.getLinearAccel(), packet.hasLinearAccel());
        setFields(eulerAngleControls, packet.getEulerAccel(), packet.hasEuler());

        loggingManager.writeLine(packet.toCsvLine());
    }

    // Code to manage Service lifecycle
    private final ServiceConnection _ServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            _BluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!_BluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            _BluetoothLeService.connect(_DeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            _BluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device. This can be a result of read or notification operations.
    private final BroadcastReceiver _GattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                switchStreaming.setEnabled(true);
                // do nothing
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState(R.string.disconnected);
                switchStreaming.setChecked(false);
                switchStreaming.setEnabled(false);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                try {
                    queue.put(data);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Failed to push data for processing");
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(_GattUpdateReceiver, makeGattUpdateIntentFilter());
        if (_BluetoothLeService != null) {
            _BluetoothLeService.connect(_DeviceAddress);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        _BluetoothLeService.close();
        pdiPacketProcessor.interrupt();
        pdiErrorMonitor.interrupt();

        loggingManager.close();

        try {
            unregisterReceiver(_GattUpdateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            unbindService(_ServiceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        _BluetoothLeService = null;

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        _BluetoothLeService.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_share:
                sendCsv();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendCsv() {
        Uri file = loggingManager.prepareToSend();
        if (null == file)
            return;

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        emailIntent.putExtra(Intent.EXTRA_STREAM, file);

        startActivity(Intent.createChooser(emailIntent, "Send CSV by Email..."));
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _ConnectionState.setText(resourceId);
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
