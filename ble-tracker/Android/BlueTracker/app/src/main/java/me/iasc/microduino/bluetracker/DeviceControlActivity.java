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

package me.iasc.microduino.bluetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.*;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.*;
import android.widget.*;
import me.iasc.microduino.bluetracker.ble.BluetoothLeService;

import java.text.SimpleDateFormat;

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

    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yy,MM,dd,HH,mm,ss");

    public static int BLE_MSG_SEND_INTERVAL = 100;

    private Button sendButton;
    private EditText msgEdit;
    private TextView isSerial, mConnectionState, returnText;
    private ImageView infoButton;

    private SurfaceView mSurfaceView = null;
    private SurfaceHolder mSurfaceHolder = null;
    private Paint mPaint = null;

    private String mDeviceName, mDeviceAddress;
    private float acceValusW = 0f, acceValusX = 0f, acceValusY = 0f, acceValusZ = 0f;
    private int x = 0;

    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic characteristicTX, characteristicRX;
    private boolean mConnected = false, characteristicReady = false;

    StringBuilder sbuffer = new StringBuilder();

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
                characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
                //characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
                characteristicRX = characteristicTX;

                if ((gattService != null) && (characteristicTX != null)) {
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

        msgEdit = (EditText) findViewById(R.id.editText);
        returnText = (TextView) findViewById(R.id.returnText);

        sendButton = (Button) findViewById(R.id.sendButton);
//        timeButton = (Button) findViewById(R.id.timeButton);

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (characteristicReady) {
                    StringBuffer sb = new StringBuffer("m");
                    sb.append(msgEdit.getText()).append("\n");

                    sendMessage(sb.toString());
                }
            }
        });

//        timeButton.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if (characteristicReady) {
//                    StringBuffer sb = new StringBuffer("t");
//                    Date now = new Date();
//                    sb.append(DATE_FORMAT.format(now)).append("\n");
//
//                    sendMessage(sb.toString());
//                }
//            }
//        });

        infoButton = (ImageView) findViewById(R.id.infoImage);
        infoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                iascDialog();
            }
        });

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new MyHolder());

        mPaint = new Paint();
        //画笔的粗细
        mPaint.setStrokeWidth(5.0f);

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
                wait_ble(1000);

                characteristicReady = true;

//                timeButton.setEnabled(characteristicReady);
                sendButton.setEnabled(characteristicReady);

                Toast.makeText(DeviceControlActivity.this, getString(resourceId), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            // Log.v(TAG, "BLE Return Data : " + data);

            sbuffer.append(data);
            if (data.endsWith("\n")) {
                String tmp = sbuffer.toString();
                if (tmp.startsWith("q")) {
                    returnText.setText(tmp.substring(0, tmp.length()-1));
                    onSensorChanged(tmp.substring(2));
                }
                sbuffer = new StringBuilder();
            }
        }
    }

    int BASE = 60;

    public void onSensorChanged(String sensorStr) {
        Log.v(TAG, "onSensorChanged : " + sensorStr);

        String[] fs = sensorStr.split(",");
        if (fs.length == 4) {
            acceValusW = Float.parseFloat(fs[0]) * (BASE - 10);
            //获得x轴的值
            acceValusX = Float.parseFloat(fs[1]) * (BASE - 10);
            //获得y轴的值
            acceValusY = Float.parseFloat(fs[2]) * (BASE - 10);
            //获得z轴的值
            acceValusZ = Float.parseFloat(fs[3]) * (BASE - 10);
            //锁定整个SurfaceView
            Canvas mCanvas = mSurfaceHolder.lockCanvas();
            try {
                if (mCanvas != null) {
                    //画笔的颜色(红)
                    mPaint.setColor(Color.RED);
                    //画X轴的点
                    mCanvas.drawPoint(x, (int) (BASE + acceValusX), mPaint);
                    //画笔的颜色(绿)
                    mPaint.setColor(Color.GREEN);
                    //画Y轴的点
                    mCanvas.drawPoint(x, (int) (BASE*2 + acceValusY), mPaint);
                    //画笔的颜色(蓝)
                    mPaint.setColor(Color.CYAN);
                    //画Z轴的点
                    mCanvas.drawPoint(x, (int) (BASE*3 + acceValusZ), mPaint);
                    //画笔的颜色(huang)
                    mPaint.setColor(Color.WHITE);
                    //画W轴的点
                    mCanvas.drawPoint(x, (int) (BASE*4 + acceValusW), mPaint);
                    //横坐标+1

                    x++;
                    //如果已经画到了屏幕的最右边
                    // if (x > getWindowManager().getDefaultDisplay().getWidth()) {
                    if (x > mCanvas.getWidth()) {
                        x = 0;
                        //清屏
                        mCanvas.drawColor(Color.BLACK);
                    }
                    //绘制完成，提交修改
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mCanvas != null) {
                    //重新锁一次
                    mSurfaceHolder.lockCanvas(new Rect(0, 0, 0, 0));
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            }
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

    //定义一个类，实现Callback接口
    public class MyHolder implements SurfaceHolder.Callback {

        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            // TODO Auto-generated method stub
            //add your code
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            //add your code
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            //add your code
        }

    }
}