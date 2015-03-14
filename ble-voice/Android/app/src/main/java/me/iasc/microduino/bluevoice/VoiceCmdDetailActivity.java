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

package me.iasc.microduino.bluevoice;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import me.iasc.microduino.bluevoice.db.VoiceCmdDAO;
import me.iasc.microduino.bluevoice.db.VoiceCmdModel;

public class VoiceCmdDetailActivity extends Activity {
    private static final String TAG = VoiceCmdDetailActivity.class.getSimpleName();

    public static final String EXTRAS_COMMAND = "command";

    public static final int COMMAND_MAX_LEN = 18;
    public static final int MSG_MAX_LEN = 18 + 9; // voice 18 + other chars 9

    VoiceCmdModel cmd;
    VoiceCmdDAO cmdDAO = null;

    EditText editName, editCode, editVoice;
    Button buttonSave, buttonDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_cmd_detail);

        cmdDAO = new VoiceCmdDAO(this);

        final Intent intent = getIntent();
        cmd = intent.getParcelableExtra(EXTRAS_COMMAND);

        editName = (EditText) findViewById(R.id.editName);
        editCode = (EditText) findViewById(R.id.editCode);
        editVoice = (EditText) findViewById(R.id.editVoice);

        if (cmd == null) {
            cmd = cmdDAO.getDummyVoiceCmd();
            editCode.setEnabled(true);
        }

        buttonSave = (Button) findViewById(R.id.buttonSave);
        buttonDelete = (Button) findViewById(R.id.buttonDelete);

        buttonSave.setOnClickListener(onClickListener);
        buttonDelete.setOnClickListener(onClickListener);

        getActionBar().setTitle(cmd.getName());
        getActionBar().setDisplayHomeAsUpEnabled(true);

        showCmdInfo(cmd);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (v == buttonSave) {
                Log.v(TAG, "buttonSave Clicked");

                updateCmdInfo(cmd);

                if (cmd.toString().length() > MSG_MAX_LEN) {
                    toastMessage(getString(R.string.cmd_too_long) + " " + COMMAND_MAX_LEN);
                    return;
                }

                if (cmd.getId() > 0) {
                    try {
                        cmdDAO.update(cmd);
                        Log.v(TAG, "Updated: " + " " + cmd.toString());
                    } catch (Exception e) {
                        toastMessage(getString(R.string.duplicated_voice));
                        return;
                    }
                } else {
                    long ret = cmdDAO.save(cmd);
                    if (ret == -1) {
                        toastMessage(getString(R.string.duplicated));
                        return;
                    }
                    cmd.setId((int) ret);
                    Log.v(TAG, "Saved: " + ret + " " + cmd.toString());
                }

                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();

            } else if (v == buttonDelete) {
                Log.v(TAG, "buttonDel Clicked");
                if (cmd.getId() > 0) {
                    dialog(cmd);
                } else {
                    toastMessage(getString(R.string.unsaved));
                }
            }
        }
    };

    public void showCmdInfo(VoiceCmdModel cmd) {
        Log.d(TAG, cmd.toString());

        editName.setText(cmd.getName());
        editCode.setText(cmd.getCode());
        editVoice.setText(cmd.getVoice());

        editName.setFocusable(true);
        editName.selectAll();
    }

    public void toastMessage(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    public void updateCmdInfo(VoiceCmdModel cmd) {
        cmd.setName(editName.getText().toString());
        cmd.setCode(editCode.getText().toString());
        cmd.setVoice(editVoice.getText().toString());

        Log.d(TAG, cmd.toString());
    }

    protected void dialog(final VoiceCmdModel cmd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.delete_confirm) + " " + cmd.getName() + "?");

        builder.setPositiveButton(getString(R.string.ok).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cmdDAO.deleteVoiceCmd(cmd);
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
}
