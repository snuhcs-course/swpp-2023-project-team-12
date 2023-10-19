package MultiMode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class MultiModeRoom implements Serializable {


    private static final long serialVersionUID = 1L;
    private int id; // 룸 ID
    private List<MultiModeUser> userList; //유저 정보
    private MultiModeUser roomOwner; // 방장

    private RoomCreateInfo roomCreateInfo; //방 정보

    private String title; //방 제목
    private double distance; //목표 거리
    private int numRunners; //제한 인원

    private LocalDateTime startTime; //시작 시각

    private LocalTime duration; //목표 시간(달리는 시간)

    public MultiModeRoom(int roomId, RoomCreateInfo roomCreateInfo) { // 유저가 방을 만들때
        userList = new ArrayList();
        this.id = roomId;
        this.roomCreateInfo = roomCreateInfo;
        this.title = roomCreateInfo.getTitle();
        this.distance = roomCreateInfo.getDistance();
        this.startTime = roomCreateInfo.getStartTime();
        this.duration = roomCreateInfo.getDuration();
        this.numRunners = roomCreateInfo.getNumRunners();
    }
    public MultiModeRoom(MultiModeUser user ) { // 유저가 방을 만들때
        userList = new ArrayList();
        user.enterRoom(this);
        userList.add(user); // 유저를 추가시킨 후
        this.roomOwner = user; // 방장을 유저로 만든다.

    }

    public MultiModeRoom() {

    }

    public void enterUser(MultiModeUser user){
        user.enterRoom(this);
        userList.add(user);
    }

    public void exitUser(MultiModeUser user){
        user.exitRoom(this);
        for(MultiModeUser muser : userList){
            if(muser.getId() == user.getId()){
                userList.remove(muser);
            }
        }
        userList.remove(user);

        if(userList.size() < 1){
            RoomManager.removeRoom(this);
            return;
        }


        if(this.roomOwner.equals(user)){
            this.roomOwner = userList.get(0);
            return;
        }
    }

    public void close(){
        for(MultiModeUser user : userList){
            user.exitRoom(this);
            this.userList.clear();
            this.userList = null;
        }
    }

    public void broadcast(byte[] data){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiModeRoom multiModeRoom = (MultiModeRoom) o;

        return id == multiModeRoom.id;
    }


    public String getTitle() { // 방 이름을 가져옴
        return title;
    }

    public double getDistance() { return distance;}

    public LocalDateTime getStartTime() {return startTime;}

    public int getNumRunners() {return numRunners;}

    public int getUserSize() { // 유저의 수를 리턴
        return userList.size();
    }

    public MultiModeUser getOwner() { // 방장을 리턴
        return roomOwner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List getUserList() {
        return userList;
    }

    public void setUserList(List userList) {
        this.userList = userList;
    }

    public MultiModeUser getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(MultiModeUser roomOwner) {
        this.roomOwner = roomOwner;
    }

    public LocalTime getDuration(){
        return duration;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "MultiModeRoom{" +
                "id=" + id +
                ", userList=" + userList +
                ", roomOwner=" + roomOwner +
                ", roomCreateInfo=" + roomCreateInfo +
                '}';
    }

}
