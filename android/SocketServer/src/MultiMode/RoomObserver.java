package MultiMode;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class RoomObserver {
    private MultiModeRoom room;
    RoomObserver(MultiModeRoom room){
        this.room = room;
        room.registerObserver(this);
    }
    public void update(Packet packet){
        List<ObjectOutputStream> oosList = this.room.getOutputStream();
        for (int i = 0; i < oosList.size(); i++) {
            ObjectOutputStream oos = oosList.get(i);
            if (oos != null) {
                try {
                    oos.reset();
                    oos.writeObject(packet);
                    oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
