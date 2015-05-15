/**
 * Copyright (C) 2015 Iasc CHEN
 * Created on 15/3/19.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.iasc.microduino.ble;

import android.os.AsyncTask;
import android.util.Log;

public class BleAsyncTask extends AsyncTask<String, Integer, String> {
    private final static String TAG = BleAsyncTask.class.getSimpleName();

    private final int WAIT_INTERVAL = 100;

    protected int getInterval() {
        return WAIT_INTERVAL;
    }

    public void waitIdle() {
        try {
            Thread.sleep(getInterval());
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    protected String doInBackground(String... params) {
        Log.i(TAG, "BleAsyncTask doInBackground call :" + params);

        // TODO: Do Something
        // waitIdle();

        return "Done";
    }

    @Override
    protected void onProgressUpdate(Integer... progresses) {
        // Log.i(TAG, "BleAsyncTask onProgressUpdate called :" + progresses);
    }

    @Override
    protected void onPostExecute(String result) {
        // Log.v(TAG, "BleAsyncTask onPostExecute called :" + result);
    }
}