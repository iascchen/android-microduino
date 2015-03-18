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

package me.iasc.microduino.blueledpad.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class LedMatrixDAO extends BleDBDAO {
    private final static String TAG = LedMatrixDAO.class.getSimpleName();

    private static final String WHERE_ID_EQUALS = DataBaseHelper.C_ID + " =?";

    public LedMatrixDAO(Context context) {
        super(context);
    }

    public long save(LedMatrixModel matrix) {
        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.C_NAME, matrix.getName());
        values.put(DataBaseHelper.C_MATRIX, matrix.getMatrix());

        Bitmap preview = matrix.getPreview();
        if (preview != null) {
            values.put(DataBaseHelper.C_PREVIEW, ImgUtility.getBytes(matrix.getPreview()));
        }

        long result = database.insert(DataBaseHelper.LED_MATRIX_TABLE, null, values);
        Log.d(TAG, "New Result:" + result);
        return result;
    }

    public long update(LedMatrixModel matrix) {
        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.C_NAME, matrix.getName());
        values.put(DataBaseHelper.C_MATRIX, matrix.getMatrix());

        Bitmap preview = matrix.getPreview();
        if (preview != null) {
            values.put(DataBaseHelper.C_PREVIEW, ImgUtility.getBytes(matrix.getPreview()));
        }

        long result = database.update(DataBaseHelper.LED_MATRIX_TABLE, values,
                WHERE_ID_EQUALS,
                new String[]{String.valueOf(matrix.getId())});
        Log.d(TAG, "Update Result:" + result);
        return result;
    }

    public int deleteLedMatrix(LedMatrixModel matrix) {
        return database.delete(DataBaseHelper.LED_MATRIX_TABLE,
                WHERE_ID_EQUALS, new String[]{matrix.getId() + ""});
    }

    public void deleteAll() {
        List<LedMatrixModel> items = getLedMatrixes();
        for (LedMatrixModel _i : items) {
            deleteLedMatrix(_i);
        }
    }

    public List<LedMatrixModel> getLedMatrixes() {
        List<LedMatrixModel> cmds = new ArrayList<LedMatrixModel>();

        Cursor cursor = database.query(DataBaseHelper.LED_MATRIX_TABLE,
                new String[]{DataBaseHelper.C_ID,
                        DataBaseHelper.C_NAME,
                        DataBaseHelper.C_MATRIX,
                        DataBaseHelper.C_PREVIEW}, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int i = 0;

            LedMatrixModel mtx = new LedMatrixModel();

            int id = cursor.getInt(i++);
            mtx.setId(id);

            mtx.setName(cursor.getString(i++));
            mtx.setMatrix(cursor.getString(i++));

            byte[] blob = cursor.getBlob(i++);
            if (blob != null) {
                mtx.setPreview(ImgUtility.getPhoto(blob));
            }


            Log.d(TAG, mtx.toString());

            cmds.add(mtx);
        }
        return cmds;
    }

    public static final String MTX_BLANK = "0000000000000000000000000000000000000000000000000000000000000000";
    public static final String MTX_SMILE = "0011110001000010103003011000000110200201100220010100001000111100";
    public static final String MTX_SAD = "0033330003000030301001033000000330022003302002030300003000333300";
    public static final String MTX_HEART = "0000000001100110100110011000000110000001010000100010010000011000";
    public static final String MTX_PLANT = "0001100000011000000110000012210020133102111221110003300000122100";

    private static Bitmap IMG_NONE = null;

    public LedMatrixModel getDummyLedMatrix() {
        LedMatrixModel ret = new LedMatrixModel();

        ret.setName("空白");
        ret.setMatrix(MTX_BLANK);
        ret.setPreview(IMG_NONE);

        return ret;
    }

    static String[] matrix_namees = {"文字", "笑脸", "哭脸", "桃心", "飞机"};
    static Bitmap[] matrix_previews = {IMG_NONE, IMG_NONE, IMG_NONE, IMG_NONE, IMG_NONE};   //TODO, add Preview image
    static String[] matrix_matrixes = {null, MTX_SMILE, MTX_SAD, MTX_HEART, MTX_PLANT};

    public void loadSamples() {
        LedMatrixModel _mtx;
        for (int i = 0; i < matrix_namees.length; i++) {
            _mtx = new LedMatrixModel();
            _mtx.setName(matrix_namees[i]);
            _mtx.setMatrix(matrix_matrixes[i]);

            if (matrix_previews[i] != null) {
                _mtx.setPreview(matrix_previews[i]);
            }

            save(_mtx);
        }
    }
}
