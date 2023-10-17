package MultiMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MultiModeRoom implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id; // 룸 ID
    private List<MultiModeUser> userList;
    private MultiModeUser roomOwner; // 방장

    private RoomCreateInfo roomCreateInfo; //방 정보

    public MultiModeRoom(int roomId, RoomCreateInfo roomCreateInfo) { // 유저가 방을 만들때
        userList = new ArrayList();
        this.id = roomId;
        this.roomCreateInfo = roomCreateInfo;
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
        return roomCreateInfo.getTitle();
    }

    public double getDistance() { return roomCreateInfo.getDistance();}

    public String getStartTime() {return roomCreateInfo.getStartTime();}

    public int getNumRunners() {return roomCreateInfo.getNumRunners();}

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

    public int getRunningTimeHour() {
        return roomCreateInfo.getRunningTimeHour();
    }

    public int getRunningTimeMinute() {
        return roomCreateInfo.getRunningTimeMinute();
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
