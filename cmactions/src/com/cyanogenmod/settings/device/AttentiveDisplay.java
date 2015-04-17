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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import static com.cyanogenmod.settings.device.IrGestureManager.*;

public class AttentiveDisplay implements ActionableSensor, SensorEventListener {
    private static final String TAG = "CMActions-AD";

    private static final int IR_GESTURES_FOR_SCREEN_ON = (1 << IR_GESTURE_OBJECT_DETECTED) | (1 << IR_GESTURE_GESTURE_OBJECT_NOT_DETECTED);
    private static final int IR_GESTURES_FOR_SCREEN_OFF = 0;

    private final CMActionsSettings mCMActionsSettings;
    private final SensorHelper mSensorHelper;
    private final IrGestureVote mIrGestureVote;
    private final PowerManager mPowerManager;
    private final Sensor mSensor;

    private boolean mEnabled;
    private boolean mScreenIsLocked;
    private WakeLock mWakeLock;

    public AttentiveDisplay(CMActionsSettings cmActionsSettings, SensorHelper sensorHelper,
                IrGestureManager irGestureManager, Context context) {
        mCMActionsSettings = cmActionsSettings;
        mSensorHelper = sensorHelper;
        mIrGestureVote = new IrGestureVote(irGestureManager);
        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);

        mSensor = sensorHelper.getIrGestureSensor();
        mIrGestureVote.voteForState(false, 0);
    }

    @Override
    public void setScreenOn() {
        if (mCMActionsSettings.isAttentiveDisplayEnabled()) {
            enableSensor();
        } else {
            // Option was potentially disabled while the screen is on, make
            // sure everything is turned off if it was enabled.
            disableSensor();
            disableScreenLock();
        }
    }

    @Override
    public void setScreenOff() {
        disableSensor();
        disableScreenLock();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int gesture = (int) event.values[1];

        if (gesture == IR_GESTURE_OBJECT_DETECTED) {
            enableScreenLock();
        } else if (gesture == IR_GESTURE_GESTURE_OBJECT_NOT_DETECTED) {
            disableScreenLock();
        }
    }

    private void enableSensor() {
        if (! mEnabled) {
            Log.d(TAG, "Enabling");
            mSensorHelper.registerListener(mSensor, this);
            mIrGestureVote.voteForState(true, IR_GESTURES_FOR_SCREEN_OFF);
            mEnabled = true;
        }
    }

    private void disableSensor() {
        if (mEnabled) {
            Log.d(TAG, "Disabling");
            mSensorHelper.unregisterListener(this);
            mIrGestureVote.voteForState(false, IR_GESTURES_FOR_SCREEN_ON);
            mEnabled = false;
        }
    }

    private void enableScreenLock() {
        if (! mScreenIsLocked) {
            Log.d(TAG, "Acquiring screen wakelock");
            mScreenIsLocked = true;
            mWakeLock.acquire();
        }
    }

    private void disableScreenLock() {
        if (mScreenIsLocked) {
            mScreenIsLocked = false;
            mWakeLock.release();
            Log.d(TAG, "Released screen wakelock");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor mSensor, int accuracy) {
    }
}
