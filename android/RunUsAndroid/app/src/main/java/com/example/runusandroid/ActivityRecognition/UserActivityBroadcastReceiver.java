package com.example.runusandroid.ActivityRecognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.Objects;

public class UserActivityBroadcastReceiver extends BroadcastReceiver {

    boolean isRunning = false;
    String lastActivityType = "DEFAULT";
    String lastTransitionType = "DEFAULT";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Objects.equals(intent.getAction(), UserActivityTransitionManager.CUSTOM_INTENT_USER_ACTION)) {
            return;
        }
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : result.getTransitionEvents()) {

                if (event.getActivityType() == DetectedActivity.WALKING) lastActivityType = "WALKING";
                else if (event.getActivityType() == DetectedActivity.RUNNING) lastActivityType = "RUNNING";
                else if (event.getActivityType() == DetectedActivity.STILL) lastActivityType = "STILL";
                else lastActivityType = "illegal";

                if(event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER) lastTransitionType = "ENTER";
                else if(event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT) lastTransitionType = "EXIT";
                else lastTransitionType = "illegal";

                Toast.makeText(context, "activity detected, " + lastTransitionType+ " " + lastActivityType, Toast.LENGTH_LONG).show();
                saveState(lastActivityType, lastTransitionType);
            }
        }
    }

    // TODO: isRunning을 너무 보수적으로 잡으면 기록이 안되어 유저가 빡치는 상황이 있을 수 있음, 그 반대는 사용성에 크리티컬하게 체감되지 않음
    private void saveState(String activityType, String transitionType) {
        if(activityType == "RUNNING" && transitionType == "ENTER") {
            isRunning = true;
        }
        else if(activityType == "RUNNING" && transitionType == "EXIT") {
            isRunning = false;
        }
        else if(activityType == "WALKING" && transitionType == "ENTER") {
            isRunning = true;
        }
        else if(activityType == "WALKING" && transitionType == "EXIT") {
            isRunning = false;
        }
        else if(activityType == "STILL" && transitionType == "ENTER") {
            isRunning = false;
        }
        else {
            isRunning = false;
        }
    }

    public boolean getIsRunning() {
        return isRunning;
    }
    public String getLastActivityType() {
        return lastActivityType;
    }
    public String getLastTransitionType() {
        return lastTransitionType;
    }
}
