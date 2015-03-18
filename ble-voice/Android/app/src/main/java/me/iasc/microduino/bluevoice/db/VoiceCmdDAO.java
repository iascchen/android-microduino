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

package me.iasc.microduino.bluevoice.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class VoiceCmdDAO extends BleDBDAO {
    private final static String TAG = VoiceCmdDAO.class.getSimpleName();

    private static final String WHERE_ID_EQUALS = DataBaseHelper.C_ID + " =?";

    public VoiceCmdDAO(Context context) {
        super(context);
    }

    public long save(VoiceCmdModel cmd) {
        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.C_CODE, cmd.getCode());
        values.put(DataBaseHelper.C_VOICE, cmd.getVoice());
        values.put(DataBaseHelper.C_NAME, cmd.getName());

        long result = database.insert(DataBaseHelper.VOICE_CMD_TABLE, null, values);
        Log.d("VoiceCmdDAO New Result:", "=" + result);
        return result;
    }

    public long update(VoiceCmdModel cmd) {
        ContentValues values = new ContentValues();

        values.put(DataBaseHelper.C_CODE, cmd.getCode());
        values.put(DataBaseHelper.C_VOICE, cmd.getVoice());
        values.put(DataBaseHelper.C_NAME, cmd.getName());

        long result = database.update(DataBaseHelper.VOICE_CMD_TABLE, values,
                WHERE_ID_EQUALS,
                new String[]{String.valueOf(cmd.getId())});
        Log.d("VoiceCmdDAO Update Result:", "=" + result);
        return result;
    }

    public int deleteVoiceCmd(VoiceCmdModel cmd) {
        return database.delete(DataBaseHelper.VOICE_CMD_TABLE,
                WHERE_ID_EQUALS, new String[]{cmd.getId() + ""});
    }

    public void deleteAll() {
        List<VoiceCmdModel> items = getVoiceCmds();
        for (VoiceCmdModel _i : items) {
            deleteVoiceCmd(_i);
        }
    }

    public List<VoiceCmdModel> getVoiceCmds() {
        List<VoiceCmdModel> cmds = new ArrayList<VoiceCmdModel>();

        Cursor cursor = database.query(DataBaseHelper.VOICE_CMD_TABLE,
                new String[]{DataBaseHelper.C_ID,
                        DataBaseHelper.C_CODE,
                        DataBaseHelper.C_VOICE,
                        DataBaseHelper.C_NAME}, null, null, null, null, DataBaseHelper.C_CODE + " ASC");

        while (cursor.moveToNext()) {
            int i = 0;

            VoiceCmdModel cmd = new VoiceCmdModel();

            int id = cursor.getInt(i++);
            // Log.d(TAG, "user.id = " + id);
            cmd.setId(id);

            cmd.setCode(cursor.getString(i++));
            cmd.setVoice(cursor.getString(i++));
            cmd.setName(cursor.getString(i++));

            Log.d(TAG, cmd.toString());

            cmds.add(cmd);
        }
        return cmds;
    }

    public VoiceCmdModel getDummyVoiceCmd() {
        VoiceCmdModel ret = new VoiceCmdModel();

        ret.setCode("ff");
        ret.setVoice("pin yin");
        ret.setName("名称");

        return ret;
    }

    static String[][] A0_CMDS = {
            {"a1", "kai men", "开门"},
            {"a2", "guan men", "关门"},

            {"b1", "kai deng", "开灯"},
            {"b2", "guan deng", "关灯"},

            {"c1", "se diao yi", "灯，暖色调"},
            {"c2", "se diao er", "灯，冷色调"},

            {"d1", "dan se yi", "灯，红"},
            {"d2", "dan se er", "灯，橙"},
            {"d3", "dan se san", "灯，黄"},
            {"d4", "dan se si", "灯，绿"},
            {"d5", "dan se wu", "灯，青"},
            {"d6", "dan se liu", "灯，蓝"},
            {"d7", "dan se qi", "灯，紫"},

            {"e1", "duo cai yi", "灯，多彩1"},
            {"e2", "duo cai er", "灯，多彩2"},

            {"f1", "mei ke", "美科"} //触发口令，用来唤醒语音识别模块
    };

    public void loadSamples() {
        VoiceCmdModel _cmd = null;
        for (int i = 0; i < A0_CMDS.length; i++) {
            _cmd = new VoiceCmdModel();
            _cmd.setCode(A0_CMDS[i][0]);
            _cmd.setVoice(A0_CMDS[i][1]);
            _cmd.setName(A0_CMDS[i][2]);

            save(_cmd);
        }
    }
}
