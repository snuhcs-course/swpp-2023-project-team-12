package com.example.runusandroid.ui.multi_mode;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


import androidx.annotation.Nullable;

import java.util.Objects;

public class BackGroundSocketService extends Service {
    static final String START_SOCKET_SERVICE = "start";
    static final String STOP_SOCKET_SERVICE = "stop";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("start service","socket service come");
        if(intent==null){
            return START_STICKY;
        }
        if (Objects.equals(intent.getAction(), START_SOCKET_SERVICE)) {
            Log.d("start service","socket service start");
            Notification notification = getNotification();
            startForeground(1133, notification);

        } else if (Objects.equals(intent.getAction(), STOP_SOCKET_SERVICE)) {
            Log.d("start service","socket service stop");
            stopForeground(true);
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private Notification getNotification() {
        NotificationChannel channel = new NotificationChannel("1_Socket_channel", "location", NotificationManager.IMPORTANCE_LOW);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "1_Socket_channel").setAutoCancel(false);

        return builder.build();
    }
}
