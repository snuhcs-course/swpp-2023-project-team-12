package com.example.runusandroid.ui.multi_mode;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.Protocol;

public class MultiModeWaitFragment extends Fragment {

    SocketListenerThread socketListenerThread = MultiModeFragment.socketListenerThread;
    OnBackPressedCallback backPressedCallBack;
    private final Handler handler = new Handler(); // 남은 시간 계산 위한 Handler
    private final int updateTimeInSeconds = 1; // 1초마다 업데이트/
    MultiModeUser user = MultiModeFragment.user;
    SocketManager socketManager = SocketManager.getInstance();  // SocketManager 인스턴스를 가져옴
    private MultiModeRoom selectedRoom; // MultiModeRoom 객체를 저장할 멤버 변수
    private TextView titleTextView;
    private TextView startTimeTextView;
    private TextView timeRemainingTextView;
    private Duration duration;
    //남은 시간 계산 로직
    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            // 현재 시간 가져오기
            LocalDateTime currentDateTime = LocalDateTime.now();

            // startTime 가져오기
            LocalDateTime startTimeDateTime = selectedRoom.getStartTime();  // startTime을 LocalDateTime 객체로 가정합니다.

            // 남은 시간 계산
            duration = Duration.between(currentDateTime, startTimeDateTime);

            startGame();

            long secondsRemaining = duration.getSeconds();

            // 시간, 분으로 변환
            long hours = secondsRemaining / 3600;
            long minutes = (secondsRemaining % 3600) / 60;
            long seconds = secondsRemaining % 60;

            // "x시간 x분 남음" 형식으로 문자열 구성
            String remainingTime;
            if (hours > 0) {
                remainingTime = String.format(Locale.getDefault(), "시작까지 %d시간 %d분 남음", hours, minutes);
            } else if (secondsRemaining >= 0) {
                remainingTime = String.format(Locale.getDefault(), "시작까지 %d분 %d초 남음", minutes, seconds);
            } else {
                remainingTime = "경기가 곧 시작됩니다";
            }

            // 업데이트된 시간을 텍스트 뷰에 설정
            timeRemainingTextView.setText(remainingTime);

