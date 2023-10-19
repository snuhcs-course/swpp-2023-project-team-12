package com.example.runusandroid.ui.multi_mode;

import android.util.Log;

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

    public void openSocket() throws IOException { //소켓 열기
        if (socket == null || socket.isClosed()) {
            socket = new Socket("172.20.10.3", 5001);
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
        Log.d("response closeSocket", "close start");
        if (socket != null && !socket.isClosed()) {
            Log.d("response closeSocket", "close success");

            socket.close();
            oos = null;
            ois = null;
            socket = null;
        }else{
            Log.d("response closeSocket", "close failed");

        }
    }
}