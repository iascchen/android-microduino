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

package me.iasc.microduino.bluevoice;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import me.iasc.microduino.bluevoice.db.VoiceCmdDAO;
import me.iasc.microduino.bluevoice.db.VoiceCmdModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for scanning and displaying available Bluetooth LE devices_menu.
 */
public class VoiceCmdsActivity extends ListActivity {
    private static final String TAG = VoiceCmdsActivity.class.getSimpleName();

    private VoiceCmdListAdapter mListAdapter;

    public static VoiceCmdDAO cmdDAO = null;
    private List<VoiceCmdModel> cmdlist;

    private static final int REQUEST_ADD_CMD = 1;
    private static final int REQUEST_UPDATE_CMD = 2;
    private static final int REQUEST_SHOW_DEVICES = 3;
    private static final int REQUEST_UPLOAD_CMDS = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cmdDAO = new VoiceCmdDAO(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.commands_menu, menu);

        menu.findItem(R.id.menu_add).setVisible(true);
        menu.findItem(R.id.menu_resetdb).setVisible(true);
        menu.findItem(R.id.menu_upload).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                final Intent intent = new Intent(this, VoiceCmdDetailActivity.class);
                startActivityForResult(intent, REQUEST_ADD_CMD);
                break;
            case R.id.menu_upload:
                final Intent intent2 = new Intent(this, DeviceScanActivity.class);
                startActivityForResult(intent2, REQUEST_SHOW_DEVICES);
                break;
            case R.id.menu_resetdb:
                dialog();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initializes list view adapter.
        mListAdapter = new VoiceCmdListAdapter();
        setListAdapter(mListAdapter);

        queryCommands();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ADD_CMD && resultCode == Activity.RESULT_OK) {
            Log.v(TAG, "Add New CMD");
            queryCommands();
            return;
        } else if (requestCode == REQUEST_SHOW_DEVICES && resultCode == Activity.RESULT_OK) {
            Log.v(TAG, "Selected Device");

            String deviceName = data.getStringExtra(VoiceCmdUploadActivity.EXTRAS_DEVICE_NAME);
            String deviceAddress = data.getStringExtra(VoiceCmdUploadActivity.EXTRAS_DEVICE_ADDRESS);

            final Intent intent2 = new Intent(this, VoiceCmdUploadActivity.class);
            intent2.putExtra(VoiceCmdUploadActivity.EXTRAS_DEVICE_NAME, deviceName);
            intent2.putExtra(VoiceCmdUploadActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress);

            startActivityForResult(intent2, REQUEST_UPLOAD_CMDS);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final VoiceCmdModel cmd = mListAdapter.getDevice(position);
        if (cmd == null) return;

        final Intent intent = new Intent(this, VoiceCmdDetailActivity.class);
        intent.putExtra(VoiceCmdDetailActivity.EXTRAS_COMMAND, cmd);
        startActivityForResult(intent, REQUEST_UPDATE_CMD);
    }

    private void queryCommands() {
        mListAdapter.clear();

        cmdlist = cmdDAO.getVoiceCmds();
        if (cmdlist.size() <= 0) {
            cmdDAO.loadSamples();
            cmdlist = cmdDAO.getVoiceCmds();
        }

        getActionBar().setTitle(getString(R.string.command) + " (" + cmdlist.size() + ")");

        for (VoiceCmdModel _c : cmdlist) {
            mListAdapter.addCmd(_c);
        }
        mListAdapter.notifyDataSetChanged();

        invalidateOptionsMenu();
    }

    protected void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.resetdb_confirm));

        builder.setPositiveButton(getString(R.string.ok).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cmdDAO.deleteAll();
                queryCommands();
                dialog.dismiss();
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

    // Adapter for holding devices_menu found through scanning.
    private class VoiceCmdListAdapter extends BaseAdapter {
        private ArrayList<VoiceCmdModel> cmds;
        private LayoutInflater mInflator;

        public VoiceCmdListAdapter() {
            super();
            cmds = new ArrayList<VoiceCmdModel>();
            mInflator = VoiceCmdsActivity.this.getLayoutInflater();
        }

        public void addCmd(VoiceCmdModel device) {
            if (!cmds.contains(device)) {
                cmds.add(device);
            }
        }

        public VoiceCmdModel getDevice(int position) {
            return cmds.get(position);
        }

        public void clear() {
            cmds.clear();
        }

        @Override
        public int getCount() {
            return cmds.size();
        }

        @Override
        public Object getItem(int i) {
            return cmds.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.list_voice_cmd_item, null);

                viewHolder = new ViewHolder();
                viewHolder.code = (TextView) view.findViewById(R.id.cmd_code);
                viewHolder.name = (TextView) view.findViewById(R.id.cmd_name);

                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            VoiceCmdModel cmd = cmds.get(i);
            viewHolder.code.setText(cmd.getVoice());
            viewHolder.name.setText(cmd.getName());

            return view;
        }
    }

    static class ViewHolder {
        TextView code;
        TextView name;
    }
}