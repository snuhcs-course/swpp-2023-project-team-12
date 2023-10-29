package MultiMode;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class MultiModeRoom implements Serializable {


    private static final long serialVersionUID = 1L;
    private int id; // 룸 ID
    private List<MultiModeUser> userList; //유저 정보
    private MultiModeUser roomOwner; // 방장

    private RoomCreateInfo roomCreateInfo; //방 정보
    private final transient List<ObjectOutputStream> clientOutputStreams = new ArrayList<>();

    private String title; //방 제목
    private double distance; //목표 거리
    private int numRunners; //제한 인원

    private LocalDateTime startTime; //시작 시각

    private Duration duration; //목표 시간(달리는 시간)

    private Queue<UserDistance> updateQueue = new LinkedList<>();

    public MultiModeRoom(int roomId, RoomCreateInfo roomCreateInfo) { // 유저가 방을 만들때
        userList = new ArrayList<MultiModeUser>();
        this.id = roomId;
        this.roomCreateInfo = roomCreateInfo;
        this.title = roomCreateInfo.getTitle();
        this.distance = roomCreateInfo.getDistance();
        this.startTime = roomCreateInfo.getStartTime();
        this.duration = roomCreateInfo.getDuration();
        this.numRunners = roomCreateInfo.getNumRunners();
    }

    public MultiModeRoom(MultiModeUser user) { // 유저가 방을 만들때
        userList = new ArrayList<MultiModeUser>();
        user.enterRoom(this);
        userList.add(user); // 유저를 추가시킨 후
        this.roomOwner = user; // 방장을 유저로 만든다.

    }

    public MultiModeRoom() {

    }

    public void enterUser(MultiModeUser user) {
        user.enterRoom(this);
        userList.add(user);
    }


    public int exitUser(MultiModeUser user) {
        user.exitRoom(this);
        MultiModeUser userToRemove = null;
        int index = -1;
        for (int i = 0; i < userList.size(); i++) {
            MultiModeUser muser = userList.get(i);
            if (muser.getId() == user.getId()) {
                userToRemove = muser;
                index = i;
                break;
            }
        }

        // 대상 사용자를 제거합니다.
        if (userToRemove != null) {
            System.out.println("remove user: " + userToRemove.getNickName());
            userList.remove(userToRemove);
        }

        if (userList.size() < 1) {
            RoomManager.removeRoom(this);
            return index;
        }


        if (this.roomOwner.equals(user)) {
            this.roomOwner = userList.get(0);
        }
        return index;
    }

    public void close() {
        for (MultiModeUser user : userList) {
            user.exitRoom(this);
            this.userList.clear();
            this.userList = null;
        }
    }

    public void broadcast(byte[] data) {

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

    public double getDistance() {
        return distance;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public int getNumRunners() {
        return numRunners;
    }

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

    public List<MultiModeUser> getUserList() {
        return userList;
    }

    public void setUserList(List<MultiModeUser> userList) {
        this.userList = userList;
    }

    public MultiModeUser getRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(MultiModeUser roomOwner) {
        this.roomOwner = roomOwner;
    }

    public Duration getDuration(){
        return duration;
    }

    public RoomCreateInfo getRoomCreateInfo() {
        return roomCreateInfo;
    }

    public List<ObjectOutputStream> getOutputStream() {
        return clientOutputStreams;
    }

    public void addOutputStream(ObjectOutputStream o) {
        clientOutputStreams.add(o);
    }

    public void removeOutputStream(int index) {
        clientOutputStreams.remove(index);
    }


    public void addUser(MultiModeUser user) {
        userList.add(user);
    }

    public boolean isRoomOwner(MultiModeUser user){
        if(user.getId() == roomOwner.getId()){
            return true;
        }
        return false;
    }

    public void updateDistance(UserDistance userDistance){ //유저의 distance를 업데이트
        updateQueue.add(userDistance);
    }

    public UserDistance[] getTop3UserDistance(){ //탑3 유저 distance를 리턴
        if (updateQueue.size() >= userList.size()) {
            // userList의 크기만큼의 원소를 updateQueue에서 빼내어 저장할 리스트
            List<UserDistance> topUserDistances = new ArrayList<>();

            // userList의 크기만큼 원소를 빼내어 topUserDistances에 저장
            for (int i = 0; i < userList.size(); i++) {
                UserDistance userDistance = updateQueue.poll();
                if (userDistance != null) {
                    topUserDistances.add(userDistance);
                }
            }

            // distance를 기준으로 내림차순으로 정렬
            Collections.sort(topUserDistances, new Comparator<UserDistance>() {
                @Override
                public int compare(UserDistance user1, UserDistance user2) {
                    return Double.compare(user2.getDistance(), user1.getDistance());
                }
            });

            // 상위 3개의 UserDistance 객체를 배열에 저장
            UserDistance[] top3UserDistances = new UserDistance[Math.min(3, topUserDistances.size())];
            for (int i = 0; i < top3UserDistances.length; i++) {
                top3UserDistances[i] = topUserDistances.get(i);
            }

            return top3UserDistances;
        }

        return null;
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
