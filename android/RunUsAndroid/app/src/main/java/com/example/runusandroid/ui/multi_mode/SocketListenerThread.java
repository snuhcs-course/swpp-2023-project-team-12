package com.example.runusandroid.ui.multi_mode;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.runusandroid.R;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.SocketException;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.Protocol;
import MultiMode.UserDistance;

public class SocketListenerThread extends Thread implements Serializable { // 소켓이 연결되어 있을 때 서버로부터 오는 이벤트를 캐치하기 위해 사용
    private final Handler handler;
    private final ObjectInputStream ois;
    private final MultiModeRoom selectedRoom;
    private MultiModeWaitFragment waitFragment = null;
    private MultiModePlayFragment playFragment = null;
    private boolean isPaused = false; // 일시 중지 상태 관리 플래그

    public SocketListenerThread(MultiModeWaitFragment waitFragment, Handler handler, MultiModeRoom selectedRoom, ObjectInputStream ois) {
        this.handler = handler;
        this.ois = ois;
        this.waitFragment = waitFragment;
        this.selectedRoom = selectedRoom;

        Log.d("response", "waitFragment is " + waitFragment);
        Log.d("response", "playFragment is " + playFragment);

    }

    public void pauseListening() {
        isPaused = true;
    }

    public void resumeListening() {
        isPaused = false;
    }

    public void addPlayFragment(MultiModePlayFragment playFragment) {
        this.playFragment = playFragment;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (isPaused) {
                    // 일시 중지 상태이면 대기
                    Thread.sleep(1000); // 예: 1초마다 체크
                    continue;
                }
                Object receivedObject;
                synchronized (ois) {
                    receivedObject = ois.readObject();
                }
                Log.d("response", "getReceivedObject " + receivedObject);

                if (receivedObject instanceof Packet) {
                    Packet packet = (Packet) receivedObject;

                    if (packet.getProtocol() == 5 || packet.getProtocol() == 4) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                MultiModeRoom room = packet.getSelectedRoom();
                                MultiModeUser user = packet.getUser();
                                if (packet.getProtocol() == 5) {
                                    selectedRoom.enterUser(user);
                                    waitFragment.addUserNameToWaitingList(user.getNickName());
                                } else {
                                    selectedRoom.exitUser(user);
                                    waitFragment.removeUserNameFromWaitingList(user.getNickName());
                                }
                                Log.d("event", "user list: " + room.getUserList());
                                Log.d("event", "user num: " + room.getUserSize());
                                waitFragment.updateParticipantCount(room.getUserSize(), room.getNumRunners());
                            }
                        });
                    } else if (packet.getProtocol() == Protocol.START_GAME) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("room", selectedRoom);
                                NavController navController = Navigation.findNavController(waitFragment.requireView());
                                navController.navigate(R.id.navigation_multi_room_play, bundle);
                            }
                        });
                    } else if (packet.getProtocol() == Protocol.UPDATE_TOP3_STATES) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                UserDistance[] userDistances = packet.getTop3UserDistance();
                                if (userDistances == null) {
                                    Log.d("response", "userDistances.length is null");
                                } else {
                                    Log.d("response", "userDistances.length is " + userDistances.length);

                                    for (int i = 0; i < userDistances.length; i++) {

                                        Log.d("response", "user " + i + " : " + userDistances[0].getUser().getNickName() + " , distance : " + userDistances[0].getDistance());
                                    }
                                    Log.d("response", "playFragment is " + playFragment);
                                    Log.d("response", "waitFragment is " + waitFragment);
                                    playFragment.updateTop3UserDistance(userDistances);
                                }

                            }
                        });
                    }
                }
            }
        } catch (SocketException e) {
            Log.d("Socket manager", "connection finished");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}