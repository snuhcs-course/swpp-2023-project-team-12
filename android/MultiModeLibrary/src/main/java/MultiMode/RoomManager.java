package MultiMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoomManager implements Serializable {
    private static List<MultiModeRoom> roomList; // 방의 리스트
    private static AtomicInteger atomicInteger;

    static {
        roomList = new ArrayList();
        atomicInteger = new AtomicInteger();
    }
    public RoomManager(){
        roomList.add(new MultiModeRoom());
    }

    public static MultiModeRoom createRoom(){
        int roomId = atomicInteger.incrementAndGet();
        MultiModeRoom room = new MultiModeRoom(roomId);
        roomList.add(room);
        System.out.println("Room Created");
        return room;
    }

    public static MultiModeRoom createRoom(MultiModeUser owner){
        int roomId = atomicInteger.incrementAndGet();

        MultiModeRoom room = new MultiModeRoom(roomId);
        room.enterUser(owner);
        room.setRoomOwner(owner);

        roomList.add(room);
        System.out.println("Room Created, RoomId is " + roomId);
        return room;
    }


    public static MultiModeRoom getRoom(MultiModeRoom room){
        int idx = roomList.indexOf(room);
        if(idx >= 0){
            return roomList.get(idx);
        }else {
            return null;
        }
    }

    public static MultiModeRoom getRoom(int idx){
        if(idx >= 0){
            return roomList.get(idx);
        }else {
            return null;
        }
    }

    public static void removeRoom(MultiModeRoom room){
        room.close();
        roomList.remove(room);
        System.out.println("Room Deleted");
    }

    public static int roomCount(){
        return roomList.size();
    }

    public static List<MultiModeRoom> getRoomList(){
        return roomList;
    }
}
