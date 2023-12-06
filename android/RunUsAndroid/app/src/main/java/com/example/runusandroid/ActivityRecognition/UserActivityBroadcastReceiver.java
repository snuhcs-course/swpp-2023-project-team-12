package com.example.runusandroid.ActivityRecognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

public class UserActivityBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                Log.d("ActivityTransition", "activity detected");
                String activityType;
                String transitionType;

                if (event.getActivityType() == DetectedActivity.WALKING) activityType = "WALKING";
                else if (event.getActivityType() == DetectedActivity.RUNNING)
                    activityType = "RUNNING";
                else if (event.getActivityType() == DetectedActivity.STILL) activityType = "STILL";
                else activityType = "illegal";

                if (event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    transitionType = "ENTER";
                else if (event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    transitionType = "EXIT";
                else transitionType = "illegal";

                //Toast.makeText(context, "activity detected, " + transitionType + " " + activityType, Toast.LENGTH_LONG).show();
                RunningState.saveState(activityType, transitionType);
            }
        }
    }

}


