package MultiMode;

import java.io.Serializable;
import java.net.Socket;

public class MultiModeUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private MultiModeRoom room;
    private Socket socket;
    private String nickName;

    public MultiModeUser(String nickName){
        this.nickName = nickName;
    }

    public MultiModeUser(int id, String nickName){
        this.id = id;
        this.nickName = nickName;
    }

    public void enterRoom(MultiModeRoom room){
        this.room = room;
    }

    public void exitRoom(MultiModeRoom room){
        this.room = null;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MultiModeRoom getRoom() {
        return room;
    }

    public void setRoom(MultiModeRoom room) {
        this.room = room;
    }

    public Socket getSock() {
        return socket;
    }

    public void setSock(Socket socket) {
        this.socket = socket;
    }

    public String getNickName() {
        return nickName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiModeUser gameUser = (MultiModeUser) o;

        return id == gameUser.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
