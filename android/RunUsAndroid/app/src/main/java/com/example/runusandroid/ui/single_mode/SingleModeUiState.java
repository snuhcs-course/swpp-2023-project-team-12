package com.example.runusandroid.ui.single_mode;

import android.widget.Chronometer;

// TODO: refactor using this class
public class SingleModeUiState {

    private final float currentDistance;
    private final Chronometer currentTime;

    public SingleModeUiState(float currentDistance, Chronometer currentTime) {
        this.currentDistance = currentDistance;
        this.currentTime = currentTime;
    }

    public float getCurrentDistance() {
        return currentDistance;
    }

    public Chronometer getCurrentTime() {
        return currentTime;
    }
}
