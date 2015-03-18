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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.*;
import me.iasc.microduino.blueledpad.ble.BluetoothLeService;
import me.iasc.microduino.blueledpad.db.LedMatrixDAO;
import me.iasc.microduino.blueledpad.db.LedMatrixModel;

import java.util.ArrayList;
import java.util.List;

public class UploadMatrixActivity extends AbstractBleControlActivity {
    private final static String TAG = UploadMatrixActivity.class.getSimpleName();

    public static final String EXTRAS_LED_MATRIX = "LED_MATRIX";

    public static final int MATRIX_N = 8;
    public static final int MATRIX_NN = MATRIX_N * MATRIX_N;

    LedMatrixModel currMatrix;
    LedMatrixDAO mtxDAO = null;

    Activity activity;
    List<LedButton> ledButtons = new ArrayList<LedButton>();
    List<Integer> ledColors = new ArrayList<Integer>();

    int currColorIndex = 1;
    boolean drawMode = false;

    private Button buttonSave, buttonDelete;
    private EditText editNameInDialog;
    private Switch switchDraw;

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

                msgBuffer = new StringBuilder("B:");
                msgBuffer.append(currMatrix.getMatrix()).append("\n");

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

    protected void updateReadyState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                characteristicReady = true;
                buttonSend.setEnabled(characteristicReady);
                switchDraw.setEnabled(characteristicReady);

                toastMessage(getString(resourceId));
            }
        });
    }

    public void sendLedColor(LedButton btn) {
        int index = ledButtons.indexOf(btn);
        sendMessage("L:" + (index / MATRIX_N) + "," + (index % MATRIX_N) + "," + btn.getColorIndex() + "\n");
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
}