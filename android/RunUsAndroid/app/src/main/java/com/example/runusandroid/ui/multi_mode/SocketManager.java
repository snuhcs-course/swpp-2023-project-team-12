package com.example.runusandroid.ui.multi_mode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketManager { // 모든 fragment에서 공통의 소켓을 활용하기 위해 필요한 class
    private static SocketManager instance;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private SocketManager() {
        // 생성자에서는 소켓을 직접 열지 않습니다.
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public synchronized SocketManager resetInstance() throws IOException {
        socket = null;
        openSocket();
        return instance;
    }

    public void openSocket() throws IOException { //소켓 열기
        if (socket == null || socket.isClosed()) {
            //socket = new Socket("192.168.0.4", 5001);
            //socket = new Socket("10.0.2.2", 5001);
            socket = new Socket("ec2-3-36-116-64.ap-northeast-2.compute.amazonaws.com", 5001);
            System.out.println("open socket");

            oos = new ObjectOutputStream(socket.getOutputStream()); //서버로 보내는 바이트스트림을 직렬화 하기 위해 사용
            ois = new ObjectInputStream(socket.getInputStream()); //서버로부터 받는 바이트스트림을 역직렬화 하기 위해 사용
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectOutputStream getOOS() {
        return oos;
    }

    public ObjectInputStream getOIS() {
        return ois;
    }

    public void closeSocket() throws IOException { //소켓 닫기
        //Log.d("response closeSocket", "close start");
        if (socket != null && !socket.isClosed()) {
            //Log.d("response closeSocket", "close success");

            socket.close();
            oos = null;
            ois = null;
            socket = null;
        } else {
            //Log.d("response closeSocket", "close failed");

        }
    }
}
