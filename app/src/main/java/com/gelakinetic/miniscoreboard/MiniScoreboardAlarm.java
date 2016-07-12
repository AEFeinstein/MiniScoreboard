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

package com.gelakinetic.miniscoreboard;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

public class MiniScoreboardAlarm extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 50377;
    private static final String DAILY_NOTIFICATION_INTENT = "com.gelakinetic.miniscoreboard.DAILY_NOTIFICATION";

    /**
     * TODO
     *
     * @param context
     * @return
     */
    public static PendingIntent GetPendingIntent(Context context) {
        Intent intent = new Intent(DAILY_NOTIFICATION_INTENT);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    /**
     * TODO
     *
     * @param context
     */
    public static void SetAlarm(Context context) {

        /* Set the alarm to start at approximately 12:00 p.m. */
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);

        /* Set it to repeat daily in an inexact fashion. This saves battery because the system
         * can bunch together alarms
         */
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, GetPendingIntent(context));
    }

    /**
     * TODO
     *
     * @param context
     */
    public static void CancelAlarm(Context context) {
        /* Cancel the alarm by canceling an equivalent pending intent */
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(GetPendingIntent(context));
    }

    /**
     * TODO
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        /* Create an intent to open the mini crossword in a web browser */
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        notificationIntent.setData(Uri.parse("http://www.nytimes.com/crosswords/game/mini"));
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        /* Set the notification parameters */
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_notify_puzzle)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_summary))
                .setContentIntent(contentIntent)
                .setVibrate(new long[]{0, 500, 500, 500})
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        /* Gets an instance of the NotificationManager service */
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        /* Build the notification and issue it. */
        mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
    }
}