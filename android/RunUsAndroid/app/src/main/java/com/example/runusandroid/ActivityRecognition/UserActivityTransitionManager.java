package com.example.runusandroid.ActivityRecognition;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class UserActivityTransitionManager {
    Context context;
    final private ActivityRecognitionClient activityClient;
    public static final String CUSTOM_INTENT_USER_ACTION = "USER-ACTIVITY-DETECTION-INTENT-ACTION";
    public static final int CUSTOM_REQUEST_CODE_USER_ACTION = 1000;

    public UserActivityTransitionManager(Context context) {
        this.context = context;
        this.activityClient = ActivityRecognition.getClient(this.context);
    }

    private ActivityTransitionRequest buildTransitionRequest() {
        List<ActivityTransition> transitions = new ArrayList<>();
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());


        return new ActivityTransitionRequest(transitions);
    }

    public void registerActivityTransitions(PendingIntent pendingIntent) {
        ActivityTransitionRequest request = buildTransitionRequest();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "permission not granted", Toast.LENGTH_LONG).show();
                return;
            }
        }
        Task<Void> task = activityClient.requestActivityTransitionUpdates(request, pendingIntent);
        task.addOnSuccessListener(
                result -> Log.d("ActivityTransition", "register success")
        );
        task.addOnFailureListener(
                e -> Log.d("ActivityTransition", "register failed")
        );
    }

    public void removeActivityTransitions(PendingIntent pendingIntent) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "permission not granted", Toast.LENGTH_LONG).show();
                return;
            }
        }
        Task<Void> task = activityClient.removeActivityTransitionUpdates(pendingIntent);
        task.addOnSuccessListener(
                result -> Log.d("ActivityTransition","remove success")
        );
        task.addOnFailureListener(
                e -> Log.d("ActivityTransition","remove failed")
        );
    }
}
