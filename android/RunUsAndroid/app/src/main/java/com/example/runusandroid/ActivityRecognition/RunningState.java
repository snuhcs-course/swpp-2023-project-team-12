package com.example.runusandroid.ActivityRecognition;

public class RunningState {
    static boolean isRunning = false;
    static String lastActivityType = "DEFAULT";
    static String lastTransitionType = "DEFAULT";

    static public void saveState(String activityType, String transitionType) {
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

        lastActivityType = activityType;
        lastTransitionType = transitionType;
    }


    public static boolean getIsRunning() {
        return isRunning;
    }
    public static String getLastActivityType() {
        return lastActivityType;
    }
    public static String getLastTransitionType() {
        return lastTransitionType;
    }
}
