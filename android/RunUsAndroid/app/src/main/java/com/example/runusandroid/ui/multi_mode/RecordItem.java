package com.example.runusandroid.ui.multi_mode;

public class RecordItem {
    private final double section;
    private final double averageSpeed;

    public RecordItem(double section, double pace) {
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
