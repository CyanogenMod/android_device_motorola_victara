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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import android.util.Log;

public class CMActionsSettings {
    private static final String TAG = "CMActions";

    private static final String GESTURE_CAMERA_KEY = "gesture_camera";
    private static final String GESTURE_PICK_UP_KEY = "gesture_pick_up";
    private static final String GESTURE_IR_WAKEUP_KEY = "gesture_ir_wake_up";
    private static final String GESTURE_IR_SILENCER_KEY = "gesture_ir_silencer";

    private final Context mContext;
    private final UpdatedStateNotifier mUpdatedStateNotifier;

    private boolean mCameraGestureEnabled;
    private boolean mIrWakeUpEnabled;
    private boolean mIrSilencerEnabled;
    private boolean mPickUpGestureEnabled;

    public CMActionsSettings(Context context, UpdatedStateNotifier updatedStateNotifier) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        loadPreferences(sharedPrefs);
        sharedPrefs.registerOnSharedPreferenceChangeListener(mPrefListener);
        mContext = context;
        mUpdatedStateNotifier = updatedStateNotifier;
    }

    private void loadPreferences(SharedPreferences sharedPreferences) {
        mCameraGestureEnabled = sharedPreferences.getBoolean(GESTURE_CAMERA_KEY, false);
        mIrWakeUpEnabled = sharedPreferences.getBoolean(GESTURE_IR_WAKEUP_KEY, false);
        mPickUpGestureEnabled = sharedPreferences.getBoolean(GESTURE_PICK_UP_KEY, false);
        mIrSilencerEnabled = sharedPreferences.getBoolean(GESTURE_IR_SILENCER_KEY, false);
    }

    public boolean isCameraGestureEnabled() {
        return mCameraGestureEnabled;
    }

    public boolean isDozeEnabled() {
        return (Settings.Secure.getInt(mContext.getContentResolver(),
                                    Settings.Secure.DOZE_ENABLED, 1) != 0);
    }

    public boolean isIrWakeupEnabled() {
        return isDozeEnabled() && mIrWakeUpEnabled;
    }

    public boolean isPickUpEnabled() {
        return isDozeEnabled() && mPickUpGestureEnabled;
    }

    public boolean isIrSilencerEnabled() {
        return mIrSilencerEnabled;
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mPrefListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            boolean updated = true;

            if (GESTURE_CAMERA_KEY.equals(key)) {
                mCameraGestureEnabled = sharedPreferences.getBoolean(GESTURE_CAMERA_KEY, false);
            } else if (GESTURE_IR_WAKEUP_KEY.equals(key)) {
                mIrWakeUpEnabled = sharedPreferences.getBoolean(GESTURE_IR_WAKEUP_KEY, false);
            } else if (GESTURE_PICK_UP_KEY.equals(key)) {
                mPickUpGestureEnabled = sharedPreferences.getBoolean(GESTURE_PICK_UP_KEY, false);
            } else if (GESTURE_IR_SILENCER_KEY.equals(key)) {
                mIrSilencerEnabled = sharedPreferences.getBoolean(GESTURE_IR_SILENCER_KEY, false);
            } else {
                updated = false;
            }

            if (updated) {
                mUpdatedStateNotifier.updateState();
            }
        }
    };
}
