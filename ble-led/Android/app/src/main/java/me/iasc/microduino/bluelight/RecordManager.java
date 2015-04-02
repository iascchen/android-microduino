package me.iasc.microduino.bluelight;

/**
 * Copyright (C) 2015 Iasc CHEN
 * Created on 15/3/31.
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

import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.io.File;

/**
 * amr音频处理
 *
 * @author hongfa.yy
 * @version 创建时间2012-11-21 下午4:33:28
 */
public class RecordManager {
    private final static String TAG = RecordManager.class.getSimpleName();

    private MediaRecorder mMediaRecorder;
    private String filePath;

    private Runnable runabler;

    private long startTime;
    private long endTime;

    private final Handler mHandler = new Handler();

    public RecordManager(Runnable runabler) {
        this.filePath = "/dev/null";
        this.runabler = runabler;
    }

    public RecordManager(Runnable runabler, File file) {
        this.filePath = file.getAbsolutePath();
        this.runabler = runabler;
    }

    /**
     * 开始录音 使用amr格式
     */
    public void startRecord() {
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();

        try {
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.setOutputFile(filePath);

            mMediaRecorder.prepare();
            mMediaRecorder.start();

            startTime = System.currentTimeMillis();

            updateMicStatus(runabler);

            Log.v(TAG, "Action StartTime " + startTime);
        } catch (Exception e) {
            Log.v(TAG, "call startAmr(File mRecAudioFile) failed!" + e.getMessage());
        }
    }

    /**
     * 停止录音
     */
    public long stopRecord() {
        if (mMediaRecorder == null)
            return 0L;

        endTime = System.currentTimeMillis();

        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;

        Log.v(TAG, "Action StopTime" + (endTime - startTime));
        return endTime - startTime;
    }

    /**
     * 更新话筒状态 分贝是也就是相对响度 分贝的计算公式K=20lg(Vo/Vi) Vo当前振幅值 Vi基准值为600：
     * 我是怎么制定基准值的呢？ 当20 * Math.log10(mMediaRecorder.getMaxAmplitude() / Vi) == 0的时候
     * vi就是我所需要的基准值
     * 当我不对着麦克风说任何话的时候，测试获得的mMediaRecorder.getMaxAmplitude()值即为基准值。
     * Log.i("mic_", "麦克风的基准值：" + mMediaRecorder.getMaxAmplitude());前提时不对麦克风说任何话
     */
    private int BASE = 600;
    private int SAMPLE_INTERVAL = 200;// 间隔取样时间

    public double updateMicStatus(Runnable timerRun) {
        double ret = 0;

        if (mMediaRecorder != null) {
            double maxAmp = mMediaRecorder.getMaxAmplitude();
            Log.v(TAG, "maxAmp：" + maxAmp);

            double ratio = maxAmp / BASE;
            double db = 0;  // DB
            if (ratio > 1)
                db = 20 * Math.log10(ratio);

            Log.v(TAG, "db：" + db);
            ret = db;

            mHandler.postDelayed(timerRun, SAMPLE_INTERVAL);
        }

        return ret;
    }
}
