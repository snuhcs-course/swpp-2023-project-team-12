package com.example.runusandroid.ui.multi_mode;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.runusandroid.R;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.Protocol;
import MultiMode.UserDistance;

public class MultiModePlayFragment extends Fragment {

    SocketManager socketManager = SocketManager.getInstance();
    ObjectOutputStream oos;
    MultiModeRoom selectedRoom;
    //MultiModeUser user = new MultiModeUser(1, "choco");
    MultiModeUser user = new MultiModeUser(2, "berry"); // 유저 정보 임시로 더미데이터 활용
    //MultiModeUser user = new MultiModeUser(3, "apple");

    double distance = 0;
    TextView paceGoalContentTextView;
    TextView timeGoalContentTextView;
    TextView goldDistanceTextView;
    TextView goldNickNameTextView;

    TextView silverDistanceTextView;
    TextView silverNickNameTextView;
    TextView bronzeDistanceTextView;
    TextView bronzeNickNameTextView;
    private final Handler top3UpdateHandler = new Handler(Looper.getMainLooper()) {//탑3 유저 업데이트. 아마 handleMessage 코드가 실제로 실행되는지는 모르겟음
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.obj instanceof Packet) {
                Packet receivedPacket = (Packet) msg.obj;
                if (receivedPacket.getProtocol() == Protocol.UPDATE_TOP3_STATES) {
                    UserDistance[] top3UserDistance = receivedPacket.getTop3UserDistance();
                    updateTop3UserDistance(top3UserDistance);

                }
            }
        }
    };
    TextView distancePresentContentTextView; //API 사용해서 구한 나의 현재 이동 거리
    TextView pacePresentContentTextView; //API 사용해서 구한 나의 현재 페이스
    SocketListenerThread socketListenerThread = null;
    private TextView timePresentContentTextView;
    private Handler timeHandler;
    private Runnable timeRunnable;

    private Handler sendDataHandler;

    private Runnable sendDataRunnable;
    private int isFinished = 0;
    private ObjectInputStream ois;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        selectedRoom = (MultiModeRoom) getArguments().getSerializable("room");
        socketListenerThread = (SocketListenerThread) getArguments().getSerializable("socketListenerThread"); //waitFragment의 socketListenrThread객체 가져와서 이어서 사용
        socketListenerThread.addPlayFragment(this);
        socketListenerThread.resumeListening();
