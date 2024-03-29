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

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;

import com.gelakinetic.miniscoreboard.R;
import com.gelakinetic.miniscoreboard.activity.MainActivity;

import java.util.Calendar;

public class MiniScoreboardAlarm extends BroadcastReceiver {

    /* A random ID for the notification displayed */
    private static final int NOTIFICATION_ID = 50377;
    public static final String FROM_NOTIFICATION = "FROM_NOTIFICATION";

    /**
     * Build and return a PendingIntent for the Alarm to call
     *
     * @param context The Context to build the intent with
     * @return A PendingIntent to either set or clear
     */
    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, MiniScoreboardAlarm.class);
        int pendingIntentFlags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, 0, intent, pendingIntentFlags);
    }

    /**
     * Set the daily alarm. This is called when the preference is toggled and on boot
     *
     * @param context A Context to set the alarm with
     */
    public static void setAlarm(Context context) {

        /* First, cancel any pending alarms, just in case */
        cancelAlarm(context);

        // Set the alarm to start at approximately noon
        Calendar calendar = Calendar.getInstance();

        // DateFormat sdf = SimpleDateFormat.getDateTimeInstance();
        // Log.e("CAL", "Set at " + sdf.format(calendar.getTime()));

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);

        // calendar.add(Calendar.MINUTE, 1);
        // Log.e("CAL", "Expire at " + sdf.format(calendar.getTime()));

        // With setInexactRepeating(), you have to use one of the AlarmManager interval constants--in this case, AlarmManager.INTERVAL_DAY.
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, getPendingIntent(context));
    }

    /**
     * Cancel the daily Alarm. This is called when the preference is toggled or before resetting
     * the alarm
     *
     * @param context a Context to cancel the alarm with
     */
    public static void cancelAlarm(Context context) {
        /* Cancel the alarm by canceling an equivalent pending intent */
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getPendingIntent(context));
    }

    /**
     * This is called when the Alarm happens. Post a notification to play the puzzle
     *
     * @param context A Context to build the notification with
     * @param intent  Unused
     */
    @Override
    public void onReceive(final Context context, Intent intent) {

        /* Figure out what today's date is */
        final Calendar calendar = Calendar.getInstance();
        int todaysDay = calendar.get(Calendar.DAY_OF_YEAR);

        /* Find out the day of the last submission */
        long lastSubmissionTime = PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(context.getString(R.string.pref_key_last_submission), 0);
        calendar.setTimeInMillis(lastSubmissionTime);
        int lastDay = calendar.get(Calendar.DAY_OF_YEAR);

        /* Only show the notification if there wasn't a submission today */
        if (todaysDay != lastDay) {
            showNotification(context);
        }

        /* Reschedule the next alarm, this should take care of DST */
        setAlarm(context);
    }

    /**
     * Show a notification to play the daily crossword
     *
     * @param context A Context to build the notification with
     */
    public static void showNotification(Context context) {

        int pendingIntentFlags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        /* Create an intent to open the mini crossword in a web browser */
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        notificationIntent.setData(Uri.parse(context.getString(R.string.mini_crossword_url)));
        PendingIntent playCrosswordIntent = PendingIntent.getActivity(context, 0, notificationIntent, pendingIntentFlags);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(FROM_NOTIFICATION, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent submitScoreIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags | PendingIntent.FLAG_UPDATE_CURRENT);

        /* Set the notification parameters */
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NotificationHelper.NOTIFICATION_CHANNEL_DAILY)
                .setSmallIcon(R.drawable.ic_stat_notify_puzzle)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_summary))
                .setContentIntent(playCrosswordIntent)
                .addAction(new NotificationCompat.Action.Builder(
                        R.drawable.ic_puzzle_white_32dp,
                        context.getString(R.string.action_play_crossword),
                        playCrosswordIntent)
                        .build())
                .addAction(new NotificationCompat.Action.Builder(
                        R.drawable.ic_file_upload_white_32dp,
                        context.getString(R.string.action_submit_score),
                        submitScoreIntent).build())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(context);

        /* If the user has vibration enabled, add a buzz */
        if (preferenceManager.getBoolean(context.getString(R.string.pref_key_daily_notification_vibrate), false)) {
            mBuilder = mBuilder.setVibrate(new long[]{0, 500, 500, 500});
        }

        /* If the user has sounds enabled, add a ding */
        if (preferenceManager.getBoolean(context.getString(R.string.pref_key_daily_notification_sound), false)) {
            mBuilder = mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        /* Gets an instance of the NotificationManager service */
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        /* Build the notification and issue it. */
        mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * Clear the daily crossword notification
     *
     * @param context A Context to clear the notification from
     */
    public static void clearNotification(Context context) {
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(NOTIFICATION_ID);
    }
}