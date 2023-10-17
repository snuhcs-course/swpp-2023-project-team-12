import MultiMode.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class Server {
    private List<ObjectOutputStream> clientOutputStreams = new ArrayList<>();
    private List<MultiModeUser> userList = new ArrayList<>();
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
                    clientOutputStreams.add(oos);
                    broadcastNewClientInfo();

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
        try {
            InputStream is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            while (true) {
                try {
                    Object data = ois.readObject();

                    if (data instanceof Packet) {
                        MultiModeUser user = ((Packet) data).getUser();
                        System.out.println(user.getNickName() +  ((Packet) data).getProtocol());
                        userList.add(user);
                        if (((Packet) data).getProtocol() == Protocol.ROOM_LIST) {
                            Packet roomListPacket = new Packet(Protocol.ROOM_LIST, RoomManager.getRoomList());
                            System.out.println("RoomList size is "  + RoomManager.getRoomList().size());
                            for (ObjectOutputStream clientOOS : clientOutputStreams) {
                                if (clientOOS != oos) {
                                    try {
                                        clientOOS.writeObject(roomListPacket);
                                        clientOOS.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else if (((Packet) data).getProtocol() == Protocol.CREATE_ROOM) {
                            System.out.println("create_room request came");
                            RoomCreateInfo roomCreateInfo = ((Packet) data).getRoomCreateInfo();
                            System.out.println("CREATE_ROOM Success" + " title : " + roomCreateInfo.getTitle() + "time : " + roomCreateInfo.getStartTime() + " distance : " + roomCreateInfo.getDistance());
                            MultiModeRoom selectedRoom = RoomManager.createRoom(user, roomCreateInfo);
                            System.out.println(selectedRoom.toString());
                            Packet createRoomPacket = new Packet(Protocol.CREATE_ROOM, RoomManager.getRoomList(), selectedRoom);
                            oos.writeObject(createRoomPacket);
                            oos.flush();
                            //broadcastNewClientInfo(socket, clientOutputStreams);
                        } else if (((Packet) data).getProtocol() == Protocol.ENTER_ROOM) {
                            RoomManager.getRoom(((Packet) data).getSelectedRoom().getId()).enterUser(user);
                            Packet enterRoomPacket = new Packet(Protocol.ENTER_ROOM, RoomManager.getRoomList(), RoomManager.getRoom(((Packet) data).getSelectedRoom().getId()));
                            oos.writeObject(enterRoomPacket);
                            oos.flush();
                        } else if (((Packet) data).getProtocol() == Protocol.EXIT_ROOM) {
                            RoomManager.getRoom(userList.get(user.getId()).getRoom().getId()).exitUser(user);
                            Packet exitRoomPacket = new Packet(Protocol.EXIT_ROOM, RoomManager.getRoomList());
                            oos.writeObject(exitRoomPacket);
                            oos.flush();
                        }
                    }else if(data instanceof String){
                        System.out.println((String) data);
                    }
                } catch (EOFException e) {
                    System.out.println("클라이언트 연결 종료: " + socket.getInetAddress());
                    clientOutputStreams.removeIf(clientOOS -> clientOOS == oos);
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            cleanupClientResources(oos, socket);  // Cleanup resources if there's an error

        }
    }

    private void broadcastNewClientInfo() {
        MultiModeRoom room = new MultiModeRoom();
        room.setId(100);
        Packet packet = new Packet(Protocol.ROOM_LIST, RoomManager.getRoomList(), room);

        synchronized (clientOutputStreams) {
            for (ObjectOutputStream oos : clientOutputStreams) {
                try {
                    oos.writeObject(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void cleanupClientResources(ObjectOutputStream oos, Socket socket) {
        try {
            clientOutputStreams.remove(oos);
            oos.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}