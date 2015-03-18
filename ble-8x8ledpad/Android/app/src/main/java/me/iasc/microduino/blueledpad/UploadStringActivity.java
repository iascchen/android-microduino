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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import me.iasc.microduino.blueledpad.ble.BluetoothLeService;

public class UploadStringActivity extends AbstractBleControlActivity {
    private final static String TAG = UploadStringActivity.class.getSimpleName();

    public static int BLE_MSG_BUFFER_LEN = 8;
    private EditText editMsg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_string);

        // Sets up UI references.
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        // is serial present?
        isSerial = (TextView) findViewById(R.id.isSerial);

        editMsg = (EditText) findViewById(R.id.editMessage);

        buttonSend = (Button) findViewById(R.id.sendButton);
        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                msgBuffer = new StringBuilder("M:");
                msgBuffer.append(editMsg.getText()).append("\n");

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