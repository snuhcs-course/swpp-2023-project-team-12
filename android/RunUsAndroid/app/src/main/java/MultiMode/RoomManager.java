package MultiMode;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoomManager implements Serializable {
    private static final List<MultiModeRoom> roomList; // 방의 리스트
    private static final AtomicInteger atomicInteger; // 방 ID 만들기 위한 AtomicInteger

    static {
        roomList = new ArrayList<MultiModeRoom>();
        atomicInteger = new AtomicInteger();
    }

    public RoomManager() {

    }

    public static MultiModeRoom createRoom(MultiModeUser owner, RoomCreateInfo roomCreateInfo, ObjectOutputStream o) {
        int roomId = atomicInteger.incrementAndGet();

        MultiModeRoom room = new MultiModeRoom(roomId, roomCreateInfo);
        room.enterUser(owner);
        room.setRoomOwner(owner);
        room.addOutputStream(o);
        roomList.add(room);
        System.out.println("Room Created, RoomId is " + roomId + ", Owner Image URL: " + owner.getProfileImageUrl());
        return room;
    }

    public static MultiModeRoom getRoom(MultiModeRoom room) {
        for (MultiModeRoom r : roomList) {
            if (r.getId() == room.getId()) {
                return r;
            }
        }
        return null;
    }

    public static MultiModeRoom getRoom(int id) {
        for (MultiModeRoom r : roomList) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public static void removeRoom(MultiModeRoom room) {
        room.close();
        MultiModeRoom removeRoom = null;
        for (MultiModeRoom mroom : roomList) {
            if (mroom.getId() == room.getId()) {
                removeRoom = mroom;
            }
        }
        roomList.remove(removeRoom);
        System.out.println("Room Deleted");
    }

    public static int roomCount() {
        return roomList.size();
    }

    public static List<MultiModeRoom> getRoomList() {
        return roomList;
    }
}
