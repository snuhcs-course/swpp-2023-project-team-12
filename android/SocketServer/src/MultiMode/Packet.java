package MultiMode;

import java.io.Serializable;
import java.util.List;

public class Packet implements Serializable { //서버와 통신하기 위해 사용하는 클래스. Protocol + 필요한 정보 넣어서 전송 및 수신
    private static final long serialVersionUID = 1L;

    // 데이터 유형을 나타내는 필드
    private int protocol;

    private RoomCreateInfo roomCreateInfo = null;

    // 실제 데이터
    private MultiModeUser user;

    private MultiModeRoom selectedRoom = null;

    private List<MultiModeRoom> roomList = null;

    public Packet(int protocol, MultiModeUser user) {
        this.protocol = protocol;
        this.user = user;
    }

    public Packet(int protocol, MultiModeRoom selectedRoom) {
        this.protocol = protocol;
        this.selectedRoom = selectedRoom;
    }

    public Packet(int protocol, MultiModeUser user, RoomCreateInfo roomCreateInfo ){
        this.protocol = protocol;
        this.user = user;
        this.roomCreateInfo = roomCreateInfo;
    }

    public Packet(int protocol, MultiModeUser user, MultiModeRoom selectedRoom ){
        this.protocol = protocol;
        this.user = user;
        this.selectedRoom = selectedRoom;
    }

    public Packet(int protocol, List<MultiModeRoom> roomList ){
        this.protocol = protocol;
        this.roomList = roomList;
    }

    public Packet(int protocol, List<MultiModeRoom> roomList, MultiModeRoom selectedRoom ){
        this.protocol = protocol;
        this.roomList = roomList;
        this.selectedRoom = selectedRoom;
    }

    public Packet(int protocol, List<MultiModeRoom> roomList, MultiModeUser user, MultiModeRoom selectedRoom ){
        this.protocol = protocol;
        this.roomList = roomList;
        this.selectedRoom = selectedRoom;
        this.user = user;
    }




    public int getProtocol() {
        return protocol;
    }

    public MultiModeUser getUser() {
        return user;
    }

    public RoomCreateInfo getRoomCreateInfo() {return roomCreateInfo; }

    public MultiModeRoom getSelectedRoom() {return selectedRoom;}

    public List<MultiModeRoom> getRoomList() {return roomList;}
}