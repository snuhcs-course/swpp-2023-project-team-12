package MultiMode;

import java.io.*;
import java.net.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoomManager implements Serializable {
    private static List<MultiModeRoom> roomList; // 방의 리스트
    private static List<MultiModeRoom> inGameRoomList;
    private static AtomicInteger atomicInteger; // 방 ID 만들기 위한 AtomicInteger

    static {
        roomList = new ArrayList<MultiModeRoom>();
        inGameRoomList = new ArrayList<MultiModeRoom>();
        atomicInteger = new AtomicInteger();
    }

    public RoomManager() {

    }

    public static MultiModeRoom createRoom(MultiModeUser owner, RoomCreateInfo roomCreateInfo, ObjectOutputStream o) {
        int roomId = atomicInteger.incrementAndGet();

        MultiModeRoom room = new MultiModeRoom(roomId, roomCreateInfo);
        RoomObserver observer = new RoomObserver(room);
        room.addUser(owner);
        room.setRoomOwner(owner);
        room.addOutputStream(o);
        roomList.add(room);
        System.out.println("Room Created, RoomId is " + roomId);
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

    public static MultiModeRoom getInGameRoom(MultiModeRoom room) {
        for (MultiModeRoom r : inGameRoomList) {
            if (r.getId() == room.getId()) {
                return r;
            }
        }
        return null;
    }

    public static MultiModeRoom getInGameRoom(int id) {
        for (MultiModeRoom r : inGameRoomList) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    public static void updateRoom(MultiModeRoom room) {
        for (MultiModeRoom mroom : roomList) {
            if (mroom.getId() == room.getId()) {
                mroom.setUserList(room.getUserList());
                mroom.setRoomOwner(room.getRoomOwner());
                break;
            }
        }
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

    public static void removeInGameRoom(MultiModeRoom room) {
        room.close();
        MultiModeRoom removeRoom = null;
        for (MultiModeRoom mroom : inGameRoomList) {
            if (mroom.getId() == room.getId()) {
                removeRoom = mroom;
            }
        }
        roomList.remove(removeRoom);
        System.out.println("Room Deleted");
    }

    public static void startRoom(MultiModeRoom room) {
        MultiModeRoom removeRoom = null;
        for (MultiModeRoom mroom : roomList) {
            if (mroom.getId() == room.getId()) {
                removeRoom = mroom;
            }
        }
        roomList.remove(removeRoom);
        inGameRoomList.add(removeRoom);
    }

    public static int roomCount() {
        return roomList.size();
    }

    public static List<MultiModeRoom> getRoomList() {
        return roomList;
    }
}
