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

package me.iasc.microduino.bluelight;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class PicControlActivity extends AbstractBleControlActivity {
    private final static String TAG = PicControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    public static int BLE_MSG_SEND_INTERVAL = 100;

    public static int COLOR_WHITE = Color.WHITE;
    public static int COLOR_SUN = Color.parseColor("#FFFFD600");

    private ToggleButton onButton;

//    private RecordManager recorder;

//    private BluetoothLeService mBluetoothLeService;
//    private BluetoothGattCharacteristic characteristicTX, characteristicRX;
//    private boolean mConnected = false, characteristicReady = false;
//
//    // Code to manage Service lifecycle.
//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder service) {
//            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//            if (!mBluetoothLeService.initialize()) {
//                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
//            }
//            // Automatically connects to the device upon successful start-up initialization.
//            mBluetoothLeService.connect(mDeviceAddress);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mBluetoothLeService = null;
//        }
//    };
//
//    // Handles various events fired by the Service.
//    // ACTION_GATT_CONNECTED: connected to a GATT server.
//    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
//    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
//    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
//    //                        or notification operations.
//    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
//                mConnected = true;
//                updateConnectionState(R.string.connected);
//                invalidateOptionsMenu();
//            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
//                mConnected = false;
//                updateConnectionState(R.string.disconnected);
//                invalidateOptionsMenu();
//            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
//                BluetoothGattService gattService = mBluetoothLeService.getSoftSerialService();
//                if (gattService == null) {
//                    Toast.makeText(SoundControlActivity.this, getString(R.string.without_service), Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                if(mDeviceName.startsWith("Microduino")) {
//                    characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_MD_RX_TX);
//                }else if(mDeviceName.startsWith("EtOH")) {
//                    characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_ETOH_RX_TX);
//                }
//                characteristicRX = characteristicTX;
//
//                if (characteristicTX != null) {
//                    isSerial.setText("Serial ready");
//                    updateReadyState(R.string.ready);
//                } else {
//                    isSerial.setText("Serial can't be found");
//                }
//
//            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(mBluetoothLeService.EXTRA_DATA));
//            }
//        }
//    };

//    private static IntentFilter makeGattUpdateIntentFilter() {
//        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
//        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
//        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
//        return intentFilter;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.pic_control);
        super.onCreate(savedInstanceState);

        wait_ble(BLE_MSG_SEND_INTERVAL);

        onButton = (ToggleButton) findViewById(R.id.toggleButton);
        onButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (onButton.isChecked()) {
                    // makeChange(multiColorPicker.getColors());
                } else {
                    makeChange(0);
                }
            }
        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
//        if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
//            Log.d(TAG, "Connect request result=" + result);
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        unregisterReceiver(mGattUpdateReceiver);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unbindService(mServiceConnection);
//        mBluetoothLeService = null;
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.gatt_services, menu);
//        if (mConnected) {
//            menu.findItem(R.id.menu_connect).setVisible(false);
//            menu.findItem(R.id.menu_disconnect).setVisible(true);
//        } else {
//            menu.findItem(R.id.menu_connect).setVisible(true);
//            menu.findItem(R.id.menu_disconnect).setVisible(false);
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_connect:
//                mBluetoothLeService.connect(mDeviceAddress);
//                return true;
//            case R.id.menu_disconnect:
//                mBluetoothLeService.disconnect();
//                return true;
//            case android.R.id.home:
//                onBackPressed();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

//    private void updateConnectionState(final int resourceId) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mConnectionState.setText(resourceId);
//            }
//        });
//    }

//    private void updateReadyState(final int resourceId) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                wait_ble(3000);
//
//                characteristicReady = true;
//                Toast.makeText(SoundControlActivity.this, getString(resourceId), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void displayData(String data) {
//        if (data != null) {
//            //You can add some code here
//            Log.v(TAG, "BLE Return Data : " + data);
//        }
//    }

    // on change of single color
    private void makeChange(int color) {
        StringBuilder sb = new StringBuilder();

        sb.append(Color.red(color)).append(",")
                .append(Color.green(color)).append(",")
                .append(Color.blue(color)).append(",")
                .append("-1\n");

        sendMessage(sb.toString());
    }

    private void makeChange(int[] colors) {
        for (int i = 0; i < colors.length; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(Color.red(colors[i])).append(",")
                    .append(Color.green(colors[i])).append(",")
                    .append(Color.blue(colors[i])).append(",")
                    .append(i).append("\n");
            sendMessage(sb.toString());

            // add delay for ble massage transfer
            wait_ble(BLE_MSG_SEND_INTERVAL);
        }
    }

//    public void wait_ble(int i) {
//        try {
//            Thread.sleep(i);
//        } catch (Exception e) {
//            // ignore
//        }
//    }
//
//    private void sendMessage(String msg) {
//        Log.d(TAG, "Sending Result=" + msg);
//
//        if (characteristicReady && (mBluetoothLeService != null)
//                && (characteristicTX != null) && (characteristicRX != null)) {
//            characteristicTX.setValue(msg);
//            mBluetoothLeService.writeCharacteristic(characteristicTX);
//            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
//        } else {
//            Toast.makeText(SoundControlActivity.this, "BLE Disconnected", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void iascDialog() {
//        LayoutInflater inflater = getLayoutInflater();
//        View layout = inflater.inflate(R.layout.iasc_dialog,
//                (ViewGroup) findViewById(R.id.dialog));
//        new AlertDialog.Builder(this).setView(layout)
//                .setPositiveButton("OK", null).show();
//    }
}