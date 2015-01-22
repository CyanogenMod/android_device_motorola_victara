/*
 * Copyright (c) 2015 The CyanogenMod Project
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

package org.cyanogenmod.cmactions;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class StowSensor implements ActionableSensor, SensorEventListener {
    private static final String TAG = "CMActions-StowSensor";

    private SensorHelper mSensorHelper;
    private State mState;
    private SensorAction mSensorAction;

    private Sensor sensor;

    public StowSensor(SensorHelper sensorHelper, State state, SensorAction action) {
        mSensorHelper = sensorHelper;
        mState = state;
        mSensorAction = action;

        sensor = sensorHelper.getStowSensor();
    }

    @Override
    public void enable() {
        Log.d(TAG, "Enabling");
        mSensorHelper.registerListener(sensor, this);
    }

    @Override
    public void disable() {
        Log.d(TAG, "Disabling");
        mSensorHelper.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean thisStowed = (event.values[0] != 0);
        Log.d(TAG, "event: " + thisStowed);
        if (mState.setIsStowed(thisStowed) && ! thisStowed) {
            mSensorAction.action();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
