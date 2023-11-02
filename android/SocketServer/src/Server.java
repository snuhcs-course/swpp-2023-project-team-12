import MultiMode.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class Server {
    private List<ObjectOutputStream> allClientOutputStreams = new ArrayList<>();
//    private List<MultiModeUser> userList = new ArrayList<>();
    private RoomManager roomManager = new RoomManager();
    private ObjectOutputStream oos;
    private static final int PORT = 5001;
    private static final String SERVER_IP = "0.0.0.0";

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
        try {
            InputStream is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            while (true) {
                try {
                    Object data = ois.readObject();

                    if (data instanceof Packet) {
                        connectedUser = ((Packet) data).getUser();
                        user = connectedUser;
//                        int userInUserList = 0;
//                        if(connectedUser != null){
//                            for(MultiModeUser multiModeUser : userList){
//                                if(multiModeUser.getId() == connectedUser.getId()){
//                                    user = multiModeUser;
//                                    userInUserList = 1;
//                                    break;
//                                }
//                            }
//                            userList.add(user);
//                            System.out.println(user.getNickName());
//                        }
//                        if(userInUserList == 0){
//                            userList.add(user);
//                        }
                        System.out.println("protocol : "+((Packet) data).getProtocol());

                        if (((Packet) data).getProtocol() == Protocol.ROOM_LIST) {
                            Packet roomListPacket = new Packet(Protocol.ROOM_LIST, RoomManager.getRoomList());
                            System.out.println("RoomList size is "  + RoomManager.getRoomList().size());
                            broadcastPacketToAllUsers(roomListPacket);  // 모든 사용자에게 패킷을 보내는 부분
                        } else if (((Packet) data).getProtocol() == Protocol.CREATE_ROOM) {
                            System.out.println("create_room request came");
                            RoomCreateInfo roomCreateInfo = ((Packet) data).getRoomCreateInfo();
                            System.out.println("CREATE_ROOM Success" + " title : " + roomCreateInfo.getTitle() + "time : " + roomCreateInfo.getStartTime() + " distance : " + roomCreateInfo.getDistance());
                            MultiModeRoom selectedRoom = RoomManager.createRoom(user, roomCreateInfo, oos);
                            System.out.println(selectedRoom.toString());
                            Packet createRoomPacket = new Packet(Protocol.CREATE_ROOM, RoomManager.getRoomList(), selectedRoom);
                            oos.writeObject(createRoomPacket);
                            oos.flush();
                        } else if (((Packet) data).getProtocol() == Protocol.ENTER_ROOM) {
                            MultiModeRoom enteredRoom = RoomManager.getRoom(((Packet) data).getSelectedRoom().getId());
                            enteredRoom.enterUser(user);
                            System.out.println(enteredRoom);
                            Packet enterRoomPacket = new Packet(Protocol.ENTER_ROOM, RoomManager.getRoomList(), user, enteredRoom);
                            oos.writeObject(enterRoomPacket);
                            oos.flush();
                            broadcastToRoomUsers(enteredRoom, new Packet(Protocol.UPDATE_ROOM, user, enteredRoom));
                            enteredRoom.addOutputStream(oos);
                        } else if (((Packet) data).getProtocol() == Protocol.EXIT_ROOM) {
                            MultiModeRoom exitRoom = RoomManager.getRoom(((Packet) data).getSelectedRoom().getId());
                            int index = exitRoom.exitUser(user);
                            if(index != -1) exitRoom.removeOutputStream(index);
                            Packet exitRoomPacket = new Packet(Protocol.EXIT_ROOM, RoomManager.getRoomList(), user, exitRoom);
                            oos.writeObject(exitRoomPacket);
                            oos.flush();
                            broadcastToRoomUsers(exitRoom, new Packet(Protocol.EXIT_ROOM, user, exitRoom));
                        } else if(((Packet) data).getProtocol() == Protocol.UPDATE_USER_DISTANCE){
                            MultiModeRoom updateRoom = RoomManager.getRoom(user.getRoom().getId());
                            Float distance = ((Packet) data).getDistance();
                            System.out.println("user " + ((Packet) data).getUser().getNickName() + "'s update distance is " + distance);
                            updateRoom.updateDistance(new UserDistance(user, distance));


                            if(updateRoom.canUpdate()){
                                System.out.println("update top3 distance : " + updateRoom);
                                updateTop3Users(Protocol.UPDATE_TOP3_STATES, updateRoom);
                            }
                        } else if (((Packet) data).getProtocol() == Protocol.START_GAME) {
                            MultiModeRoom enteredRoom = RoomManager.getRoom(((Packet) data).getSelectedRoom().getId());
                            enteredRoom.startGame();
                            broadcastToRoomUsers(enteredRoom, new Packet(Protocol.START_GAME, enteredRoom));
                        }else if(((Packet) data).getProtocol() == Protocol.EXIT_GAME){
                            MultiModeRoom exitRoom = RoomManager.getRoom(((Packet) data).getSelectedRoom().getId());
                            System.out.println("EXIT_GAME packet received from " + user.getId() + user.getNickName() + "\n\n\n\n");
                            int index = exitRoom.exitUser(user);
                            if(index != -1) exitRoom.removeOutputStream(index);
                        }else if(((Packet) data).getProtocol() == Protocol.FINISH_GAME){
                            MultiModeRoom finishRoom = RoomManager.getRoom(((Packet) data).getSelectedRoom().getId());
                            System.out.println("!!!!!!!!FINISH_GAME packet received from " + user.getId() + user.getNickName() + "\n\n\n\n");
                            finishRoom.addFinishCount(user);
                            if(finishRoom.checkGameFinished()){
                                System.out.println("send packet");
                                System.out.println(finishRoom);
                                sendResultToRoomOwner(Protocol.SAVE_GROUP_HISTORY, finishRoom);
                            }
                        }else if(((Packet) data).getProtocol() == Protocol.SAVE_GROUP_HISTORY){
                            MultiModeRoom finishRoom = RoomManager.getRoom(((Packet) data).getSelectedRoom().getId());
                            System.out.println("!!!!!!!!got saved group history packet " + user.getId() + user.getNickName() + "\n\n\n\n");
                            sendResultTop3Users(Protocol.CLOSE_GAME, finishRoom, ((Packet) data).getGroupHistoryId());
                            RoomManager.removeRoom(finishRoom);
                        }
                    }

                    else if(data instanceof String){
                        System.out.println((String) data);
                    }

                    printRoomListInfo(RoomManager.getRoomList());

                } catch (SocketException | EOFException e) {
                    System.out.println("클라이언트 연결 종료: " + socket.getInetAddress());
                    allClientOutputStreams.removeIf(clientOOS -> clientOOS == oos);
                    break;
                }
            }
        } catch (SocketException e) {
            MultiModeUser currentUser = null;
            MultiModeRoom exitRoom = null;
//            for(MultiModeUser muser : userList) {
//                if (user.getId() == muser.getId()) {
//                    currentUser = muser;
//                    exitRoom = currentUser.getRoom();
//                    break;
//                }
//            }
//
//            if (connectedUser != null && exitRoom != null) {
//                if(RoomManager.roomCount() != 0){
//                    exitRoom.exitUser(user);
//                    userList.remove(currentUser);
//                    broadcastToRoomUsers(exitRoom, new Packet(Protocol.EXIT_ROOM, user, exitRoom));
//                }
//            }
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            cleanupClientResources(oos, socket);  // Cleanup resources if there's an error
        } finally { //유저가 경기 방에 있다가 서버와의 연결이 갑자기 끊겼을 때 유저를 방에서 내보내는 코드
        }
    }

    private void updateTop3Users(int protocol, MultiModeRoom room){
        UserDistance[] top3UserDistance = null;
        System.out.println("here");
        while(true){
            top3UserDistance  = room.getTop3UserDistance();
            if(top3UserDistance != null){
                break;
            }
        }
        System.out.println("there");

        for(int i = 0; i < top3UserDistance.length; i++){
            System.out.println("user " + i + " : " + top3UserDistance[0].getUser().getNickName() + " , distance : " + top3UserDistance[0].getDistance());
        }
        Packet updateTop3Packet = new Packet(protocol, top3UserDistance);
        broadcastToRoomUsers(room, updateTop3Packet);
    }
    private void sendResultToRoomOwner(int protocol, MultiModeRoom room) throws IOException {
        UserDistance[] top3UserDistance = null;
        while(true){
            top3UserDistance  = room.getResultTop3UserDistances();
            if(top3UserDistance != null){
                break;
            }
        }
        Packet updateTop3Packet = new Packet(protocol, top3UserDistance);

        ObjectOutputStream oos = room.getRoomOwnerOos();
        if (oos != null ){
            //&& oos != findOutputStreamByUser(packet.getUser())
            // 방에 들어오거나 나가는 유저가 아닌 다른 유저들한테만 패킷을 보내기 위한 조건
            try {
                oos.writeObject(updateTop3Packet);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void sendResultTop3Users(int protocol, MultiModeRoom room, long groupHistoryId){
        UserDistance[] top3UserDistance = null;
        while(true){
            top3UserDistance  = room.getResultTop3UserDistances();
            if(top3UserDistance != null){
                break;
            }
        }

        for(int i = 0; i < top3UserDistance.length; i++){
            System.out.println("user " + i + " : " + top3UserDistance[0].getUser().getNickName() + " , distance : " + top3UserDistance[0].getDistance());
        }
        Packet updateTop3Packet = new Packet(protocol, top3UserDistance, groupHistoryId);
        broadcastToRoomUsers(room, updateTop3Packet);
    }

//    private void addNewUserToList(MultiModeUser newUser) {
//        userList.add(newUser);
//    }
    private void broadcastPacketToAllUsers(Packet packet) {
        synchronized (allClientOutputStreams) {
            for (ObjectOutputStream oos : allClientOutputStreams) {
                try {
                    oos.writeObject(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void broadcastToRoomUsers(MultiModeRoom room, Packet packet) {

        List<ObjectOutputStream> oosList = room.getOutputStream();
        System.out.println("numbers of oos list : "+oosList.size());
        if(packet.getProtocol() == Protocol.UPDATE_TOP3_STATES){
            System.out.println("send top3 packet " + packet.getProtocol() + " 1st user is " + packet.getTop3UserDistance()[0].getUser().getNickName());
        }else if(packet.getProtocol() == Protocol.CLOSE_GAME){
            System.out.println("!!!send close room packet " + packet.getProtocol() + " 1st user is " + packet.getTop3UserDistance()[0].getUser().getNickName());
        }
        for(int i=0; i<oosList.size(); i++){
            ObjectOutputStream oos = oosList.get(i);
            if (oos != null ){
                //&& oos != findOutputStreamByUser(packet.getUser())
                // 방에 들어오거나 나가는 유저가 아닌 다른 유저들한테만 패킷을 보내기 위한 조건
                try {
                    oos.writeObject(packet);
                    oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    private ObjectOutputStream findOutputStreamByUser(MultiModeUser user) {
//        int index = userList.indexOf(user);
//        if (index != -1 && index < allClientOutputStreams.size()) {
//            return allClientOutputStreams.get(index);
//        }
//        return null;
//    }




    private void cleanupClientResources(ObjectOutputStream oos, Socket socket) {
        try {
            allClientOutputStreams.remove(oos);
            oos.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void printRoomListInfo(List<MultiModeRoom> roomList){
        for(MultiModeRoom room : roomList){
            printRoomInfo(room);
        }
    }

    private void printRoomInfo(MultiModeRoom room){
        if(room == null){
            System.out.println("room is null");
            return;
        }else{
            List<MultiModeUser> userList = room.getUserList();
            System.out.println(room.getTitle());
            for(MultiModeUser user : userList){
                System.out.println("username : " + user.getNickname());
            }
            System.out.println();
        }
    }


    public static void main(String[] args) {
        new Server();
    }
}