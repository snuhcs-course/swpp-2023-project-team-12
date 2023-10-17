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

    }

    public static MultiModeRoom createRoom(MultiModeUser owner, RoomCreateInfo roomCreateInfo){
        int roomId = atomicInteger.incrementAndGet();

        MultiModeRoom room = new MultiModeRoom(roomId, roomCreateInfo);
        room.enterUser(owner);
        room.setRoomOwner(owner);

        roomList.add(room);
        System.out.println("Room Created, RoomId is " + roomId);
        return room;
    }



    public static MultiModeRoom getRoom(int roomId) {
        for (MultiModeRoom room : roomList) {
            if (room.getId() == roomId) { // Assuming MultiModeRoom has a getId() method to get its id
                return room;
            }
        }
        return null; // Return null if no matching room is found
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
