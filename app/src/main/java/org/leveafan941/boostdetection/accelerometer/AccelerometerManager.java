/*
 * Copyright (c) 2016 Alexey Kuzin <amkuzink@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.leveafan941.boostdetection.accelerometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import org.leveafan941.boostdetection.R;

/**
 * @author Alexey Kuzin (amkuzink@gmail.com).
 */
public final class AccelerometerManager {

    private static final String TAG = AccelerometerManager.class.getSimpleName();

    private final SensorManager mSensorMgr;
    private final Sensor mSensor;
    private final Handler.Callback mSensorHandlerCallback = new BoostLimitHandlerCallback();

    private AccelerometerListener mAccelerometerListener;
    private BoostLimitListener mBoostLimitListener;
    private Handler mSensorChangedHandler;

    public interface BoostLimitListener {
        void onBoostLimitExceed(float value);
    }

    public class NoAccelerometerHardwareException extends RuntimeException {
        NoAccelerometerHardwareException(String message) {
            super(message);
        }
    }

    class BoostLimitHandlerCallback implements Handler.Callback {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == AccelerometerHandler.BOOST_LIMIT_MSG_ID) {
                mBoostLimitListener.onBoostLimitExceed((Float) msg.obj);
                return true;
            }

            return false;
        }
    }

    public AccelerometerManager(Context context) {
        mSensorMgr = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        mSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (mSensor == null) {
            throw new NoAccelerometerHardwareException(
                    context.getString(R.string.no_accelerometer_exception));
        }
        Log.d(TAG, "Sensor name = " + mSensor.getName());
        Log.d(TAG, "Resolution = " + mSensor.getResolution() + ", max range = "
                + mSensor.getMaximumRange());
    }

    public void start(BoostLimitListener limitListener, float boostLimit) {
        Log.d(TAG, "Start");

        mBoostLimitListener = limitListener;

        HandlerThread handlerThread = new HandlerThread("SensorChangedThread");
        handlerThread.start();
        mSensorChangedHandler = new Handler(handlerThread.getLooper());

        mAccelerometerListener = new AccelerometerListener(
                new AccelerometerHandler(new Handler(mSensorHandlerCallback)),
                boostLimit);

        mSensorMgr.registerListener(mAccelerometerListener, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL, mSensorChangedHandler);
    }

    public void setBoostLimit(float boostLimit) {
        Log.d(TAG, "Set boost limit to " + boostLimit);

        if (mAccelerometerListener != null) {
            mAccelerometerListener.setBoostLimit(boostLimit);
        }
    }

    public void stop() {
        Log.d(TAG, "Stop");

        if (mSensorChangedHandler != null) {
            mSensorChangedHandler.getLooper().quit();
        }

        mSensorMgr.unregisterListener(mAccelerometerListener);
    }
}
