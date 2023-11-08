package MultiMode;

import java.io.Serializable;
import java.net.Socket;

public class MultiModeUser implements Serializable {
    private static final long serialVersionUID = 2194848L;

    private long id;
    private MultiModeRoom room;
    private Socket socket;
    private String nickname;

    public MultiModeUser(String nickName){
        this.nickname = nickname;
    }

    public MultiModeUser(int id, String nickname){
        this.id = id;
        this.nickname = nickname;
    }

    public void enterRoom(MultiModeRoom room){
        this.room = room;
    }

    public void exitRoom(MultiModeRoom room){
        this.room = null;
    }
    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {return this.nickname; }

    public void setnickname(String nickname){
        this.nickname = nickname;
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
        return nickname;
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
        return (int) id;
    }
}
