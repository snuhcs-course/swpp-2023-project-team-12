import MultiMode.*;

import java.io.*;
import java.net.*;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public class Server {
    private List<ObjectOutputStream> allClientOutputStreams = new ArrayList<>();
    private RoomManager roomManager = new RoomManager();
    private ObjectOutputStream oos;
    private static final int PORT = 5001;
    private static final String SERVER_IP = "0.0.0.0";
    private static int num = 0;

    public Server() {
        try {
            InetSocketAddress address = new InetSocketAddress(SERVER_IP, PORT);
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(address);
            System.out.println("서버 가동됨");
            System.out.println("서버 IP 주소: " + getServerIPAddress());
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("클라이언트 연결 접수됨...");
                System.out.println("[client] : " + socket.getInetAddress());

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                allClientOutputStreams.add(oos);

                // Thread clientThread = new ClientThread(socket, oos);
                Thread clientThread = new Thread(() -> handleClient(socket, oos));
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getServerIPAddress() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "Unknown";
        }
    }

    private void handleClient(Socket socket, ObjectOutputStream oos) {
        MultiModeUser connectedUser = null;
        MultiModeUser user = null;
        PacketBuilder packetBuilder = null;
        try {
            InputStream is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            while (true) {
                try {
                    Object data = ois.readObject();

                    if (data instanceof Packet) {
                        connectedUser = ((Packet) data).getUser();
                        if (user == null)
                            user = connectedUser;
                        System.out.println("protocol : " + ((Packet) data).getProtocol());
                        if (user != null){
                            System.out.println("user : " + user.getNickName());
                            System.out.println("userID : " + user.getNickName());
                            System.out.println("userImageURL : " + user.getProfileImageUrl());
                        }


                        if (((Packet) data).getProtocol() == Protocol.ROOM_LIST) {
                            packetBuilder = new PacketBuilder().protocol(Protocol.ROOM_LIST).roomList(RoomManager.getRoomList());
                            Packet roomListPacket = packetBuilder.getPacket();
                            System.out.println("RoomList size is " + RoomManager.getRoomList().size());
//                            System.out.println("First RoomOwner Image: "
//                                    + RoomManager.getRoomList().getFirst().getOwner().getProfileImageUrl() != null
//                                            ? RoomManager.getRoomList().getFirst().getOwner().getProfileImageUrl()
//                                            : "null");
                            oos.reset();
                            oos.writeObject(roomListPacket);
                            oos.flush();
                            // broadcastPacketToAllUsers(roomListPacket); // 모든 사용자에게 패킷을 보내는 부분
                        } else if (((Packet) data).getProtocol() == Protocol.CREATE_ROOM) {
                            System.out.println("create_room request came");
                            RoomCreateInfo roomCreateInfo = ((Packet) data).getRoomCreateInfo();
                            MultiModeRoom selectedRoom = RoomManager.createRoom(user, roomCreateInfo, oos);
                            System.out.println(RoomManager.getRoomList().get(0).getOwner().getProfileImageUrl());
                            System.out.println(selectedRoom.toString());
                            packetBuilder = new PacketBuilder().protocol(Protocol.CREATE_ROOM).roomList(RoomManager.getRoomList()).selectedRoom(selectedRoom);
                            Packet createRoomPacket = packetBuilder.getPacket();

                            oos.reset();
                            oos.writeObject(createRoomPacket);
                            oos.flush();
                        } else if (((Packet) data).getProtocol() == Protocol.ENTER_ROOM) {
                            MultiModeRoom enteredRoom = RoomManager.getRoom(((Packet) data).getSelectedRoom().getId());
                            if (enteredRoom == null) {
                                packetBuilder = new PacketBuilder().protocol(Protocol.CLOSED_ROOM_ERROR);
                                Packet closedRoomPacket = packetBuilder.getPacket();
                                oos.reset();
                                oos.writeObject(closedRoomPacket);
                                oos.flush();
                            } else if (enteredRoom.isRoomFull()) {
                                packetBuilder = new PacketBuilder().protocol(Protocol.FULL_ROOM_ERROR);
                                Packet fullRoomPacket = packetBuilder.getPacket();
                                oos.reset();
                                oos.writeObject(fullRoomPacket);
                                oos.flush();
                            } else {
                                enteredRoom.enterUser(user);
                                packetBuilder = new PacketBuilder().protocol(Protocol.ENTER_ROOM).user(user).selectedRoom(enteredRoom);
                                Packet enterRoomPacket = packetBuilder.getPacket();
                                oos.reset();
                                oos.writeObject(enterRoomPacket);
                                oos.flush();
                                System.out.println(enteredRoom);
                                packetBuilder = new PacketBuilder().protocol(Protocol.UPDATE_ROOM).user(user).selectedRoom(enteredRoom);
                                Packet updateRoomPacket = packetBuilder.getPacket();
                                broadcastToRoomUsers(enteredRoom, updateRoomPacket);

                                enteredRoom.addOutputStream(oos);
                            }
                        } else if (((Packet) data).getProtocol() == Protocol.EXIT_ROOM) {
                            MultiModeRoom exitRoom = RoomManager.getRoom(((Packet) data).getSelectedRoom().getId());
                            int index = exitRoom.exitUser(user);
                            if (index != -1)
                                exitRoom.removeOutputStream(index);
                            // Packet exitRoomPacket = new Packet(Protocol.EXIT_ROOM,
                            // RoomManager.getRoomList(), user, exitRoom);
                            // oos.writeObject(exitRoomPacket);
                            // oos.flush();
                            packetBuilder = new PacketBuilder().protocol(Protocol.EXIT_ROOM).user(user).selectedRoom(exitRoom);
                            Packet exitRoomPacket = packetBuilder.getPacket();
                            broadcastToRoomUsers(exitRoom, exitRoomPacket);

                        } else if (((Packet) data).getProtocol() == Protocol.UPDATE_USER_DISTANCE) {
                            MultiModeRoom updateRoom = RoomManager.getInGameRoom(user.getRoom().getId());
                            Float distance = ((Packet) data).getDistance();
                            System.out.println("user " + ((Packet) data).getUser().getNickName()
                                    + "'s update distance is " + distance);
                            updateRoom.updateDistance(new UserDistance(user, distance));

                            if (updateRoom.canUpdate()) {
                                System.out.println("update top3 distance");
                                updateTop3Users(Protocol.UPDATE_TOP3_STATES, updateRoom);
                            }
                        } else if (((Packet) data).getProtocol() == Protocol.START_GAME) {
                            MultiModeRoom enteredRoom = RoomManager.getRoom(((Packet) data).getSelectedRoom().getId());
                            enteredRoom.startGame();
                            RoomManager.startRoom(enteredRoom);
                            packetBuilder = new PacketBuilder().protocol(Protocol.START_GAME).selectedRoom(enteredRoom);
                            Packet startGamePacket = packetBuilder.getPacket();
                            broadcastToRoomUsers(enteredRoom, startGamePacket);
                        } else if (((Packet) data).getProtocol() == Protocol.EXIT_GAME) {
                            MultiModeRoom exitRoom = RoomManager
                                    .getInGameRoom(((Packet) data).getSelectedRoom().getId());
                            System.out.println(
                                    "EXIT_GAME packet received from " + user.getId() + user.getNickName() + "\n");
                            int index = exitRoom.exitUser(user);
                            if (index != -1)
                                exitRoom.removeOutputStream(index);
                        } else if (((Packet) data).getProtocol() == Protocol.FINISH_GAME) {
                            MultiModeRoom finishRoom = RoomManager
                                    .getInGameRoom(((Packet) data).getSelectedRoom().getId());
                            System.out.println("!!!!!!!!FINISH_GAME packet received from " + user.getId()
                                    + user.getNickName() + "\n");
                            finishRoom.addFinishCount(user);
                            if (finishRoom.checkGameFinished()) {
                                System.out.println("send packet");
                                System.out.println(finishRoom);
                                sendResultToRoomOwner(Protocol.SAVE_GROUP_HISTORY, finishRoom);
                            }
                        } else if (((Packet) data).getProtocol() == Protocol.SAVE_GROUP_HISTORY) {
                            MultiModeRoom finishRoom = RoomManager
                                    .getInGameRoom(((Packet) data).getSelectedRoom().getId());
                            System.out.println("!!!!!!!!got saved group history packet " + user.getId()
                                    + user.getNickName() + "\n");
                            System.out.println(((Packet) data).getGroupHistoryId());
                            sendResultTop3Users(Protocol.CLOSE_GAME, finishRoom, ((Packet) data).getGroupHistoryId());
                            RoomManager.removeInGameRoom(finishRoom);
                        }
                    }

                    else if (data instanceof String) {
                        System.out.println((String) data);
                    }

                    printRoomListInfo(RoomManager.getRoomList());

                } catch (SocketException | EOFException e) {
                    System.out.println("클라이언트 연결 종료: " + socket.getInetAddress());
                    if (user != null && user.getRoom() != null) {
                        System.out.println("user room : " + user.getRoom());
                        MultiModeRoom exitRoom = RoomManager.getRoom(user.getRoom().getId());
                        int index = exitRoom.exitUser(user);
                        if (index != -1)
                            exitRoom.removeOutputStream(index);
                        packetBuilder = new PacketBuilder().protocol(Protocol.EXIT_ROOM).user(user).selectedRoom(exitRoom);
                        Packet exitRoomPacket = packetBuilder.getPacket();
                        broadcastToRoomUsers(exitRoom, exitRoomPacket);
                    }
                    allClientOutputStreams.removeIf(clientOOS -> clientOOS == oos);
                    break;
                }
            }
        } catch (SocketException e) {
            MultiModeUser currentUser = null;
            MultiModeRoom exitRoom = null;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            cleanupClientResources(oos, socket); // Cleanup resources if there's an error
        } finally { // 유저가 경기 방에 있다가 서버와의 연결이 갑자기 끊겼을 때 유저를 방에서 내보내는 코드
        }
    }

    private void updateTop3Users(int protocol, MultiModeRoom room) {
        UserDistance[] top3UserDistance = null;
        while (true) {
            top3UserDistance = room.getTop3UserDistance();
            if (top3UserDistance != null) {
                break;
            }
        }
        /*
         * for(int i = 0; i < top3UserDistance.length; i++){
         * System.out.println("user " + i + " : " +
         * top3UserDistance[0].getUser().getNickName() + " , distance : " +
         * top3UserDistance[0].getDistance());
         * }
         */
        List<UserDistance> lTop3UserDistances = new ArrayList<UserDistance>(Arrays.asList(top3UserDistance));
        PacketBuilder packetBuilder = new PacketBuilder().protocol(protocol).listTop3UserDistance(lTop3UserDistances).temp(0);
        Packet updateTop3Packet = packetBuilder.getPacket();
        broadcastToRoomUsers(room, updateTop3Packet);
    }

    private void sendResultToRoomOwner(int protocol, MultiModeRoom room) throws IOException {
        UserDistance[] top3UserDistance = null;
        while (true) {
            top3UserDistance = room.getResultTop3UserDistances();
            if (top3UserDistance != null) {
                break;
            }
        }
        List<UserDistance> lTop3UserDistances = new ArrayList<UserDistance>(Arrays.asList(top3UserDistance));
        System.out.println(lTop3UserDistances);
        PacketBuilder packetBuilder = new PacketBuilder().protocol(protocol).listTop3UserDistance(lTop3UserDistances).temp(0);
        Packet updateTop3Packet = packetBuilder.getPacket();

        ObjectOutputStream oos = room.getRoomOwnerOos();
        if (oos != null) {
            // && oos != findOutputStreamByUser(packet.getUser())
            // 방에 들어오거나 나가는 유저가 아닌 다른 유저들한테만 패킷을 보내기 위한 조건
            try {
                oos.reset();
                oos.writeObject(updateTop3Packet);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendResultTop3Users(int protocol, MultiModeRoom room, long groupHistoryId) {
        UserDistance[] top3UserDistance = null;
        while (true) {
            top3UserDistance = room.getResultTop3UserDistances();
            if (top3UserDistance != null) {
                break;
            }
        }

        for (int i = 0; i < top3UserDistance.length; i++) {
            System.out.println("user " + i + " : " + top3UserDistance[i].getUser().getNickName() + " , distance : "
                    + top3UserDistance[i].getDistance());
        }
        List<UserDistance> lTop3UserDistances = new ArrayList<UserDistance>(Arrays.asList(top3UserDistance));
        PacketBuilder packetBuilder = new PacketBuilder().protocol(protocol).listTop3UserDistance(lTop3UserDistances).groupHistoryId((int) groupHistoryId);
        Packet updateTop3Packet = packetBuilder.getPacket();
        System.out.println((int) groupHistoryId);
        broadcastToRoomUsers(room, updateTop3Packet);
    }

    private void broadcastPacketToAllUsers(Packet packet) {
        synchronized (allClientOutputStreams) {
            for (ObjectOutputStream oos : allClientOutputStreams) {
                try {
                    oos.reset();
                    oos.writeObject(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void broadcastToRoomUsers(MultiModeRoom room, Packet packet) {

        List<ObjectOutputStream> oosList = room.getOutputStream();
        System.out.println("numbers of oos list : " + oosList.size());
        if (packet.getProtocol() == Protocol.UPDATE_TOP3_STATES) {
            for (int i = 0; i < packet.getListTop3UserDistance().size(); i++) {
                System.out.println("user " + i + " : " + packet.getListTop3UserDistance().get(i).getUser().getNickName()
                        + " , distance : " + packet.getListTop3UserDistance().get(i).getDistance());
            }
        } else if (packet.getProtocol() == Protocol.CLOSE_GAME) {
            // System.out.println("!!!send close room packet " + packet.getProtocol() + "
            // 1st user is " + packet.getTop3UserDistance()[0].getUser().getNickName());
            System.out.println(packet.getGroupHistoryId());
        }
        for (int i = 0; i < oosList.size(); i++) {
            ObjectOutputStream oos = oosList.get(i);
            if (oos != null) {
                // && oos != findOutputStreamByUser(packet.getUser())
                // 방에 들어오거나 나가는 유저가 아닌 다른 유저들한테만 패킷을 보내기 위한 조건
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

    private void cleanupClientResources(ObjectOutputStream oos, Socket socket) {
        try {
            allClientOutputStreams.remove(oos);
            oos.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printRoomListInfo(List<MultiModeRoom> roomList) {
        for (MultiModeRoom room : roomList) {
            printRoomInfo(room);
        }
    }

    private void printRoomInfo(MultiModeRoom room) {
        if (room == null) {
            System.out.println("room is null");
            return;
        } else {
            List<MultiModeUser> userList = room.getUserList();
            System.out.println(room.getTitle());
            for (MultiModeUser user : userList) {
                System.out.println("username : " + user.getNickname());
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}