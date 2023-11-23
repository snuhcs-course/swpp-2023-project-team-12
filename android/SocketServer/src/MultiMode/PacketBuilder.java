package MultiMode;

import java.util.List;

public class PacketBuilder {
    private Packet packet;

    public PacketBuilder(){
        this.packet = new Packet();
    }

    public PacketBuilder protocol(int protocol){
        packet.setProtocol(protocol);
        return this;
    }
    public PacketBuilder top3UserDistance(UserDistance[] top3UserDistance){
        packet.setTop3UserDistance(top3UserDistance);
        return this;
    }
    public PacketBuilder listTop3UserDistance(List<UserDistance> listTop3UserDistance){
        packet.setListTop3UserDistance(listTop3UserDistance);
        return this;
    }
    public PacketBuilder roomCreateInfo(RoomCreateInfo roomCreateInfo){
        packet.setRoomCreateInfo(roomCreateInfo);
        return this;
    }
    public PacketBuilder user(MultiModeUser user){
        packet.setUser(user);
        return this;
    }
    public PacketBuilder distance(float distance){
        packet.setDistance(distance);
        return this;
    }
    public PacketBuilder selectedRoom(MultiModeRoom selectedRoom){
        packet.setSelectedRoom(selectedRoom);
        return this;
    }
    public PacketBuilder roomList(List<MultiModeRoom> roomList){
        packet.setRoomList(roomList);
        return this;
    }
    public PacketBuilder groupHistoryId(long groupHistoryId){
        packet.setGroupHistoryId(groupHistoryId);
        return this;
    }

    public PacketBuilder temp(int temp){
        packet.setTemp(temp);
        return this;
    }

    public Packet getPacket(){
        return packet;
    }
}
