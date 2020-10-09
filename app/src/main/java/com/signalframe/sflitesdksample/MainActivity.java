package com.signalframe.sflitesdksample;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.signalframe.sflitesdk.SFBeaconManager;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 1;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SFBeaconManager beaconManager = SFBeaconManager.Companion.getInstance(this);
        setContentView(R.layout.activity_main);
        beaconManager.getMonitoringData().getState().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer beaconVisibility) {
                Log.d(TAG, "I changed state from seeing/not seeing beacons: "+beaconVisibility);
            }
        });
        beaconManager.getRangingData().getBeacons().observe(this, new Observer<Collection<Beacon>>() {
            @Override
            public void onChanged(final Collection<Beacon> beacons) {
                Log.d(TAG, "I see "+beacons.size()+" beacons.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView label = MainActivity.this.findViewById(R.id.label);
                        label.setText("SFLiteSDK version: "+beaconManager.getVersion()+"\n"+beacons.size()+" beacon(s) visible.");
                    }
                });
            }
        });
        Log.d(TAG, "SFLiteSDK version: "+beaconManager.getVersion());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermissions();
        }
    }

    // Code needed to request and obtain background location permission from the user is complex
    // as of Android 6.0, and more complex as of Android 11.0.  We recommend you use the following
    // method to get these permissions, and execute it whenever the Activity resumes.  Code below
    // should be modified only if you are confident you know how this all works.  Please feel free
    // to customize the text messaging to the user as you see fit for your application.
    @RequiresApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            int backgroundGrantStatus = checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            if (backgroundGrantStatus  != PackageManager.PERMISSION_GRANTED ) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(this);
                    builder.setTitle("This app needs background location access");
                    builder.setMessage("Please grant location access so this app can detect beacons in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @TargetApi(23)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                    PERMISSION_REQUEST_BACKGROUND_LOCATION);
                        }

                    });
                    builder.show();
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(this);
                        builder.setTitle("'Allow All the Time' Location Access Required");
                        builder.setMessage("Please go to Permissions -> Location and grant this app 'Allow All the Time' access");
                        builder.setPositiveButton(android.R.string.ok, null);
                        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                            @TargetApi(23)
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                // Tragically, there is no way to link directly to the Permissions page of the app.  The best you can do is link to the
                                // general settings page.
                                startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
                            }

                        });
                        builder.show();
                    }
                }
            }
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_REQUEST_FINE_LOCATION);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Functionality limited");
                builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.show();
            }
        }
    }


}