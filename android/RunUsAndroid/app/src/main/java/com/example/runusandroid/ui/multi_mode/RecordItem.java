package com.example.runusandroid.ui.multi_mode;

public class RecordItem {
    private final double section;
    private final double averageSpeed;

    RecordItem(double section, double pace) {
        this.section = section;
        this.averageSpeed = pace;
    }

    double getSection() {
        return section;
    }

    double getAverageSpeed() {
        return averageSpeed;
    }
}
