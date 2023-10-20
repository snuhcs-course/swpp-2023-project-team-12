package com.example.runusandroid.ui.multi_mode;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import MultiMode.MultiModeUser;
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
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ObjectInputStream ois = socketManager.getOIS();
                Object receivedObject = ois.readObject();

                if (receivedObject instanceof Packet) {
                    Packet packet = (Packet) receivedObject;
                    Log.d("response", "got new packet from server" );
                    Log.d("response", "protocol is " + packet.getProtocol());
                    if(packet.getProtocol() == 5){
                        Log.d("response", "broadcastToRoomUsers : " + packet.getSelectedRoom().getUserList().size());
                        List<MultiModeUser> userList = packet.getSelectedRoom().getUserList();
                        for(MultiModeUser user : userList){
                            Log.d("response", "broadcastToRoomUser " + user.getNickName());
                        }
                        Message msg = handler.obtainMessage();
                        msg.obj = packet;
                        handler.sendMessage(msg);
                    }

                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

