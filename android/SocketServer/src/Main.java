import MultiMode.MultiModeUser;
import MultiMode.Protocol;

import java.util.ArrayList;
import java.util.List;

public class Main {
    static final int CREATE_ROOM = 1;
    static final int ENTER_ROOM = 2;
    static final int EXIT_ROOM = 3;
    public static void main(String[] args) {
        List<MultiModeUser> multiModeUserList = new ArrayList<>();
        multiModeUserList.add(new MultiModeUser(0, "Jack"));
        multiModeUserList.add(new MultiModeUser(1, "Alice"));
        multiModeUserList.add(new MultiModeUser(2, "John"));
        multiModeUserList.add(new MultiModeUser(3, "Bob"));

        // 서버와 클라이언트를 별도의 스레드에서 실행
        Thread serverThread = new Thread(() -> {
            Server server = new Server();
        });

        Thread clientThread1 = new Thread(() -> {
            Client client = new Client(multiModeUserList.get(0), Protocol.CREATE_ROOM);
        });

        Thread clientThread2 = new Thread(() -> {
            try {
                Thread.sleep(2000); // 2초 대기
                Client client = new Client(multiModeUserList.get(1), Protocol.ENTER_ROOM);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread clientThread3 = new Thread(() -> {
            Client client;
            try {
                Thread.sleep(4000); // 4초 대기
                client = new Client(multiModeUserList.get(2), Protocol.CREATE_ROOM);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread clientThread4 = new Thread(() -> {
            try {
                Thread.sleep(6000); // 6초 대기
                Client client = new Client(multiModeUserList.get(3), Protocol.ENTER_ROOM);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread clientThread5 = new Thread(() -> {
            try {
                Thread.sleep(8000); // 8초 대기
                Client client = new Client(multiModeUserList.get(3), Protocol.EXIT_ROOM);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        serverThread.start();
        clientThread1.start();
        clientThread2.start();
        clientThread3.start();
        clientThread4.start();
        clientThread5.start();
    }
}
//
//public class Main {
//    public static void main(String[] args) {
//        // 서버와 클라이언트를 별도의 스레드에서 실행
//        Thread serverThread = new Thread(() -> {
//            Server server = new Server();
//        });
//
//        Thread clientThread1 = new Thread(() -> {
//            Client client = new Client("Jack", "1234");
//        });
//
//        Thread clientThread2 = new Thread(() -> {
//            Client client = new Client("John", "5678");
//        });
//
//
//        serverThread.start();
//        clientThread1.start();
//        clientThread2.start();
//
//    }
//}