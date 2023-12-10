package com.runus.runusandroid;

import com.google.gson.annotations.SerializedName;

public class GroupHistoryData {

    @SerializedName("roomname")
    String roomname;
    @SerializedName("start_time")
    String start_time;
    @SerializedName("duration")
    long duration;
    @SerializedName("num_players")
    int num_players;

    @SerializedName("first_place_user_id")
    long first_place_user_id;

    @SerializedName("first_place_user_distance")
    float first_place_user_distance;

    @SerializedName("second_place_user_id")
    long second_place_user_id;

    @SerializedName("second_place_user_distance")
    float second_place_user_distance;

    @SerializedName("third_place_user_id")
    long third_place_user_id;

    @SerializedName("third_place_user_distance")
    float third_place_user_distance;

    public GroupHistoryData(String roomname, String start_time, long duration, int num_players,
                            long first_place_user_id, float first_place_user_distance,
                            long second_place_user_id, float second_place_user_distance,
                            long third_place_user_id, float third_place_user_distance) {
        this.roomname = roomname;
        this.start_time = start_time;
        this.duration = duration;
        this.num_players = num_players;
        this.first_place_user_id = first_place_user_id;
        this.first_place_user_distance = first_place_user_distance;
        this.second_place_user_id = second_place_user_id;
        this.second_place_user_distance = second_place_user_distance;
        this.third_place_user_id = third_place_user_id;
        this.third_place_user_distance = third_place_user_distance;
    }

    public GroupHistoryData(String roomname, String start_time, long duration, int num_players,
                            long first_place_user_id, float first_place_user_distance,
                            long second_place_user_id, float second_place_user_distance
    ) {
        this.roomname = roomname;
        this.start_time = start_time;
        this.duration = duration;
        this.num_players = num_players;
        this.first_place_user_id = first_place_user_id;
        this.first_place_user_distance = first_place_user_distance;
        this.second_place_user_id = second_place_user_id;
        this.second_place_user_distance = second_place_user_distance;
        this.third_place_user_id = -1;
        this.third_place_user_distance = -1;
    }

    public GroupHistoryData(String roomname, String start_time, long duration, int num_players,
                            long first_place_user_id, float first_place_user_distance
    ) {
        this.roomname = roomname;
        this.start_time = start_time;
        this.duration = duration;
        this.num_players = num_players;
        this.first_place_user_id = first_place_user_id;
        this.first_place_user_distance = first_place_user_distance;
        this.second_place_user_id = -1;
        this.second_place_user_distance = -1;
        this.third_place_user_id = -1;
        this.third_place_user_distance = -1;
    }
}
