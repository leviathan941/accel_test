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
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import org.leveafan941.boostdetection.R;
import org.leveafan941.boostdetection.accelerometer.AccelerometerManager;

/**
 * @author Alexey Kuzin (amkuzink@gmail.com).
 */
public class MainActivity extends AppCompatActivity {

    private static final String GLOBAL_SHARED_PREFS_NAME = "BOOST_MAIN_PREFS";
    private static final String BOOST_PREF_NAME = "boost_preference";

    private static final int DEFAULT_BOOST_VALUE = 1;

    private EditText mBoostInputEdit;

    private AccelerometerManager mAccelManager;

    private class BoostInputTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // This method is not used.
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0) {
                return;
            }

            try {
                final int limit = Integer.parseInt(s.toString());
                mAccelManager.setBoostLimit(
                        limit > DEFAULT_BOOST_VALUE ? limit : DEFAULT_BOOST_VALUE);
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Invalid value", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            // This method is not used.
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAccelManager = new AccelerometerManager(this);

        mBoostInputEdit = (EditText) findViewById(R.id.boost_input);
        mBoostInputEdit.addTextChangedListener(new BoostInputTextWatcher());
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAccelManager.start(new AccelerometerManager.BoostLimitListener() {

            @Override
            public void onBoostLimitExceed(float value) {
                Toast.makeText(MainActivity.this, "Acceleration limit exceed: " + value,
                        Toast.LENGTH_SHORT).show();
            }
        }, getAccelerationLimit());
    }

    @Override
    protected void onPause() {
        super.onPause();

        mAccelManager.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();

        restoreBoostFromPreferences();
    }

    @Override
    protected void onStop() {
        super.onStop();

        saveBoostInPreferences(getAccelerationLimit());
    }

    private int getAccelerationLimit() {
        return Integer.parseInt(mBoostInputEdit.getText().toString());
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