            // 1초마다 업데이트
            handler.postDelayed(this, 1000);
        }
    };
    private MainActivity2 mainActivity2;
    private ConstraintLayout waitingListBox;
    private TextView participantCountTextView;
    private ObjectInputStream ois;

    public MultiModeWaitFragment() {
    }

    public TextView getParticipantCountTextView() {
        return participantCountTextView;
    }

    void startGame() {
        // startTime이 현재 시간보다 앞선 경우
        if (duration.isNegative() || duration.isZero()) {
            timeRemainingTextView.setText("곧 경기가 시작됩니다");
            if (selectedRoom.getRoomOwner().getId() == user.getId()) {
                new StartRoomTask().execute();
            }

            // Runnable 종료
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // XML 레이아웃 파일을 inflate
        mainActivity2 = (MainActivity2) getActivity();
        BottomNavigationView navView = mainActivity2.findViewById(R.id.nav_view);
        navView.setVisibility(View.GONE);
        View view = inflater.inflate(R.layout.fragment_multi_room_wait, container, false);
        titleTextView = view.findViewById(R.id.multi_room_wait_title);
        startTimeTextView = view.findViewById(R.id.multi_room_wait_start_time);
        timeRemainingTextView = view.findViewById(R.id.time_remaining);

        waitingListBox = view.findViewById(R.id.waiting_list_box);

        selectedRoom = (MultiModeRoom) getArguments().getSerializable("room");
        // 여기에서 MultiModeRoom 객체(multiModeRoom)를 사용하여 UI에 표현되어야 하는 text 설정
        if (selectedRoom != null) {
            // MultiModeRoom 객체에 저장된 정보를 화면에 표시
            TextView titleTextView = view.findViewById(R.id.multi_room_wait_title);
            TextView startTimeTextView = view.findViewById(R.id.multi_room_wait_start_time);
            participantCountTextView = view.findViewById(R.id.participant_count);

            titleTextView.setText(selectedRoom.getTitle());
            startTimeTextView.setText(String.format("%02d:%02d", selectedRoom.getStartTime().getHour(), selectedRoom.getStartTime().getMinute()));
            List<MultiModeUser> userList = selectedRoom.getUserList();
            updateParticipantCount(userList.size(), selectedRoom.getNumRunners());

            if (userList != null && !userList.isEmpty()) {
                for (MultiModeUser user : userList) {
                    addUserNameToWaitingList(user.getNickname());
                }
            }
            handler.postDelayed(updateTimeRunnable, updateTimeInSeconds * 1000);

        } else {
            Log.d("Response", "no multiroom object");

        }
        Button leaveButton = view.findViewById(R.id.leaveButton); //떠나기 버튼
        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 떠나기 버튼을 눌렀을 때 실행할 동작 추가
                new ExitRoomTask().execute();
            }
        });
        return view;
    }

    //현재 유저 / 총 유저 보여주는 부분 업데이트 함수
    public void updateParticipantCount(int size, int total) {
        String text = size + "/" + total;
        participantCountTextView.setText(text);
    }
    //유저가 나갔을 때 패킷을 받아 방 인원 업데이트

    // 입장한 유저 이름 보여주는 waiting list
    public void addUserNameToWaitingList(String userName) {
        TextView userNameTextView = new TextView(getContext());
        userNameTextView.setId(View.generateViewId());
        userNameTextView.setText(userName);
        userNameTextView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.round_name_background));
        userNameTextView.setPadding(10, 10, 10, 10);
        userNameTextView.setGravity(Gravity.CENTER);

        // 레이아웃 매개 변수 설정
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                200,  // 너비를 200dp로 설정
                200   // 높이를 200dp로 설정
        );
        userNameTextView.setLayoutParams(layoutParams);

        int childCount = waitingListBox.getChildCount();

        if (childCount < 9) { // 최대 9개까지만 추가
            waitingListBox.addView(userNameTextView);

            ConstraintSet set = new ConstraintSet();
            set.clone(waitingListBox);

            // 그리드의 열 수
            int columns = 3;

            // 행 및 열 계산
            int row = childCount / columns;
            int col = childCount % columns;

            // 왼쪽 상단에 배치되도록 설정
            set.connect(userNameTextView.getId(), ConstraintSet.TOP, waitingListBox.getId(), ConstraintSet.TOP, 50);
            set.connect(userNameTextView.getId(), ConstraintSet.START, waitingListBox.getId(), ConstraintSet.START, 50);

            if (row > 0) {
                // 두 번째 행부터 아래쪽으로 이동
                View previousRowView = waitingListBox.getChildAt(childCount - columns);
                set.connect(userNameTextView.getId(), ConstraintSet.TOP, previousRowView.getId(), ConstraintSet.BOTTOM, 50);
            }

            if (col > 0) {
                // 두 번째 열부터 오른쪽으로 이동
                View previousColView = waitingListBox.getChildAt(childCount - 1);
                set.connect(userNameTextView.getId(), ConstraintSet.START, previousColView.getId(), ConstraintSet.END, 50);
            }

            set.applyTo(waitingListBox);
        }
    }

    public void removeUserNameFromWaitingList(String userName) {
        int childCount = waitingListBox.getChildCount();
        View viewToRemove = null;

        for (int i = 0; i < childCount; i++) {
            View childView = waitingListBox.getChildAt(i);

            if (childView instanceof TextView) {
                TextView userNameTextView = (TextView) childView;
                if (userName.equals(userNameTextView.getText().toString())) {
                    viewToRemove = childView;
                    break; // 유저 아이디를 찾으면 루프 종료
                }
            }
        }

        if (viewToRemove != null) {
            waitingListBox.removeView(viewToRemove);

            // 다시 UI를 그리기 위한 레이아웃 로직 구현
            ConstraintSet set = new ConstraintSet();
            set.clone(waitingListBox);

            int columns = 3; // 그리드의 열 수
            int childCountAfterRemoval = waitingListBox.getChildCount();

            for (int i = 0; i < childCountAfterRemoval; i++) {
                View childView = waitingListBox.getChildAt(i);

                if (childView instanceof TextView) {
                    TextView userNameTextView = (TextView) childView;

                    // 행 및 열 계산
                    int row = i / columns;
                    int col = i % columns;

                    // 왼쪽 상단에 배치되도록 설정
                    set.connect(userNameTextView.getId(), ConstraintSet.TOP, waitingListBox.getId(), ConstraintSet.TOP, 50);
                    set.connect(userNameTextView.getId(), ConstraintSet.START, waitingListBox.getId(), ConstraintSet.START, 50);

                    if (row > 0) {
                        // 두 번째 행부터 아래쪽으로 이동
                        View previousRowView = waitingListBox.getChildAt(i - columns);
                        set.connect(userNameTextView.getId(), ConstraintSet.TOP, previousRowView.getId(), ConstraintSet.BOTTOM, 50);
                    }

                    if (col > 0) {
                        // 두 번째 열부터 오른쪽으로 이동
                        View previousColView = waitingListBox.getChildAt(i - 1);
                        set.connect(userNameTextView.getId(), ConstraintSet.START, previousColView.getId(), ConstraintSet.END, 50);
                    }
                }
            }

            set.applyTo(waitingListBox);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backPressedCallBack = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new ExitRoomTask().execute();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallBack);
        socketManager = SocketManager.getInstance();
        ois = socketManager.getOIS();
        socketListenerThread.addWaitFragment(this);
        socketListenerThread.setRoom(selectedRoom);
    }

    @Override
    public void onPause() {
        super.onPause();
        backPressedCallBack.remove();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 타이머가 더 이상 필요하지 않을 때 핸들러를 제거합니다.
        handler.removeCallbacks(updateTimeRunnable);
    }

    //방에서 나갈 때 소켓과 연결하여 패킷 송수신
    private class ExitRoomTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = true;
            try {
                ObjectOutputStream oos = socketManager.getOOS();
                Packet requestPacket = new Packet(Protocol.EXIT_ROOM, user, selectedRoom);
                oos.reset();
                oos.writeObject(requestPacket);
                oos.flush();
                selectedRoom.exitUser(user);
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
            return success;
        }

        //ExitRoomTask 실행 결과에 따라 수행
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.navigation_multi_mode);
                Log.d("exitroomSendPacket", "Packet sent successfully!");
            } else {
                Log.d("ExitSendPacket", "Failed to send packet!");
            }
        }

    }

    private class StartRoomTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = true;
            try {
                ObjectOutputStream oos = socketManager.getOOS();
                Packet requestPacket = new Packet(Protocol.START_GAME, selectedRoom);
                oos.reset();
                oos.writeObject(requestPacket);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Log.d("SendPacket", "Start Game Packet sent successfully!");

            } else {
                Log.d("ExitSendPacket", "Failed to send Start Game packet!");
            }
        }

    }
}