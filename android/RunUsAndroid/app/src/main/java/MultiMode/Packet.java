package MultiMode;

import java.io.Serializable;
import java.util.List;

public class Packet implements Serializable {
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