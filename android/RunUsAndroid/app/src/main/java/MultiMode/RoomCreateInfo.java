package MultiMode;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RoomCreateInfo implements Serializable {
    private String title;
    private double distance;
    private String startTime;
    private int numRunners;



    private int runningTimeHour;

    private int runningTimeMinute;


    // 생성자
    public RoomCreateInfo(String title, double distance, String startTime, int numRunners, int runningTimeHour, int runningTimeMinute) {
        this.title = title;
        this.distance = distance;
        this.startTime = startTime;
        this.numRunners = numRunners;
        this.runningTimeHour = runningTimeHour;
        this.runningTimeMinute = runningTimeMinute;
    }

    // Getter 및 Setter 메서드
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getNumRunners() {
        return numRunners;
    }

    public void setNumRunners(int numRunners) {
        this.numRunners = numRunners;
    }

    public int getRunningTimeHour() {
        return runningTimeHour;
    }

    public void setRunningTimeHour(int runningTimeHour) {
        this.runningTimeHour = runningTimeHour;
    }

    public int getRunningTimeMinute() {
        return runningTimeMinute;
    }

    public void setRunningTimeMinute(int runningTimeMinute) {
        this.runningTimeMinute = runningTimeMinute;
    }


}