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

package org.leveafan941.boostdetection.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.leveafan941.boostdetection.AccelConstants;
import org.leveafan941.boostdetection.R;
import org.leveafan941.boostdetection.accelerometer.AccelerometerManager;
import org.leveafan941.boostdetection.notification.AccelNotifFacade;
import org.leveafan941.boostdetection.notification.AccelfNotifications;

/**
 * @author Alexey Kuzin (amkuzink@gmail.com).
 */
public class AccelerometerService extends Service {

    private static final String TAG = AccelerometerService.class.getSimpleName();

    private AccelerometerManager mAccelMgr;
    private AccelNotifFacade mNotifMgr;

    private AccelerometerLimitListener mBoostLimitListener;

    private AccelerometerBinder mBinder;


    private class AccelerometerLimitListener implements AccelerometerManager.BoostLimitListener {

        private int mLimitExceedNumber = 0;

        @Override
        public void onBoostLimitExceed(float value) {
            Toast.makeText(AccelerometerService.this,
                    getString(R.string.boost_limit_exceed_message, value),
                    Toast.LENGTH_SHORT).show();

            mNotifMgr.showBoostLimitExceedNotification(++mLimitExceedNumber);
            mNotifMgr.playBoostLimitExceedAudioNotification();
        }

        void resetCounter() {
            mLimitExceedNumber = 0;
        }
    }

    private class AccelerometerBinder extends Binder implements AccelServiceBinder {

        @Override
        public void setBoostLimit(float boostLimit) {
            mAccelMgr.setBoostLimit(boostLimit > AccelConstants.MINIMUM_BOOST_VALUE
                    ? boostLimit : AccelConstants.MINIMUM_BOOST_VALUE);
        }
    }


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        try {
            mAccelMgr = new AccelerometerManager(this);
        } catch (AccelerometerManager.NoAccelerometerHardwareException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            stopSelf();
        }

        mBoostLimitListener = new AccelerometerLimitListener();
        mNotifMgr = new AccelNotifFacade(this);
        mBinder = new AccelerometerBinder();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        if (mAccelMgr != null) {
            mAccelMgr.stop();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");

        return mAccelMgr == null ? null : mBinder;
    }

    private void handleIntent(Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "handleIntent: " + action);

        if (AccelServiceIntents.START_ACCELEROMETER_SERVICE_ACTION.equals(action)) {
            startAccelerationTracking(intent.getFloatExtra(AccelServiceIntents.BOOST_LIMIT_EXTRA,
                    AccelConstants.DEFAULT_BOOST_VALUE));
        } else if (AccelfNotifications.EXCEED_NOTIF_REMOVE_ACTION.equals(action)) {
            handleNotificationRemoved();
        }
    }

    private void startAccelerationTracking(float boostLimit) {
        mAccelMgr.start(mBoostLimitListener, boostLimit);
    }

    private void handleNotificationRemoved() {
        mBoostLimitListener.resetCounter();
    }
}
