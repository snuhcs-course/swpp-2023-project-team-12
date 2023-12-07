package MultiMode;

import java.util.List;

public class PacketBuilder {
    private final Packet packet;

    public PacketBuilder() {
        this.packet = new Packet();
    }

    public PacketBuilder buildProtocol(int protocol) {
        packet.setProtocol(protocol);
        return this;
    }

    public PacketBuilder buildTop3UserDistance(UserDistance[] top3UserDistance) {
        packet.setTop3UserDistance(top3UserDistance);
        return this;
    }

    public PacketBuilder buildListTop3UserDistance(List<UserDistance> listTop3UserDistance) {
        packet.setListTop3UserDistance(listTop3UserDistance);
        return this;
    }

    public PacketBuilder buildRoomCreateInfo(RoomCreateInfo roomCreateInfo) {
        packet.setRoomCreateInfo(roomCreateInfo);
        return this;
    }

    public PacketBuilder buildUser(MultiModeUser user) {
        packet.setUser(user);
        return this;
    }

    public PacketBuilder buildDistance(float distance) {
        packet.setDistance(distance);
        return this;
    }

    public PacketBuilder buildSelectedRoom(MultiModeRoom selectedRoom) {
        packet.setSelectedRoom(selectedRoom);
        return this;
    }

    public PacketBuilder buildRoomList(List<MultiModeRoom> roomList) {
        packet.setRoomList(roomList);
        return this;
    }

    public PacketBuilder buildGroupHistoryId(long groupHistoryId) {
        packet.setGroupHistoryId(groupHistoryId);
        return this;
    }
    

    public Packet getPacket() {
        return packet;
    }
}
