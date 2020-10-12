package com.signalframe.sflitesdksample;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.signalframe.sflitesdk.SFBeaconManager;

// This class must be registered in your AndroidManifest.xml under
// <application android:name=".MyApplication"
public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
        // You must set up the SDK here for background detections
        final SFBeaconManager beaconManager = SFBeaconManager.Companion.getInstance(this);

        beaconManager.setClient("acme"); // This should be your company name registered with the servers
        beaconManager.setEmail("test@test.com"); // This should be the logged in user

        // When the SDK runs in the background on Android 8+ devices, it must display a persistent
        // notification to the user to indicate it is running.  The code below shows how you
        // customize this notificaton
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(this, "Foreground Service Channel");
            builder.setSmallIcon(R.drawable.notification_icon_background);
            builder.setContentTitle("Scanning for Beacons");
            NotificationChannel channel =  new NotificationChannel("Foreground Service Channel",
                    "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Lets you know when the app is running in the background.");
            NotificationManager notificationManager =  (NotificationManager) this.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
            TaskStackBuilder stackBuilder =  TaskStackBuilder.create(this);
            stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            builder.setContentIntent(resultPendingIntent);
            beaconManager.foregroundServiceNotification = builder.build();
        } else {
            Notification.Builder builder =  new Notification.Builder(this);
            builder.setSmallIcon(R.drawable.notification_icon_background);
            builder.setContentTitle("Scanning for Beacons");
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
            );
            builder.setContentIntent(pendingIntent);
            beaconManager.foregroundServiceNotification = builder.build();
        }

        beaconManager.start();
    }
}

