package MultiMode;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class RoomCreateInfo implements Serializable {
    //방을 새로 생성할 때 담기는 정보.
    private String title;
    private double distance;
    //private String startTime;

    private LocalDateTime startTime;
    private int numRunners;
    private Duration duration;

    // 생성자
    public RoomCreateInfo(String title, double distance, LocalDateTime startTime, int numRunners, Duration duration) {
        this.title = title;
        this.distance = distance;
        this.startTime = startTime;
        this.numRunners = numRunners;
        this.duration = duration;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public int getNumRunners() {
        return numRunners;
    }

    public void setNumRunners(int numRunners) {
        this.numRunners = numRunners;
    }


}