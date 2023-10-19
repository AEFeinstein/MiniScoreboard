/**
 * Copyright 2016 Adam Feinstein
 * <p/>
 * This file is part of Mini Scoreboard.
 * <p/>
 * Mini Scoreboard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * Mini Scoreboard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with Mini Scoreboard.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.gelakinetic.miniscoreboard.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.preference.PreferenceManager;

import com.gelakinetic.miniscoreboard.R;

public class MiniScoreboardBootReceiver extends BroadcastReceiver {

    /**
     * Called when the device boots, this allows the app to set the daily alarm
     *
     * @param context A Context to get preferences and set the alarm with
     * @param intent  The intent that called this receiver,
     *                should be android.intent.action.BOOT_COMPLETED
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            if (PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.pref_key_daily_notification), false)) {
                /* alarm is enabled */
                MiniScoreboardAlarm.setAlarm(context);
            } else {
                /* alarm is disabled */
                MiniScoreboardAlarm.cancelAlarm(context);
            }
        }
    }
}