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

    boolean isRunning = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Objects.equals(intent.getAction(), UserActivityTransitionManager.CUSTOM_INTENT_USER_ACTION)) {
            return;
        }
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                String activityType;
                String transitionType;

                if (event.getActivityType() == DetectedActivity.WALKING) activityType = "WALKING";
                else if (event.getActivityType() == DetectedActivity.RUNNING) activityType = "RUNNING";
                else if (event.getActivityType() == DetectedActivity.STILL) activityType = "STILL";
                else activityType = "illegal";

                if(event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_ENTER) transitionType = "ENTER";
                else if(event.getTransitionType() == ActivityTransition.ACTIVITY_TRANSITION_EXIT) transitionType = "EXIT";
                else transitionType = "illegal";

                Toast.makeText(context, "activity detected, " + transitionType+ " " + activityType, Toast.LENGTH_LONG).show();
                saveState(activityType, transitionType);
            }
        }
    }

    // TODO: isRunning을 너무 보수적으로 잡으면 기록이 안되어 유저가 빡치는 상황이 있을 수 있음, 그 반대는 사용성에 크리티컬하게 체감되지 않음
    private void saveState(String activityType, String transitionType) {
        if(activityType == "RUNNING" && transitionType == "ENTER") {
            isRunning = true;
        }
        else if(activityType == "WALKING" && transitionType == "ENTER") {
            isRunning = true;
        }
        else {
            isRunning = false;
        }
    }

    public boolean getIsRunning() {
        return isRunning;
    }
}
