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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import me.iasc.microduino.blueledpad.ble.BluetoothLeService;

public class UploadStringActivity extends AbstractBleControlActivity {
    private final static String TAG = UploadStringActivity.class.getSimpleName();

    public static int BLE_MSG_BUFFER_LEN = 8;
    private EditText editMsg;

    private RadioGroup rgColor, rgDirection;
    private RadioButton rButtonRed, rButtonYellow, rButtonGreen, rButtonLeft, rButtonUp, rButtonRight, rButtonDown;

    public static int COLOR_RED = 1, COLOR_YELLOW = 2, COLOR_GREEN = 3;
    public static int DIRECTION_LEFT = 0, DIRECTION_UP = 1, DIRECTION_RIGHT = 2, DIRECTION_DOWN = 3;

    int colorIndex = 0, directionIndex = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_upload_string);

        super.onCreate(savedInstanceState);

        // Sets up UI references.
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        // is serial present?
        isSerial = (TextView) findViewById(R.id.isSerial);

        editMsg = (EditText) findViewById(R.id.editMessage);

        rgColor = (RadioGroup) findViewById(R.id.radioGroupColor);
        rButtonRed = (RadioButton) rgColor.findViewById(R.id.rButtonRed);
        rButtonYellow = (RadioButton) rgColor.findViewById(R.id.rButtonYellow);
        rButtonGreen = (RadioButton) rgColor.findViewById(R.id.rButtonGreen);

        rgDirection = (RadioGroup) findViewById(R.id.radioGroupDirection);
        rButtonLeft = (RadioButton) rgDirection.findViewById(R.id.rButtonLeft);
        rButtonUp = (RadioButton) rgDirection.findViewById(R.id.rButtonUp);
        rButtonRight = (RadioButton) rgDirection.findViewById(R.id.rButtonRight);
        rButtonDown = (RadioButton) rgDirection.findViewById(R.id.rButtonDown);

        rgColor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup rg, int arg1) {
                int radioButtonId = rg.getCheckedRadioButtonId();
                RadioButton rb = (RadioButton) rg.findViewById(radioButtonId);

                if (rButtonYellow == rb) {
                    colorIndex = COLOR_YELLOW;
                } else if (rButtonGreen == rb) {
                    colorIndex = COLOR_GREEN;
                } else {
                    colorIndex = COLOR_RED;
                }
            }
        });

        rgDirection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup rg, int arg1) {
                int radioButtonId = rg.getCheckedRadioButtonId();
                RadioButton rb = (RadioButton) rg.findViewById(radioButtonId);

                if (rButtonUp == rb) {
                    directionIndex = DIRECTION_UP;
                } else if (rButtonRight == rb) {
                    directionIndex = DIRECTION_RIGHT;
                } else if (rButtonDown == rb) {
                    directionIndex = DIRECTION_DOWN;
                } else {
                    directionIndex = DIRECTION_LEFT;
                }
            }
        });

        buttonSend = (Button) findViewById(R.id.sendButton);
        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                msgBuffer = new StringBuilder("M:");
                msgBuffer.append(colorIndex).append(",");
                msgBuffer.append(directionIndex).append(",");
                msgBuffer.append(editMsg.getText()).append("\n");

                Log.v(TAG, "message = " + msgBuffer.toString());

                UploadAsyncTask asyncTask = new UploadAsyncTask();
                asyncTask.execute();
            }
        });

        infoButton = (ImageView) findViewById(R.id.infoImage);
        infoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                iascDialog();
            }
        });

        getActionBar().setTitle(getString(R.string.title_text));
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    protected int getMsgBufferLen() {
        return BLE_MSG_BUFFER_LEN;
    }
}