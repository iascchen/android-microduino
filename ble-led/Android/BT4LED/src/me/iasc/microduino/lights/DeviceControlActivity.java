/*
 * Copyright (C) 2015 Iasc CHEN
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

package me.iasc.microduino.lights;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.*;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.*;
import android.widget.*;
import me.iasc.microduino.lights.ble.BluetoothLeService;
import me.iasc.microduino.lights.colorpicker.ColorPicker;
import me.iasc.microduino.lights.colorpicker.MultiColorPicker;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static int BLE_MSG_SEND_INTERVAL = 100;

    private MultiColorPicker multiColorPicker;
    private ColorPicker singleColorPicker;

    private Switch singleMultiColorSwitch;
    private ToggleButton onButton;
    private TextView isSerial, mConnectionState;
    private ImageView infoButton;

    private String mDeviceName, mDeviceAddress;

    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic characteristicTX, characteristicRX;
    private boolean mConnected = false, characteristicReady = false;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                BluetoothGattService gattService = mBluetoothLeService.getSoftSerialService();
                if (gattService == null) {
                    Toast.makeText(DeviceControlActivity.this, getString(R.string.without_service), Toast.LENGTH_SHORT).show();
                    return;
                }

                characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
                //characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
                characteristicRX = characteristicTX;

                if (characteristicTX != null) {
                    isSerial.setText("Serial ready");
                    updateReadyState(R.string.ready);
                } else {
                    isSerial.setText("Serial can't be found");
                }

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(mBluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        // is serial present?
        isSerial = (TextView) findViewById(R.id.isSerial);

        multiColorPicker = (MultiColorPicker) findViewById(R.id.multiColorPicker);
        singleColorPicker = (ColorPicker) findViewById(R.id.colorPicker);

        singleMultiColorSwitch = (Switch) findViewById(R.id.switchSingleMulti);
        singleMultiColorSwitch.setChecked(true);

        singleMultiColorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    multiColorPicker.setVisibility(View.VISIBLE);
                    singleColorPicker.setVisibility(View.GONE);
                } else {
                    multiColorPicker.setVisibility(View.GONE);
                    singleColorPicker.setVisibility(View.VISIBLE);
                }
            }
        });

        multiColorPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    if (characteristicReady) {
                        onButton.setChecked(true);
                        makeChange(multiColorPicker.getColors());
                    }
                }
                return false;
            }
        });

        singleColorPicker.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_UP == event.getAction()) {
                    if (characteristicReady) {
                        onButton.setChecked(true);
                        makeChange(singleColorPicker.getColor());
                    }
                }
                return false;
            }
        });

        onButton = (ToggleButton) findViewById(R.id.toggleButton);
        onButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (onButton.isChecked()) {
                    if (singleMultiColorSwitch.isChecked()) {
                        makeChange(multiColorPicker.getColors());
                    } else {
                        makeChange(singleColorPicker.getColor());
                    }
                } else {
                    makeChange(0);
                }
            }
        });

        infoButton = (ImageView) findViewById(R.id.infoImage);

        infoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                iascDialog();
            }
        });

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void updateReadyState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                wait_ble(3000);

                characteristicReady = true;
                Toast.makeText(DeviceControlActivity.this, getString(resourceId), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            //You can add some code here
            Log.v(TAG, "BLE Return Data : " + data);
        }
    }

    // on change of single color
    private void makeChange(int color) {
        StringBuffer sb = new StringBuffer();

        sb.append(Color.red(color)).append(",")
                .append(Color.green(color)).append(",")
                .append(Color.blue(color)).append(",")
                .append("-1\n");

        sendMessage(sb.toString());
    }

    private void makeChange(int[] colors) {
        for (int i = 0; i < colors.length; i++) {
            StringBuffer sb = new StringBuffer();
            sb.append(Color.red(colors[i])).append(",")
                    .append(Color.green(colors[i])).append(",")
                    .append(Color.blue(colors[i])).append(",")
                    .append(i).append("\n");
            sendMessage(sb.toString());

            // add delay for ble massage transfer
            wait_ble(BLE_MSG_SEND_INTERVAL);
        }
    }

    public void wait_ble(int i) {
        try {
            Thread.sleep(i);
        } catch (Exception e) {
            // ignore
        }
    }

    private void sendMessage(String msg) {
        Log.d(TAG, "Sending Result=" + msg);

        if (characteristicReady && (mBluetoothLeService != null)
                && (characteristicTX != null) && (characteristicRX != null)) {
            characteristicTX.setValue(msg);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        } else {
            Toast.makeText(DeviceControlActivity.this, "BLE Disconnected", Toast.LENGTH_SHORT).show();
        }
    }

    private void iascDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.iasc_dialog,
                (ViewGroup) findViewById(R.id.dialog));
        new AlertDialog.Builder(this).setView(layout)
                .setPositiveButton("OK", null).show();
    }
}