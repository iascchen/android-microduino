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

package me.iasc.microduino.blueledpad;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;
import me.iasc.microduino.blueledpad.ble.BluetoothLeService;
import me.iasc.microduino.blueledpad.db.LedMatrixDAO;
import me.iasc.microduino.blueledpad.db.LedMatrixModel;

import java.util.ArrayList;
import java.util.List;

public class UploadMatrixActivity extends Activity {
    private final static String TAG = UploadMatrixActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_LED_MATRIX = "LED_MATRIX";

    public static int BLE_MSG_SEND_INTERVAL = 100;
    public static int BLE_MSG_BUFFER_LEN = 18;

    public static final int MATRIX_N = 8;
    public static final int MATRIX_NN = MATRIX_N * MATRIX_N;

    private String currDeviceName, currDeviceAddress;

    LedMatrixModel currMatrix;
    LedMatrixDAO mtxDAO = null;

    Activity activity;
    List<LedButton> ledButtons = new ArrayList<LedButton>();
    List<Integer> ledColors = new ArrayList<Integer>();

    int currColorIndex = 1;
    boolean drawMode = false;

    private Button buttonSend, buttonSave, buttonDelete;
    private TextView isSerial, mConnectionState, returnText;
    private EditText editNameInDialog;
    private ImageView infoButton;
    private Switch switchDraw;

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
            mBluetoothLeService.connect(currDeviceAddress);
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
                    Toast.makeText(UploadMatrixActivity.this, getString(R.string.without_service), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currDeviceName.startsWith("Microduino")) {
                    characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_MD_RX_TX);
                } else if (currDeviceName.startsWith("EtOH")) {
                    characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_ETOH_RX_TX);
                }
                characteristicRX = characteristicTX;

                if (characteristicTX != null) {
                    mBluetoothLeService.setCharacteristicNotification(characteristicTX, true);

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

    View.OnClickListener onClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == buttonSave) {
                Log.v(TAG, "buttonSave Clicked");
                dialogSave(currMatrix);
            } else if (v == buttonDelete) {
                Log.v(TAG, "buttonDel Clicked");
                if (currMatrix.getId() > 0) {
                    dialogDelete(currMatrix);
                } else {
                    toastMessage(getString(R.string.unsaved));
                }
            } else if (v == buttonSend) {
                updateMatrixInfo(currMatrix);

                UploadAsyncTask asyncTask = new UploadAsyncTask();
                asyncTask.execute();
            }
        }
    };

    View.OnClickListener onLedBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LedButton btn = (LedButton) v;
            int index = ledButtons.indexOf(btn);

            if (ledColors.get(index) == 0) {
                ledColors.set(index, currColorIndex);
                btn.setColorIndex(currColorIndex);
            } else {
                ledColors.set(index, 0);
                btn.setColorIndex(0);
            }

            if (drawMode) {
                sendLedColor(btn);
            }
        }
    };

    View.OnLongClickListener onLedBtnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            LedButton btn = (LedButton) v;
            int index = ledButtons.indexOf(btn);

            currColorIndex = (currColorIndex + 1) % 3 + 1;
            // Log.v(TAG, "currColorIndex = " + currColorIndex);
            ledColors.set(index, currColorIndex);
            btn.setColorIndex(currColorIndex);
            if (drawMode) {
                sendLedColor(btn);
            }
            return true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_matrix);

        activity = this;

        mtxDAO = new LedMatrixDAO(this);

        final Intent intent = getIntent();
        currDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        currDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        currMatrix = intent.getParcelableExtra(EXTRAS_LED_MATRIX);
        if (currMatrix == null) {
            currMatrix = mtxDAO.getDummyLedMatrix();
        }

        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout);
        String ledColorsStr = currMatrix.getMatrix();
        if (ledColorsStr == null) {
            ledColorsStr = LedMatrixDAO.MTX_BLANK;
        }

        assert (ledColorsStr.length() == MATRIX_NN);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int btnWidth = (displaymetrics.widthPixels - 50) / MATRIX_N - 1;
        Log.v(TAG, "btnWidth = " + btnWidth);

        char _c;
        int _ci;
        LedButton btn;
        for (int i = 0; i < MATRIX_NN; i++) {
            _c = ledColorsStr.charAt(i);
            _ci = Integer.valueOf("" + _c);

            ledColors.add(_ci);

            btn = new LedButton(this);
            btn.setColorIndex(_ci);
            ledButtons.add(btn);

            btn.setMinimumWidth(btnWidth);
            btn.setMinimumHeight(btnWidth);
            btn.setMaxWidth(btnWidth);
            btn.setMaxHeight(btnWidth);

            btn.setOnClickListener(onLedBtnClickListener);
            btn.setOnLongClickListener(onLedBtnLongClickListener);

            grid.addView(btn);
        }

        // Sets up UI references.
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        // is serial present?
        isSerial = (TextView) findViewById(R.id.isSerial);

        switchDraw = (Switch) findViewById(R.id.switchDraw);
        switchDraw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                updateMatrixInfo(currMatrix);
                if (isChecked) {
                    drawMode = true;
                    sendMessage("B:" + currMatrix.getMatrix() + "\n");
                } else {
                    drawMode = false;
                }
            }
        });

        returnText = (TextView) findViewById(R.id.textReturn);

        buttonSave = (Button) findViewById(R.id.saveButton);
        buttonDelete = (Button) findViewById(R.id.deleteButton);
        buttonSend = (Button) findViewById(R.id.sendButton);

        buttonSave.setOnClickListener(onClickListener);
        buttonDelete.setOnClickListener(onClickListener);
        buttonSend.setOnClickListener(onClickListener);

        infoButton = (ImageView) findViewById(R.id.infoImage);
        infoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                iascDialog();
            }
        });

        getActionBar().setTitle(getString(R.string.title_image) + ":" + currMatrix.getName());
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        toastMessage(getString(R.string.opt_info));
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(currDeviceAddress);
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
                mBluetoothLeService.connect(currDeviceAddress);
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
                characteristicReady = true;
                buttonSend.setEnabled(characteristicReady);
                switchDraw.setEnabled(characteristicReady);

                Toast.makeText(UploadMatrixActivity.this, getString(resourceId), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            Log.v(TAG, "BLE Return Data : " + data);
            returnText.setText(data);
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
        int msglen = msg.length();
        Log.v(TAG, "sendMsg msg= " + msg);

        if (characteristicReady && (mBluetoothLeService != null)
                && (characteristicTX != null) && (characteristicRX != null)) {
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);

            String tmp;
            for (int offset = 0; offset < msglen; offset += BLE_MSG_BUFFER_LEN) {
                tmp = msg.substring(offset, Math.min(offset + BLE_MSG_BUFFER_LEN, msglen));
                Log.v(TAG, "sendMsg tmp= " + tmp);

                characteristicTX.setValue(tmp);
                mBluetoothLeService.writeCharacteristic(characteristicTX);
                wait_ble(BLE_MSG_SEND_INTERVAL);
            }
        } else {
            Toast.makeText(UploadMatrixActivity.this, getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
        }
    }

    public void sendLedColor(LedButton btn) {
        int index = ledButtons.indexOf(btn);
        sendMessage("L:" + (index / MATRIX_N) + "," + (index % MATRIX_N) + "," + btn.getColorIndex() + "\n");
    }

    private void iascDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.iasc_dialog,
                (ViewGroup) findViewById(R.id.dialog));
        new AlertDialog.Builder(this).setView(layout)
                .setPositiveButton("OK", null).show();
    }

    public void toastMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    public void updateMatrixInfo(LedMatrixModel mtx) {
        mtx.setMatrix(bleColors2Str());
    }

    private String bleColors2Str() {
        StringBuilder sb = new StringBuilder("");

        for (int i = 0; i < MATRIX_NN; i++) {
            int _colorIndex = ledColors.get(i);
            sb.append("" + _colorIndex);
        }
        return sb.toString();
    }

    protected void dialogDelete(final LedMatrixModel mtx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.delete_confirm) + " " + mtx.getName() + "?");

        builder.setPositiveButton(getString(R.string.ok).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mtxDAO.deleteLedMatrix(mtx);
                dialog.dismiss();

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        builder.setNegativeButton(getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    protected void dialogSave(final LedMatrixModel mtx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.save_confirm));
        // builder.

        editNameInDialog = new EditText(this);
        editNameInDialog.setSingleLine(true);
        editNameInDialog.setFocusable(true);
        editNameInDialog.setSelectAllOnFocus(true);
        editNameInDialog.setText(mtx.getName());

        builder.setView(editNameInDialog);

        builder.setPositiveButton(getString(R.string.ok).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateMatrixInfo(mtx);
                mtx.setName(editNameInDialog.getText().toString());

                if (mtx.getId() > 0) {
                    mtxDAO.update(mtx);
                    Log.v(TAG, "Updated: " + " " + currMatrix.toString());
                } else {
                    long ret = mtxDAO.save(currMatrix);
                    mtx.setId((int) ret);
                    Log.v(TAG, "Saved: " + ret + " " + currMatrix.toString());
                }

                getActionBar().setTitle(getString(R.string.title_image) + ":" + currMatrix.getName());
            }
        });

        builder.setNegativeButton(getString(R.string.cancel).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public class UploadAsyncTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... params) {
            if (characteristicReady) {
                sendMessage("B:" + currMatrix.getMatrix() + "\n");
                wait_ble(BLE_MSG_SEND_INTERVAL);
            }
            return "Done";
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(UploadMatrixActivity.this, getString(R.string.done), Toast.LENGTH_SHORT).show();
        }
    }
}