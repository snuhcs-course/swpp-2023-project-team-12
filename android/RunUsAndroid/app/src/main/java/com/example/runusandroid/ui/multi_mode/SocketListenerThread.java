package com.example.runusandroid.ui.multi_mode;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.runusandroid.R;

import org.json.JSONException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.SocketException;
import java.time.LocalDateTime;

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
    private MultiModeResultFragment resultFragment = null;
    private boolean isPaused = false; // 일시 중지 상태 관리 플래그

    public SocketListenerThread(MultiModeWaitFragment waitFragment, Handler handler, MultiModeRoom selectedRoom, ObjectInputStream ois) {
        this.handler = handler;
        this.ois = ois;
        this.waitFragment = waitFragment;
        this.selectedRoom = selectedRoom;
        Log.d("response", "waitFragment is " + this.waitFragment);
        Log.d("response", "playFragment is " + this.playFragment);

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

    public void addResultFragment(MultiModeResultFragment resultFragment) {
        this.resultFragment = resultFragment;
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

                                if (selectedRoom.getOwner().getId() == user.getId()) { //만약 기존 방장이 방을 나가는 경우 방장 변경
                                    selectedRoom.setRoomOwner(room.getRoomOwner());
                                    waitFragment.startGame();
                                }

                            }
                        });
                    } else if (packet.getProtocol() == Protocol.START_GAME) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("room", selectedRoom);
                                bundle.putSerializable("socketListenerThread", SocketListenerThread.this);
                                bundle.putSerializable("startTime", LocalDateTime.now());
                                NavController navController = Navigation.findNavController(waitFragment.requireView());
                                Log.d("response", "goto play screen");
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
                    } else if (packet.getProtocol() == Protocol.SAVE_GROUP_HISTORY) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Log.d("response", "got packet and try to save group history");
                                    playFragment.saveGroupHistoryData(packet.getTop3UserDistance());
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

                    } else if (packet.getProtocol() == Protocol.CLOSE_GAME) {
                        Log.d("response", "got close game packet!!!!!");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                playFragment.timeHandler.removeCallbacks(playFragment.timeRunnable);
                                playFragment.sendDataHandler.removeCallbacks(playFragment.sendDataRunnable);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("room", selectedRoom);
                                bundle.putSerializable("socketListenerThread", SocketListenerThread.this);
                                bundle.putSerializable("top3UserDistance", packet.getTop3UserDistance());
                                bundle.putSerializable("userDistance", playFragment.distance);
                                Log.d("response", "go to room result screen");
                                try {
                                    playFragment.saveHistoryData(packet.getGroupHistoryId());
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                                NavController navController = Navigation.findNavController(playFragment.requireView());
                                navController.navigate(R.id.navigation_multi_room_result, bundle);
                                Log.d("response", packet.getTop3UserDistance() + " ");
                                Log.d("response", resultFragment + "");

//                                resultFragment.updateTop3UserDistance(packet.getTop3UserDistance());
                                try {
                                    playFragment.socketManager.closeSocket();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
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