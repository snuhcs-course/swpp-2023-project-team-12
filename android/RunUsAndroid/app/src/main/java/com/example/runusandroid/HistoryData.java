package com.example.runusandroid;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;

import java.util.List;

public class HistoryData {
    @SerializedName("user_id")
    long user_id;
    @SerializedName("distance")
    float distance;
    @SerializedName("duration")
    long duration;
    @SerializedName("is_completed")
    Boolean is_completed;
    @SerializedName("start_time")
    String start_time;
    @SerializedName("finish_time")
    String finish_time;
    @SerializedName("calories")
    float calories;
    @SerializedName("is_group")
    boolean is_group;
    @SerializedName("max_speed")
    float max_speed;
    @SerializedName("min_speed")
    float min_speed;
    @SerializedName("median_speed")
    float median_speed;
    @SerializedName("sectional_speed")
    String sectional_record;
    @SerializedName("group_history_id")
    long group_history_id;

    @SerializedName("is_mission_succeeded")
    int is_mission_succeeded;

    @SerializedName("exp")
    int exp;

    public HistoryData(
            long user_id,
            float distance,
            long duration,
            Boolean is_completed,
            String start_time,
            String finish_time,
            float calories,
            boolean is_group,
            float max_speed,
            float min_speed,
            float median_speed,
            List<Float> sectional_record,
            long group_history_id,
            int is_mission_succeeded,
            int exp
    ) throws JSONException {
        this.user_id = user_id;
        this.distance = distance;
        this.duration = duration;
        this.is_completed = is_completed;
        this.start_time = start_time;
        this.finish_time = finish_time;
        this.calories = calories;
        this.is_group = is_group;
        this.max_speed = max_speed;
        this.min_speed = min_speed;
        this.median_speed = median_speed;
        Gson gson = new Gson();
        String jsonSectionalRecord = gson.toJson(sectional_record);
        this.sectional_record = jsonSectionalRecord;
        this.group_history_id = group_history_id;
        this.is_mission_succeeded = is_mission_succeeded;
        this.exp = exp;
    }
}
