package MultiMode;

import java.io.Serializable;
import java.util.List;

public class Packet implements Serializable { //서버와 통신하기 위해 사용하는 클래스. Protocol + 필요한 정보 넣어서 전송 및 수신
    private static final long serialVersionUID = 1L;
    public int temp;
    UserDistance[] top3UserDistance = null;
    List<UserDistance> listTop3UserDistance = null;
    // 데이터 유형을 나타내는 필드
    private int protocol;
    private RoomCreateInfo roomCreateInfo = null;
    // 실제 데이터
    private MultiModeUser user;
    private float distance;
    private MultiModeRoom selectedRoom = null;
    private List<MultiModeRoom> roomList = null; //top3 유저 정보 가져오는 자료구조
    private long groupHistoryId; //db에 저장된 히스토리 id

    public Packet() {

    }

//    public Packet(int protocol, MultiModeUser user) {
//        this.protocol = protocol;
//        this.user = user;
//    }
//
//    public Packet(int protocol) {
//        this.protocol = protocol;
//    }
//
//    public Packet(int protocol, MultiModeRoom selectedRoom) {
//        this.protocol = protocol;
//        this.selectedRoom = selectedRoom;
//    }
//
//    public Packet(int protocol, MultiModeUser user, RoomCreateInfo roomCreateInfo) {
//        this.protocol = protocol;
//        this.user = user;
//        this.roomCreateInfo = roomCreateInfo;
//    }
//
//    public Packet(int protocol, MultiModeUser user, MultiModeRoom selectedRoom) {
//        this.protocol = protocol;
//        this.user = user;
//        this.selectedRoom = selectedRoom;
//    }
//
//    public Packet(int protocol, MultiModeUser user, MultiModeRoom selectedRoom, long groupHistoryId) {
//        this.protocol = protocol;
//        this.user = user;
//        this.selectedRoom = selectedRoom;
//        this.groupHistoryId = groupHistoryId;
//    }
//
//    public Packet(int protocol, List<MultiModeRoom> roomList) {
//        this.protocol = protocol;
//        this.roomList = roomList;
//    }
//
//    public Packet(int protocol, List<MultiModeRoom> roomList, int n, int m) {
//        this.protocol = protocol;
//        this.roomList = roomList;
//        this.temp = n;
//    }
//
//    public Packet(int protocol, List<MultiModeRoom> roomList, MultiModeRoom selectedRoom) {
//        this.protocol = protocol;
//        this.roomList = roomList;
//        this.selectedRoom = selectedRoom;
//    }
//
//    public Packet(int protocol, List<MultiModeRoom> roomList, MultiModeUser user, MultiModeRoom selectedRoom) {
//        this.protocol = protocol;
//        this.roomList = roomList;
//        this.selectedRoom = selectedRoom;
//        this.user = user;
//    }
//
//    public Packet(int protocol, UserDistance[] top3UserDistance) {
//        this.protocol = protocol;
//        this.top3UserDistance = top3UserDistance;
//    }
//
//    public Packet(int protocol, UserDistance[] top3UserDistance, long groupHistoryId) {
//        this.protocol = protocol;
//        this.top3UserDistance = top3UserDistance;
//        this.groupHistoryId = groupHistoryId;
//    }
//
//    public Packet(int protocol, List<UserDistance> lTop3UserDistances, int temp) {
//        this.protocol = protocol;
//        this.listTop3UserDistance = lTop3UserDistances;
//        this.groupHistoryId = temp;
//
//    }
//
//    public Packet(int protocol, MultiModeUser user, float distance) {
//        this.protocol = protocol;
//        this.user = user;
//        this.distance = distance;
//    }

    public long getGroupHistoryId() {
        return groupHistoryId;
    }

    public void setGroupHistoryId(long groupHistoryId) {
        this.groupHistoryId = groupHistoryId;
    }

    public List<UserDistance> getListTop3UserDistance() {
        return listTop3UserDistance;
    }

    public void setListTop3UserDistance(List<UserDistance> listTop3UserDistance) {
        this.listTop3UserDistance = listTop3UserDistance;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public MultiModeUser getUser() {
        return user;
    }

    public void setUser(MultiModeUser user) {
        this.user = user;
    }

    public RoomCreateInfo getRoomCreateInfo() {
        return roomCreateInfo;
    }

    public void setRoomCreateInfo(RoomCreateInfo roomCreateInfo) {
        this.roomCreateInfo = roomCreateInfo;
    }

    public MultiModeRoom getSelectedRoom() {
        return selectedRoom;
    }

    public void setSelectedRoom(MultiModeRoom selectedRoom) {
        this.selectedRoom = selectedRoom;
    }

    public List<MultiModeRoom> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<MultiModeRoom> roomList) {
        this.roomList = roomList;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public UserDistance[] getTop3UserDistance() {
        return top3UserDistance;
    }

    public void setTop3UserDistance(UserDistance[] top3UserDistance) {
        this.top3UserDistance = top3UserDistance;
    }

    public void setTemp(int temp) {
        this.temp = temp;
    }
}