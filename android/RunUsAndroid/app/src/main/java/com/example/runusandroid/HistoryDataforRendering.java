package com.example.runusandroid;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HistoryDataforRendering {
    @SerializedName("total_distance")
    private float totalDistance;
    @SerializedName("total_time")
    private String totalTime;
    @SerializedName("total_calories")
    private float totalCalories;
    @SerializedName("daily_data")
    private List<DailyData> dailyData;

    public float getTotalDistance() {
        return totalDistance;
    }

    public String getTotalTime() {
        return totalTime;
    }

    public float getTotalCalories() {
        return totalCalories;
    }

    public List<DailyData> getDailyData() {
        return dailyData;
    }

    @Override
    public String toString() {
        return "HistoryDataforRendering{" +
                "totalDistance=" + totalDistance +
                ", totalTime='" + totalTime + '\'' +
                ", totalCalories=" + totalCalories +
                ", dailyData=" + dailyData +
                '}';
    }

    public static class DailyData {
        private String date;
        private float distance;
        private String time;
        private float calories;
        
        public String getDate() {
            return date;
        }

        public float getDistance() {
            return distance;
        }

        public String getTime() {
            return time;
        }

        public float getCalories() {
            return calories;
        }
    }

}
