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

package com.cyanogenmod.settings.device;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.util.Log;

public class CameraActivationSensor implements ActionableSensor, SensorEventListener {
    private static final String TAG = "CMActions-CameraSensor";

    private static final int TURN_SCREEN_ON_WAKE_LOCK_MS = 500;

    private final CMActionsSettings mCMActionsSettings;
    private final SensorHelper mSensorHelper;
    private final SensorAction mSensorAction;

    private final Sensor mCameraActivationSensor;
    private final Sensor mChopChopSensor;

    private boolean mIsEnabled;

    public CameraActivationSensor(CMActionsSettings cmActionsSettings, SensorHelper sensorHelper,
                SensorAction sensorAction) {
        mCMActionsSettings = cmActionsSettings;
        mSensorHelper = sensorHelper;
        mSensorAction = sensorAction;
        mCameraActivationSensor = sensorHelper.getCameraActivationSensor();
        mChopChopSensor = sensorHelper.getChopChopSensor();

        updateListener();
    }

    @Override
    public void setScreenOn() {
        updateListener();
    }

    @Override
    public void setScreenOff() {
        updateListener();
    }

    private synchronized void updateListener() {
        if (mCMActionsSettings.isCameraGestureEnabled() && !mIsEnabled) {
            Log.d(TAG, "Enabling");
            mSensorHelper.registerListener(mCameraActivationSensor, this);
            mSensorHelper.registerListener(mChopChopSensor, this);
            mIsEnabled = true;
        } else if (! mCMActionsSettings.isCameraGestureEnabled() && mIsEnabled) {
            Log.d(TAG, "Disabling");
            mSensorHelper.unregisterListener(this);
            mIsEnabled = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "activate camera");
        mSensorAction.action();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
