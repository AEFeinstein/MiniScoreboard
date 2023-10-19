package com.gelakinetic.miniscoreboard.notification;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.gelakinetic.miniscoreboard.R;

public class NotificationHelper {

    protected static final String NOTIFICATION_CHANNEL_DAILY = "daily_notification";
    private static final int REQUEST_POST_NOTIFICATION = 487676;
    private static final int REQUEST_SET_ALARM = 9681910;
    private static final int REQUEST_SCHEDULE_EXACT_ALARM = 130586;
    private static final int REQUEST_USE_EXACT_ALARM = 669201202;

    public static void createChannels(Context context) {

        // For Oreo and above, create a channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use NotificationManager, not NotificationManagerCompat
            NotificationManager manager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));

            if (null == manager) {
                return;
            }
            // Create the channel for displaying the daily notification, nearly default
            manager.createNotificationChannel(
                    new NotificationChannel(
                            NotificationHelper.NOTIFICATION_CHANNEL_DAILY,
                            context.getString(R.string.notification_title),
                            NotificationManager.IMPORTANCE_DEFAULT));
        }
    }

    public static void requestNotificationPermission(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATION);
            }
        } else if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.SET_ALARM) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.SET_ALARM},
                    REQUEST_SET_ALARM);
        } else if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM},
                        REQUEST_SCHEDULE_EXACT_ALARM);
            }
        } else if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.USE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.USE_EXACT_ALARM},
                        REQUEST_USE_EXACT_ALARM);
            }
        }
    }
}
