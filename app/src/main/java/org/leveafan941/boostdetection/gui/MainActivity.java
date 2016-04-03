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

package org.leveafan941.boostdetection.gui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.leveafan941.boostdetection.R;
import org.leveafan941.boostdetection.accelerometer.AccelerometerManager;
import org.leveafan941.boostdetection.notification.AccelNotifFacade;

/**
 * @author Alexey Kuzin (amkuzink@gmail.com).
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String GLOBAL_SHARED_PREFS_NAME = "BOOST_MAIN_PREFS";
    private static final String BOOST_PREF_NAME = "boost_preference";

    private static final String BOOST_INPUT_BUNDLE_NAME = "boost_input_bundle_name";
    private static final String BOOST_INPUT_CURSOR_POSITION = "boost_input_bundle_cursor_pos";

    private static final int DEFAULT_BOOST_VALUE = 1;
    private static final int MINIMUM_BOOST_VALUE = DEFAULT_BOOST_VALUE;

    private static final int INVALID_BOOST_VALUE = -1;
    private static final int INVALID_BOOST_INPUT_CURSOR_POS = -1;

    private EditText mBoostInputEdit;

    private AccelerometerManager mAccelManager;
    private AccelNotifFacade mNotifMgr;

    private class BoostLimitChangeListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() != KeyEvent.ACTION_DOWN || keyCode != KeyEvent.KEYCODE_ENTER) {
                return false;
            }

            try {
                int limit = getBoostLimit();
                if (limit < MINIMUM_BOOST_VALUE) {
                    limit = MINIMUM_BOOST_VALUE;
                }

                mAccelManager.setBoostLimit(limit);
                saveBoostInPreferences(limit);
                return true;

            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, getString(R.string.boost_input_invalid_value),
                        Toast.LENGTH_SHORT).show();
            }

            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mAccelManager = new AccelerometerManager(this);
        } catch (AccelerometerManager.NoAccelerometerHardwareException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            mBoostInputEdit.setEnabled(false);
        }

        mNotifMgr = new AccelNotifFacade(this);

        mBoostInputEdit = (EditText) findViewById(R.id.boost_input);
        mBoostInputEdit.setOnKeyListener(new BoostLimitChangeListener());

        restoreBoostFromPreferences();
        setBoostInputCursorPosition(mBoostInputEdit.getText().length());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAccelManager != null) {
            mAccelManager.start(new AccelerometerManager.BoostLimitListener() {

                private int mLimitExceedNumber = 0;

                @Override
                public void onBoostLimitExceed(float value) {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.boost_limit_exceed_message, value),
                            Toast.LENGTH_SHORT).show();

                    mNotifMgr.showBoostLimitExceedNotification(++mLimitExceedNumber);
                    mNotifMgr.playBoostLimitExceedAudioNotification();
                }
            }, getBoostLimit());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mAccelManager != null) {
            mAccelManager.stop();
        }

//        mNotifMgr.hideBoostLimitExceedNotification();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSaveInstanceState");

        outState.putInt(BOOST_INPUT_BUNDLE_NAME, getBoostLimit());
        outState.putInt(BOOST_INPUT_CURSOR_POSITION, getBoostInputCursorPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final int boostLimit = savedInstanceState.getInt(BOOST_INPUT_BUNDLE_NAME,
                INVALID_BOOST_VALUE);
        if (boostLimit > MINIMUM_BOOST_VALUE) {
            mBoostInputEdit.setText(String.valueOf(boostLimit));
        }

        final int cursorPos = savedInstanceState.getInt(BOOST_INPUT_CURSOR_POSITION,
                INVALID_BOOST_INPUT_CURSOR_POS);
        if (cursorPos != INVALID_BOOST_INPUT_CURSOR_POS) {
            setBoostInputCursorPosition(cursorPos);
        }

        Log.d(TAG, "onRestoreInstanceState: " + "Boost limit = " + boostLimit
                + ", Cursor pos = " + cursorPos);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Toast.makeText(this, intent.getAction(), Toast.LENGTH_LONG).show();
    }

    private int getBoostLimit() {
        return Integer.parseInt(mBoostInputEdit.getText().toString());
    }

    private int getBoostInputCursorPosition() {
        return mBoostInputEdit.getSelectionEnd();
    }

    private void setBoostInputCursorPosition(int position) {
        mBoostInputEdit.setSelection(position);
    }

    private void saveBoostInPreferences(int boostValue) {
        getGlobalSharedPrefs()
                .edit()
                .putInt(BOOST_PREF_NAME, boostValue)
                .apply();
    }

    private void restoreBoostFromPreferences() {
        mBoostInputEdit.setText(String.valueOf(
                getGlobalSharedPrefs().getInt(BOOST_PREF_NAME, DEFAULT_BOOST_VALUE)));
    }

    private SharedPreferences getGlobalSharedPrefs() {
        return getSharedPreferences(GLOBAL_SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }
}
