import MultiMode.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
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


            // 모든 클라이언트에게 RoomList 패킷을 전달


            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("클라이언트 연결 접수됨...");
                System.out.println("[client] : " + socket.getInetAddress());

                // 클라이언트에게 응답을 보내기 위한 ObjectOutputStream 생성 및 저장
                oos = new ObjectOutputStream(socket.getOutputStream());
                clientOutputStreams.add(oos);

                // 새 클라이언트의 정보를 모든 다른 클라이언트에게 전송
                broadcastNewClientInfo(socket, clientOutputStreams);

                // 클라이언트와 통신
                Thread clientThread = new Thread(() -> {
                    handleClient(socket, oos);
                });
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
                            RoomCreateInfo roomCreateInfo = ((Packet) data).getRoomCreateInfo();
                            System.out.println("CREATE_ROOM Success" + " title : " + roomCreateInfo.getTitle() + "time : " + roomCreateInfo.getStartTime() + " distance : " + roomCreateInfo.getDistance());
                            RoomManager.createRoom(user, roomCreateInfo);
                            broadcastNewClientInfo(socket, clientOutputStreams);
                        } else if (((Packet) data).getProtocol() == Protocol.ENTER_ROOM) {
                            RoomManager.getRoom(((Packet) data).getSelectedRoom().getId()).enterUser(user);
                        } else if (((Packet) data).getProtocol() == Protocol.EXIT_ROOM) {
                            RoomManager.getRoom(userList.get(user.getId()).getRoom()).exitUser(user);
                        }
                    }else if(data instanceof String){
                        System.out.println((String) data);
                    }
                    List<MultiModeRoom> roomList = RoomManager.getRoomList();
                    for (int i = 1; i < RoomManager.roomCount(); i++) {
                        MultiModeRoom room = roomList.get(i);
                        System.out.println(room.getId() + " " + roomList.get(i).getRoomOwner().getNickName());
                        List<MultiModeUser> list = room.getUserList();
                        for (int j = 0; j < list.size(); j++) {
                            MultiModeUser user = list.get(j);
                            System.out.println(room.getId() + " " + user.getId() + " " + user.getNickName());
                        }
                    }
                } catch (EOFException e) {
                    System.out.println("클라이언트 연결 종료: " + socket.getInetAddress());
                    clientOutputStreams.removeIf(clientOOS -> clientOOS == oos);
                    break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void broadcastNewClientInfo(Socket newClient, List<ObjectOutputStream> outputStreams) {
        for (ObjectOutputStream oos : outputStreams) {
            if (oos != null) {
                try {
                    oos.writeObject(new Packet(Protocol.ROOM_LIST, RoomManager.getRoomList()));
                    //oos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}