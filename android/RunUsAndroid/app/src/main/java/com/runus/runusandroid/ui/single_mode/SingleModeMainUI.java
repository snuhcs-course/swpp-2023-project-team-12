package com.runus.runusandroid.ui.single_mode;

import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

// TODO: refactor using this class

public class SingleModeMainUI {

    private final Chronometer currentTimeText;
    private final TextView currentDistanceText;
    private final TextView currentPaceText;

    private final Button showMissionButton;
    private final Button quitButton;

    public SingleModeMainUI(Chronometer currentTimeText,
                            TextView currentDistanceText,
                            TextView currentPaceText, Button showMissionButton,
                            Button quitButton) {
        this.currentTimeText = currentTimeText;
        this.currentDistanceText = currentDistanceText;
        this.currentPaceText = currentPaceText;
        this.showMissionButton = showMissionButton;
        this.quitButton = quitButton;
    }


    public void updateUI(SingleModeUiState singleModeUiState) {
        _setFloat2Text(currentDistanceText, singleModeUiState.getCurrentDistance());
        _setTime2Text(currentTimeText, singleModeUiState.getCurrentTime());
    }

    private void _setFloat2Text(TextView textView, float f) {

    }

    private void _setTime2Text(TextView textView, Chronometer time) {

    }
}

