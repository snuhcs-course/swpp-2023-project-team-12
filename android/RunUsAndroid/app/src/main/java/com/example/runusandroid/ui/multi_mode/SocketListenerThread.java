package com.example.runusandroid.ui.multi_mode;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;

public class SocketListenerThread extends Thread { // 소켓이 연결되어 있을 때 서버로부터 오는 이벤트를 캐치하기 위해 사용
    private final Handler handler;
    private final SocketManager socketManager;
    private final MultiModeWaitFragment waitFragment;
    private MultiModeRoom selectedRoom;

    public SocketListenerThread(MultiModeWaitFragment waitFragment, Handler handler, MultiModeRoom selectedRoom) {
        this.handler = handler;
        this.socketManager = SocketManager.getInstance();
        this.waitFragment = waitFragment;
        this.selectedRoom = selectedRoom;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                ObjectInputStream ois = socketManager.getOIS();
                Object receivedObject = ois.readObject();

                if (receivedObject instanceof Packet) {
                    Packet packet = (Packet) receivedObject;

                    if(packet.getProtocol() == 5 || packet.getProtocol() == 4){
                        handler.post(new Runnable(){
                            @Override
                            public void run(){
                                MultiModeRoom room = packet.getSelectedRoom();
                                MultiModeUser user = packet.getUser();
                                if(packet.getProtocol() == 5) {
                                    selectedRoom.enterUser(user);
                                    waitFragment.addUserNameToWaitingList(user.getNickName());
                                }
                                else{
                                    selectedRoom.exitUser(user);
                                    waitFragment.removeUserNameFromWaitingList(user.getNickName());
                                }
                                Log.d("event", "user list: " +room.getUserList());
                                Log.d("event", "user num: " +room.getUserSize());
                                waitFragment.updateParticipantCount(room.getUserSize(), room.getNumRunners());

                            }
                        });
                    }

                     /*
                    if(packet.getProtocol() == 5){
                        handler.post(new Runnable(){
                            @Override
                            public void run(){
                                MultiModeRoom room = packet.getSelectedRoom();
                                MultiModeUser user = packet.getUser();
                                selectedRoom.enterUser(user);
                                waitFragment.addUserNameToWaitingList(user.getNickName());
                                waitFragment.updateParticipantCount(room.getUserSize(), room.getNumRunners());
                            }
                        });
                    }

                      */

                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}