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

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * @author Alexey Kuzin (amkuzink@gmail.com).
 */
class AccelerometerListener implements SensorEventListener {

//    private static final String TAG = AccelerometerListener.class.getSimpleName();

    private volatile float mBoostLimit;
    private boolean mIsLimitExceed = false;
    private final AccelerometerEventQueue mEventQueue;

    AccelerometerListener(AccelerometerEventQueue handler, float boostLimit) {
        mBoostLimit = boostLimit;
        mEventQueue = handler;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) {
            return;
        }

        final float boostModule = calcDeviceBoost(event.values[0], event.values[1],
                event.values[2]);
//        Log.d(TAG, "Boost module = " + boostModule);
        if (boostModule > mBoostLimit) {
            if (!mIsLimitExceed) {
                mEventQueue.queueBoostLimitExceed(boostModule);
                mIsLimitExceed = true;
            }
        } else {
            mIsLimitExceed = false;
        }
    }

    void setBoostLimit(float boostLimit) {
        mBoostLimit = boostLimit;
    }

    private static float calcDeviceBoost(float xBoost, float yBoost, float zBoost) {
        return (float) Math.sqrt(xBoost * xBoost + yBoost * yBoost + zBoost * zBoost);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // This method is not used.
    }
}
