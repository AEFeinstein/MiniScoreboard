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
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class MiniScoreboardAlarm extends BroadcastReceiver {

    /* A random ID for the notification displayed */
    private static final int NOTIFICATION_ID = 50377;

    /**
     * Build and return a PendingIntent for the Alarm to call
     *
     * @param context The Context to build the intent with
     * @return A PendingIntent to either set or clear
     */
    public static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, MiniScoreboardAlarm.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    /**
     * Set the daily alarm. This is called when the preference is toggled and on boot
     *
     * @param context A Context to set the alarm with
     */
    public static void setAlarm(Context context) {

        /* First, cancel any pending alarms, just in case */
        cancelAlarm(context);

        /* Set the alarm's trigger to the next noon */
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        /* If noon already passed, set it to tomorrow's noon */
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.setTimeInMillis(calendar.getTimeInMillis() + (24 * 60 * 60 * 1000));
        }

        /* Set it to repeat daily in an inexact fashion. This saves battery because the system
         * can bunch together alarms
         */
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
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

        /* Make sure the user is authenticated */
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {

            /* Figure out what today's date is */
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.setTimeInMillis(0);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            String date = Long.toString(calendar.getTimeInMillis() / 1000);

            /* Read from the database where this user's daily score would be */
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                    .child("dailyScores")
                    .child(date)
                    .child(currentUser.getUid());

            /* If there's no internet connection, no callback is ever called */
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    DatabaseScoreEntry entry = dataSnapshot.getValue(DatabaseScoreEntry.class);
                    if (entry == null) {
                        /* No entry yet, show the notification */
                        showNotification(context);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    /* No database connection, show the notification anyway */
                    showNotification(context);
                }
            });
        }
    }

    /**
     * Show a notification to play the daily crossword
     *
     * @param context A Context to build the notification with
     */
    private void showNotification(Context context) {
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
                .setContentIntent(contentIntent);

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
}