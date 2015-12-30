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

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import me.iasc.microduino.bluelight.ble.BluetoothLeService;
import me.iasc.microduino.bluelight.colorpicker.ColorPicker;
import me.iasc.microduino.bluelight.colorpicker.MultiColorPicker;

import java.util.Timer;
import java.util.TimerTask;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends AbstractBleControlActivity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static int COLOR_WHITE = Color.WHITE;
    public static int COLOR_SUN = Color.parseColor("#ffffdd6e");

    public static int HEARTBEAT_INTERVAL = 1000;

    public static final int MAX_DB = 100;

    private ActionBar actionbar;

    private MultiColorPicker multiColorPicker;
    private ColorPicker singleColorPicker;

    private Switch singleMultiColorSwitch;
    private TextView singleMultiColorTextView;
    private ToggleButton onButton;

    private FloatingActionsMenu floatingMenu;

    private RecordManager recorder;
    private boolean isRecording = false;
    private float[] colorHSV = new float[]{0f, 0f, 0f};

    private static Timer heartbeatTimer;

    Runnable mUpdateMicStatusTimer = new Runnable() {
        public void run() {
            double voice = recorder.updateMicStatus(this);
            Log.v(TAG, "voice：" + voice);
            makeChange(changeLightness(voice));

            wait_ble(BLE_MSG_SEND_INTERVAL * 2);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.gatt_services_characteristics);
        super.onCreate(savedInstanceState);

//        this.getView().setBackgroundResource(R.drawable.list_bg);
////        activity = this;

        actionbar = getActionBar();

        actionbar.setHomeAsUpIndicator(R.drawable.back);
        actionbar.setIcon(R.drawable.icon_white);

        actionbar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.ble_unconnect)));

        multiColorPicker = (MultiColorPicker) findViewById(R.id.multiColorPicker);
        singleColorPicker = (ColorPicker) findViewById(R.id.colorPicker);

        floatingMenu = (FloatingActionsMenu) findViewById(R.id.multiple_actions);

        singleMultiColorTextView = (TextView) findViewById(R.id.textSingleMulti);
        singleMultiColorSwitch = (Switch) findViewById(R.id.switchSingleMulti);

        singleMultiColorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                stopRecorder();

                if (isChecked) {
                    multiColorPicker.setVisibility(View.VISIBLE);
                    singleColorPicker.setVisibility(View.GONE);
                    floatingMenu.setVisibility(View.GONE);

                    singleMultiColorTextView.setText(getString(R.string.multi_colors));
                } else {
                    multiColorPicker.setVisibility(View.GONE);
                    singleColorPicker.setVisibility(View.VISIBLE);
                    floatingMenu.setVisibility(View.VISIBLE);

                    singleMultiColorTextView.setText(getString(R.string.single_color));
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
                stopRecorder();

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

        findViewById(R.id.buttonSun).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButton.setChecked(true);
                setSingleColor(COLOR_SUN);
            }
        });

        findViewById(R.id.buttonWhite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButton.setChecked(true);
                setSingleColor(COLOR_WHITE);
            }
        });

        /*
        //TODO
        findViewById(R.id.buttonPic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(activity, PicControlActivity.class);
                intent.putExtra(PicControlActivity.EXTRAS_DEVICE_NAME, currDeviceName);
                intent.putExtra(PicControlActivity.EXTRAS_DEVICE_ADDRESS, currDeviceAddress);
                startActivity(intent);
            }
        });
        */

        recorder = new RecordManager(mUpdateMicStatusTimer);
        findViewById(R.id.buttonMusic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    recorder.startRecord();
                    isRecording = true;
                    // isSerial.setText(R.string.listening);
                    singleMultiColorTextView.setText(R.string.listening);
                } else {
                    stopRecorder();

                    setSingleColor(singleColorPicker.getColor());
                }

                onButton.setChecked(true);

//                isRecording = !isRecording;

            }
        });

        singleMultiColorSwitch.setChecked(false);

        enableView(false);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(currDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    protected void onDestroy() {
        if (isRecording && recorder != null) {
            recorder.stopRecord();
        }

        stopTimer(heartbeatTimer);

        super.onDestroy();
    }

    protected void updateReadyState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                wait_ble(1000);

                characteristicReady = true;
                // isSerial.setText(getString(resourceId));
                actionbar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.ble_connected)));
                enableView(true);

                toastMessage(getString(resourceId));

                heartbeatTimer = startHeartbeatTimer(0, HEARTBEAT_INTERVAL);
            }
        });
    }

    protected void updateUnreadyState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                characteristicReady = false;
                // isSerial.setText(getString(resourceId));

                actionbar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.ble_unconnect)));
                enableView(false);

                toastMessage(getString(resourceId));
            }
        });
    }

    private void setSingleColor(int color) {
        singleMultiColorSwitch.setChecked(true);
        singleMultiColorSwitch.setChecked(false);
        singleColorPicker.setColor(color);

        makeChange(color);
    }

    private int changeLightness(double voice) {
        Color.colorToHSV(singleColorPicker.getColor(), colorHSV);
        colorHSV[2] = (float) Math.min(voice / MAX_DB, 1);

        return Color.HSVToColor(colorHSV);
    }

    private void enableView(boolean enable){
        onButton.setEnabled(enable);
        multiColorPicker.setEnabled(enable);
        singleMultiColorSwitch.setEnabled(enable);
        floatingMenu.setEnabled(enable);
    }

    private void stopRecorder() {
        if (isRecording && recorder != null) {
            recorder.stopRecord();
            isRecording = false;
            // isSerial.setText(getString(R.string.ready));
             singleMultiColorTextView.setText(R.string.single_color);
        }
    }

    // on change of single color
    private void makeChange(int color) {
        StringBuilder sb = new StringBuilder("C:");

        sb.append(Color.red(color)).append(",")
                .append(Color.green(color)).append(",")
                .append(Color.blue(color)).append(",")
                .append("-1\n");

        sendMessage(sb.toString());
    }

    private void makeChange(int[] colors) {
        for (int i = 0; i < colors.length; i++) {
            StringBuilder sb = new StringBuilder("C:");

            sb.append(Color.red(colors[i])).append(",")
                    .append(Color.green(colors[i])).append(",")
                    .append(Color.blue(colors[i])).append(",")
                    .append(i).append("\n");
            sendMessage(sb.toString());

            // add delay for ble massage transfer
            wait_ble(BLE_MSG_SEND_INTERVAL);
        }
    }

    private Timer startHeartbeatTimer(long delay, long period) {
        Timer mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(characteristicReady)
                    sendMessage(".\n");
            }
        }, delay, period);
        return mTimer;
    }

    private void stopTimer(Timer mTimer) {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
}