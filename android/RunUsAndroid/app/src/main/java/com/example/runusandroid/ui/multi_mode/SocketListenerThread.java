package com.example.runusandroid.ui.multi_mode;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.ObjectInputStream;

import MultiMode.Packet;

public class SocketListenerThread extends Thread { // 소켓이 연결되어 있을 때 서버로부터 오는 이벤트를 캐치하기 위해 사용
    private final Handler handler;
    private final SocketManager socketManager;

    public SocketListenerThread(Handler handler) {
        this.handler = handler;
        this.socketManager = SocketManager.getInstance();
    }

    @Override
    public void run() {
//        try {
//            while (!Thread.currentThread().isInterrupted()) {
//                ObjectInputStream ois = socketManager.getOIS();
//                Object receivedObject = ois.readObject();
//
//                if (receivedObject instanceof Packet) {
//                    Packet packet = (Packet) receivedObject;
//                    Message msg = handler.obtainMessage();
//                    msg.obj = packet;
//                    handler.sendMessage(msg);
//                }
//            }
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
    }
}

