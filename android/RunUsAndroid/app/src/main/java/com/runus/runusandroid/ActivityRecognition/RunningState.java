package com.runus.runusandroid.ActivityRecognition;

public class RunningState {
    static boolean isRunning = true;
    static String lastActivityType = "DEFAULT";
    static String lastTransitionType = "DEFAULT";

    static public void saveState(String activityType, String transitionType) {
        if (activityType == "RUNNING") {
            isRunning = true;
        } else if (activityType == "WALKING") {
            isRunning = true;
        } else isRunning = activityType == "STILL";

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
