package com.example.runusandroid.ui.multi_mode;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.ObjectInputStream;

import MultiMode.Packet;

public class SocketListenerThread extends Thread {
    private final Handler handler;
    private final SocketManager socketManager;

    public SocketListenerThread(Handler handler) {
        this.handler = handler;
        this.socketManager = SocketManager.getInstance();
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ObjectInputStream ois = socketManager.getOIS();
                Object receivedObject = ois.readObject();

                if (receivedObject instanceof Packet) {
                    Packet packet = (Packet) receivedObject;
                    Message msg = handler.obtainMessage();
                    msg.obj = packet;
                    handler.sendMessage(msg);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

