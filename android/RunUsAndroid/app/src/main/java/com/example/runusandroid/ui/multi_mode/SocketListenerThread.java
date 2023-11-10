package com.example.runusandroid.ui.multi_mode;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.runusandroid.R;

import org.json.JSONException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.List;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.Protocol;
import MultiMode.UserDistance;

public class SocketListenerThread extends Thread implements Serializable { // 소켓이 연결되어 있을 때 서버로부터 오는 이벤트를 캐치하기 위해 사용
    private Handler handler;
    private final ObjectInputStream ois;
    private MultiModeRoom selectedRoom;
    private transient MultiModeFragment multiModeFragment = null;;
    private transient MultiModeWaitFragment waitFragment = null;
    private transient MultiModePlayFragment playFragment = null;
    private transient MultiModeResultFragment resultFragment = null;
    private boolean isPaused = false; // 일시 중지 상태 관리 플래그

    public SocketListenerThread(MultiModeWaitFragment waitFragment, Handler handler, MultiModeRoom selectedRoom, ObjectInputStream ois) {
        this.handler = handler;
        this.ois = ois;
        this.waitFragment = waitFragment;
        this.selectedRoom = selectedRoom;
    }
    public SocketListenerThread(ObjectInputStream ois) {
        this.ois = ois;
    }

    public void pauseListening() {
        isPaused = true;
    }

    public void resumeListening() {
        isPaused = false;
    }
    public void setRoom(MultiModeRoom room) {
        this.selectedRoom = room;
    }
    public void addHandler(Handler handler) {
        this.handler = handler;
    }
    public void addMultiModeFragment(MultiModeFragment multiModeFragment) {
        this.multiModeFragment = multiModeFragment;
    }
    public void addWaitFragment(MultiModeWaitFragment waitFragment) {
        this.waitFragment = waitFragment;
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

                if (receivedObject instanceof Packet) {
                    Packet packet = (Packet) receivedObject;

                    if(packet.getProtocol() == Protocol.ROOM_LIST){
                        handler.post(() -> {
                            List<MultiModeRoom> roomList = packet.getRoomList();
                            multiModeFragment.setAdapter(roomList);
                            Log.d("roomlist ", "roomlist: "+roomList);
                        });
                    } else if(packet.getProtocol() == Protocol.ENTER_ROOM){
                        handler.post(() -> {
                            selectedRoom = packet.getSelectedRoom();
                            multiModeFragment.navigateRoomWait(selectedRoom);
                        });
                    } else if (packet.getProtocol() == Protocol.CREATE_ROOM){
                        handler.post(() -> {
                            selectedRoom = packet.getSelectedRoom();
                            multiModeFragment.navigateRoomWait(selectedRoom);
                        });
                    } else if (packet.getProtocol() == Protocol.EXIT_ROOM ||
                            packet.getProtocol() == Protocol.UPDATE_ROOM) {
                        handler.post(() -> {
                            selectedRoom = packet.getSelectedRoom();
                            waitFragment.setRoom(selectedRoom);
                            MultiModeUser user = packet.getUser();
                            if (packet.getProtocol() == Protocol.UPDATE_ROOM) {
                                waitFragment.addUserNameToWaitingList(user.getNickName());
                            } else {
                                waitFragment.removeUserNameFromWaitingList(user.getNickName());
                            }
                            Log.d("event", "user list: " + selectedRoom.getUserList());
                            waitFragment.updateParticipantCount(selectedRoom.getUserSize(), selectedRoom.getNumRunners());
                        });
                    } else if (packet.getProtocol() == Protocol.START_GAME) {
                        handler.post(() -> {
                            Log.d("start game", "start game packet come");
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("room", selectedRoom);
                            bundle.putSerializable("startTime", LocalDateTime.now());
                            NavController navController = Navigation.findNavController(waitFragment.requireView());
                            navController.navigate(R.id.navigation_multi_room_play, bundle);
                        });
                    } else if (packet.getProtocol() == Protocol.UPDATE_TOP3_STATES) {
                        handler.post(() -> {
                            List<UserDistance> temp = packet.getListTop3UserDistance();
                            UserDistance[] userDistances = temp.toArray(new UserDistance[temp.size()]);
                            Log.d("response", "userDistances.length is " + userDistances.length);
                            for (int i = 0; i < userDistances.length; i++) {
                                Log.d("response", "user " + i + " : " + userDistances[i].getUser().getNickName() + " , distance : " + userDistances[i].getDistance());
                            }
                            playFragment.updateTop3UserDistance(userDistances);
                        });
                    } else if (packet.getProtocol() == Protocol.SAVE_GROUP_HISTORY) {
                        handler.post(() -> {
                            try {
                                List<UserDistance> temp = packet.getListTop3UserDistance();
                                UserDistance[] userDistances = temp.toArray(new UserDistance[temp.size()]);
                                playFragment.saveGroupHistoryData(userDistances);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else if (packet.getProtocol() == Protocol.CLOSE_GAME) {
                        handler.post(() -> {
                            playFragment.timeHandler.removeCallbacks(playFragment.timeRunnable);
                            playFragment.sendDataHandler.removeCallbacks(playFragment.sendDataRunnable);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("room", selectedRoom);
                            List<UserDistance> temp = packet.getListTop3UserDistance();
                            playFragment.userDistances = temp.toArray(new UserDistance[temp.size()]);
                            try {
                                playFragment.saveHistoryData(packet.getGroupHistoryId());
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else if (packet.getProtocol() == Protocol.CLOSED_ROOM_ERROR) {
                        handler.post(() -> {
                            Toast.makeText(multiModeFragment.getActivity(), "존재하지 않는 방입니다.", Toast.LENGTH_SHORT).show();
                            NavController navController = Navigation.findNavController(multiModeFragment.requireView());
                            navController.navigate(R.id.navigation_multi_mode);
                        });
                    } else if(packet.getProtocol() == Protocol.FULL_ROOM_ERROR){
                        handler.post(() -> {
                            Toast.makeText(multiModeFragment.getActivity(), "인원이 초과되었습니다.", Toast.LENGTH_SHORT).show();
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