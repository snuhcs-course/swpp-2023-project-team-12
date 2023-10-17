package com.example.runusandroid.ui.multi_mode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketManager {
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

    public void openSocket() throws IOException {
        if (socket == null || socket.isClosed()) {
            socket = new Socket("10.0.2.2", 5001);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
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

    public void closeSocket() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            oos = null;
            ois = null;
        }
    }
}
