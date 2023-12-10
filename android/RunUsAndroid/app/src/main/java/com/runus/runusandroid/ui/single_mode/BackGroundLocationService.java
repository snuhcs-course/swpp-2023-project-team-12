package com.runus.runusandroid.ui.single_mode;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;


public class BackGroundLocationService extends Service {

    static final String START_LOCATION_SERVICE = "start";
    static final String STOP_LOCATION_SERVICE = "stop";
    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                Location lastLocation = locationResult.getLastLocation();
                //Log.d("test:location", "Location:" + lastLocation.getLatitude() + ", " +lastLocation.getLongitude());
                double latitude = lastLocation.getLatitude();
                double longitude = lastLocation.getLongitude();

                Intent intent = new Intent("location_update");
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitude", longitude);
                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(intent);

            }
        }
    };
    FusedLocationProviderClient fusedLocationClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }
        if (intent != null && intent.getAction().equals(START_LOCATION_SERVICE)) {
            Notification notification = getNotification();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setInterval(5000);
                locationRequest.setFastestInterval(5000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                fusedLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());
                startForeground(1122, notification);
            }

        } else if (intent != null && intent.getAction().equals(STOP_LOCATION_SERVICE) && fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(mLocationCallback);
            stopForeground(true);
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private Notification getNotification() {
        NotificationChannel channel = new NotificationChannel("1_location_channel", "location", NotificationManager.IMPORTANCE_LOW);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "1_location_channel").setAutoCancel(false);
        builder.setPriority(Notification.PRIORITY_MAX);

        return builder.build();
    }

}