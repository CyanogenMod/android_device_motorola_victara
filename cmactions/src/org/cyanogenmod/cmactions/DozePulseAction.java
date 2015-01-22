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

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DozePulseAction implements SensorAction {
    private static final String TAG = "CMActions";

    private Context mContext;
    private State mState;

    public DozePulseAction(Context context, State state) {
        mContext = context;
        mState = state;
    }

    public void action() {
         if (mState.mayDoze()) {
            Log.d(TAG, "Sending doze.pulse intent");
            mContext.sendBroadcast(new Intent("com.android.systemui.doze.pulse"));
        }
    }
}
