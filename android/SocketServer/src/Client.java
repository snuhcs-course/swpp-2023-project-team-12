import MultiMode.MultiModeUser;
import MultiMode.Packet;

import java.io.*;
import java.net.Socket;

public class Client {
    public Client(MultiModeUser newUser, int request) {
        try {
            // 서버 IP 주소와 포트 번호로 연결
            Socket socket = new Socket("localhost", 5001);

            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();

            ObjectOutputStream oos = new ObjectOutputStream(os);
            ObjectInputStream ois = new ObjectInputStream(is);

            int dataType = request; // 예를 들어, 1은 MultiModeUser 객체를 나타낸다고 가정
            Packet packet = new Packet(dataType, newUser);
            oos.writeObject(packet);
            oos.flush();

            Object receivedObject;
            while (true) {
                receivedObject = ois.readObject();
                if (receivedObject instanceof MultiModeUser) {
                    MultiModeUser receivedMember = (MultiModeUser) receivedObject;
                    System.out.println("서버로부터의 응답: ID=" + receivedMember.getId() + ", Nickname=" + receivedMember.getNickName());
                } else if (receivedObject instanceof String) {
                    String message = (String) receivedObject;
                    System.out.println("서버로부터의 응답: " + message);
                } else {
                    // 다른 데이터 타입 처리
                }
            }
        } catch (IOException e) {
            System.out.println("오류");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("오류");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
//import java.io.*;
//        import java.net.Socket;
//
//public class Client {
//    public Client(String id, String pwd) {
//        try {
//            // 서버 IP 주소와 포트 번호로 연결
//            Socket socket = new Socket("localhost", 5001);
//
//            OutputStream os = socket.getOutputStream();
//            InputStream is = socket.getInputStream();
//
//            ObjectOutputStream oos = new ObjectOutputStream(os);
//            ObjectInputStream ois = new ObjectInputStream(is);
//
//            Member member = new Member();
//            member.setId(id);
//            member.setPwd(pwd);
//
//            oos.writeObject(member);
//            oos.flush();
//
//            Object receivedObject;
//            while (true) {
//                receivedObject = ois.readObject();
//                if (receivedObject instanceof Member) {
//                    Member receivedMember = (Member) receivedObject;
//                    System.out.println("서버로부터의 응답: ID=" + receivedMember.getId() + ", PWD=" + receivedMember.getPwd());
//                } else if (receivedObject instanceof String) {
//                    String message = (String) receivedObject;
//                    System.out.println("서버로부터의 응답: " + message);
//                } else {
//                    // 다른 데이터 타입 처리
//                }
//            }
//        } catch (IOException e) {
//            System.out.println("오류");
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            System.out.println("오류");
//            e.printStackTrace();
//        }
//    }
//
//    public static void main(String[] args) {
//
//    }
//}