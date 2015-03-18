/**
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
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import me.iasc.microduino.blueledpad.db.LedMatrixDAO;
import me.iasc.microduino.blueledpad.db.LedMatrixModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for query Led Matrix records.
 */
public class LedMatrixActivity extends ListActivity {
    private static final String TAG = LedMatrixActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private LedMatrixListAdapter mListAdapter;

    public static LedMatrixDAO mtxDAO = null;
    private List<LedMatrixModel> mtxList;

    private static final int REQUEST_ADD = 1;
    private static final int REQUEST_UPDATE = 2;
    private static final int REQUEST_STRING = 3;

    private String currDeviceName, currDeviceAddress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mtxDAO = new LedMatrixDAO(this);

        final Intent intent = getIntent();
        currDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        currDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.matrixes_menu, menu);

        menu.findItem(R.id.menu_add).setVisible(true);
        menu.findItem(R.id.menu_resetdb).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                final Intent intent = new Intent(this, UploadMatrixActivity.class);
                intent.putExtra(UploadMatrixActivity.EXTRAS_DEVICE_NAME, currDeviceName);
                intent.putExtra(UploadMatrixActivity.EXTRAS_DEVICE_ADDRESS, currDeviceAddress);
                startActivityForResult(intent, REQUEST_ADD);
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
        mListAdapter = new LedMatrixListAdapter();
        setListAdapter(mListAdapter);

        queryMatrixes();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ADD && resultCode == Activity.RESULT_OK) {
            Log.v(TAG, "Add New Object");
            queryMatrixes();
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
        if (position == 0) {
            // for upload text activity
            final Intent intent = new Intent(this, UploadStringActivity.class);
            intent.putExtra(UploadStringActivity.EXTRAS_DEVICE_NAME, currDeviceName);
            intent.putExtra(UploadStringActivity.EXTRAS_DEVICE_ADDRESS, currDeviceAddress);
            startActivityForResult(intent, REQUEST_STRING);
        } else {
            // for upload image activity
            final LedMatrixModel mtx = mListAdapter.getObject(position);
            if (mtx == null) return;

            final Intent intent = new Intent(this, UploadMatrixActivity.class);
            intent.putExtra(UploadMatrixActivity.EXTRAS_DEVICE_NAME, currDeviceName);
            intent.putExtra(UploadMatrixActivity.EXTRAS_DEVICE_ADDRESS, currDeviceAddress);
            intent.putExtra(UploadMatrixActivity.EXTRAS_LED_MATRIX, mtx);
            startActivityForResult(intent, REQUEST_UPDATE);
        }
    }

    private void queryMatrixes() {
        mListAdapter.clear();

        mtxList = mtxDAO.getLedMatrixes();
        if (mtxList.size() <= 0) {
            mtxDAO.loadSamples();
            mtxList = mtxDAO.getLedMatrixes();
        }

        getActionBar().setTitle(currDeviceName + ":" + getString(R.string.led_matrix) + " (" + mtxList.size() + ")");

        for (LedMatrixModel _c : mtxList) {
            mListAdapter.addObject(_c);
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
                mtxDAO.deleteAll();
                queryMatrixes();

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
    private class LedMatrixListAdapter extends BaseAdapter {
        private ArrayList<LedMatrixModel> objlist;
        private LayoutInflater mInflator;

        public LedMatrixListAdapter() {
            super();
            objlist = new ArrayList<LedMatrixModel>();
            mInflator = LedMatrixActivity.this.getLayoutInflater();
        }

        public void addObject(LedMatrixModel obj) {
            if (!objlist.contains(obj)) {
                objlist.add(obj);
            }
        }

        public LedMatrixModel getObject(int position) {
            return objlist.get(position);
        }

        public void clear() {
            objlist.clear();
        }

        @Override
        public int getCount() {
            return objlist.size();
        }

        @Override
        public Object getItem(int i) {
            return objlist.get(i);
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
                view = mInflator.inflate(R.layout.list_led_matrix_item, null);

                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) view.findViewById(R.id.name);
                viewHolder.preview = (ImageView) view.findViewById(R.id.preview);

                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            LedMatrixModel mtx = objlist.get(i);
            viewHolder.name.setText(mtx.getName());
            viewHolder.preview.setImageBitmap(mtx.getPreview());

            return view;
        }
    }

    static class ViewHolder {
        TextView name;
        ImageView preview;
    }
}