//        try {
//            socketManager.openSocket();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        View view = inflater.inflate(R.layout.fragment_multi_room_play, container, false); //각종 view 선언
        if (selectedRoom != null) {
            timeGoalContentTextView = view.findViewById(R.id.time_goal_content);
            timePresentContentTextView = view.findViewById(R.id.time_present_content);
            goldNickNameTextView = view.findViewById(R.id.gold_nickname);
            goldDistanceTextView = view.findViewById(R.id.gold_distance);
            silverNickNameTextView = view.findViewById(R.id.silver_nickname);
            silverDistanceTextView = view.findViewById(R.id.silver_distance);
            bronzeNickNameTextView = view.findViewById(R.id.bronze_nickname);
            bronzeDistanceTextView = view.findViewById(R.id.bronze_distance);

            //목표 시간 계산하기 위한 코드
            long secondsRemaining = selectedRoom.getDuration().getSeconds();

            // 시간, 분으로 변환
            long hours = secondsRemaining / 3600;
            long minutes = (secondsRemaining % 3600) / 60;
            long seconds = secondsRemaining % 60;
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                    hours, minutes, seconds);

            timeGoalContentTextView.setText(formattedTime);

        }

        //경과 시간 업데이트
        timeHandler = new Handler(Looper.getMainLooper());

        // Runnable을 사용하여 매 초마다 시간 업데이트
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                if (selectedRoom != null) {
                    LocalDateTime currentTime = LocalDateTime.now();
                    Duration present = Duration.between(selectedRoom.getStartTime(), currentTime);
                    long secondsElapsed = present.getSeconds();

                    // 시간, 분, 초로 변환
                    long hours = secondsElapsed / 3600;
                    long minutes = (secondsElapsed % 3600) / 60;
                    long seconds = secondsElapsed % 60;

                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                            hours, minutes, seconds);
                    Log.d("response", formattedTime);
                    timePresentContentTextView.setText(formattedTime);

                    // present가 목표 시간(selectedRoom.getDuration())과 같아지면 업데이트 중지
                    Log.d("response", "present : " + present.getSeconds());
                    Log.d("response", "getDuration : " + selectedRoom.getDuration().getSeconds());
                    if (present.getSeconds() >= selectedRoom.getDuration().getSeconds()) {
                        timeHandler.removeCallbacks(timeRunnable);
                        isFinished = 1;
                    }
                }
                if (isFinished == 0) {
                    // 1초마다 Runnable 실행
                    timeHandler.postDelayed(this, 1000);
                }
            }
        };
        timeHandler.post(timeRunnable);

        //5초마다 현재 이동 거리 전송
        sendDataHandler = new Handler(Looper.getMainLooper());
        sendDataRunnable = new Runnable() {
            @Override
            public void run() {
                Packet requestPacket = new Packet(Protocol.UPDATE_USER_DISTANCE, user, distance);
                distance += 1;
                new SendPacketTask().execute(requestPacket);
                if (isFinished == 0) {
                    // 1초마다 Runnable 실행
                    sendDataHandler.postDelayed(this, 5000); // 5초마다 전송. 처음에 socketlistnerthread 설정 때문에 약간의 딜레이가 필요할 듯 함
                }
            }
        };

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start sendDataHandler after 5 seconds
                sendDataHandler.post(sendDataRunnable);
            }
        }, 5000); // 최초에 경기 시작하고 5초 뒤에 전송.

        return view;

    }


    public void updateTop3UserDistance(UserDistance[] userDistances) { // 화면에 표시되는 top3 유저 정보 업데이트. socketListenerThread에서 사용
        UserDistance[] top3UserDistance = userDistances;
        for (int i = 0; i < userDistances.length; i++) {

            Log.d("response", "In updateTop3UserDistance, top3user " + i + " : " + top3UserDistance[0].getUser().getNickName() + " , distance : " + userDistances[0].getDistance());
            Log.d("response", "In updateTop3UserDistance, user " + i + " : " + userDistances[0].getUser().getNickName() + " , distance : " + userDistances[0].getDistance());

        }
        if (top3UserDistance.length == 1) {
            goldNickNameTextView.setText(top3UserDistance[0].getUser().getNickName());
            double goldDistance = top3UserDistance[0].getDistance();
            String goldDistanceString = String.format("%.1f", goldDistance);
            goldDistanceTextView.setText(goldDistanceString);
        } else if (top3UserDistance.length == 2) {
            goldNickNameTextView.setText(top3UserDistance[0].getUser().getNickName());
            double goldDistance = top3UserDistance[0].getDistance();
            String goldDistanceString = String.format("%.1f", goldDistance);
            goldDistanceTextView.setText(goldDistanceString);

            silverNickNameTextView.setText(top3UserDistance[1].getUser().getNickName());
            double silverDistance = top3UserDistance[1].getDistance();
            String silverDistanceString = String.format("%.1f", silverDistance);
            silverDistanceTextView.setText(silverDistanceString);


        } else {
            goldNickNameTextView.setText(top3UserDistance[0].getUser().getNickName());
            double goldDistance = top3UserDistance[0].getDistance();
            String goldDistanceString = String.format("%.1f", goldDistance);
            goldDistanceTextView.setText(goldDistanceString);

            silverNickNameTextView.setText(top3UserDistance[1].getUser().getNickName());
            double silverDistance = top3UserDistance[1].getDistance();
            String silverDistanceString = String.format("%.1f", silverDistance);
            silverDistanceTextView.setText(silverDistanceString);

            bronzeNickNameTextView.setText(top3UserDistance[2].getUser().getNickName());
            double bronzeDistance = top3UserDistance[2].getDistance();
            String bronzeDistanceString = String.format("%.1f", bronzeDistance);
            silverDistanceTextView.setText(bronzeDistanceString);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        socketManager = SocketManager.getInstance();
//        ois = socketManager.getOIS();
//        socketListenerThread = new SocketListenerThread(null, this, top3UpdateHandler, selectedRoom, ois);
//        socketListenerThread.start();

        //top3UpdateHandler.postDelayed(sendDataRunnable, 5000);


    }

    @Override
    public void onPause() {
        super.onPause();
        if (socketListenerThread != null) {
            socketListenerThread.interrupt();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 타이머가 더 이상 필요하지 않을 때 핸들러를 제거합니다.
        timeHandler.removeCallbacks(timeRunnable);
    }

    private class SendPacketTask extends AsyncTask<Packet, Void, Boolean> { // 서버에 업데이트할 거리 정보 전송
        @Override
        protected Boolean doInBackground(Packet... packets) {
            boolean success = true;
            try {
                if (packets.length > 0) {
                    // Get the first Packet to send from the parameters
                    Packet packetToSend = packets[0];

                    // Get the ObjectOutputStream from the socket manager
                    ObjectOutputStream oos = socketManager.getOOS();

                    // Write the Packet object to the output stream
                    oos.writeObject(packetToSend);
                    oos.flush();
                } else {
                    // Handle the case when no packets are provided
                    success = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
            return success;
        }

        // Handle the result of sending the packet
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Log.d("SendPacket", "Packet sent successfully! distance : " + distance);
            } else {
                Log.d("SendPacket", "Failed to send packet! distance : " + distance);
            }
        }
    }
}
