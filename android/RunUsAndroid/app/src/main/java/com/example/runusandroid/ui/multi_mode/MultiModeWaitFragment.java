package com.example.runusandroid.ui.multi_mode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.runusandroid.AccountApi;
import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.example.runusandroid.RetrofitClient;
import com.example.runusandroid.UserProfileResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.Protocol;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MultiModeWaitFragment extends Fragment {

    private final Handler handler = new Handler(); // 남은 시간 계산 위한 Handler
    private final int updateTimeInSeconds = 1; // 1초마다 업데이트/
    public boolean isFragmentVisible = true;
    private boolean navRoomListWhenResumed = false;
    private boolean notificatedOneMinuteLeft = false;
    SocketListenerThread socketListenerThread = MultiModeFragment.socketListenerThread;
    OnBackPressedCallback backPressedCallBack;
    MultiModeUser user = MultiModeFragment.user;
    SocketManager socketManager = SocketManager.getInstance();  // SocketManager 인스턴스를 가져옴
    private long leaveButtonLastClickTime = 0;
    private long backButtonLastClickTime = 0;
    private boolean isGameStarted = false;
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

            if(!notificatedOneMinuteLeft && secondsRemaining <= 60){
                makeNotification();
            }

            // 시간, 분으로 변환
            long hours = secondsRemaining / 3600;
            long minutes = (secondsRemaining % 3600) / 60;
            long seconds = secondsRemaining % 60;

            // "x시간 x분 남음" 형식으로 문자열 구성
            String remainingTime;
            if (hours > 0) {
                remainingTime = String.format(Locale.getDefault(), "%d시간 %d분 남음", hours, minutes);
            } else if (secondsRemaining >= 0) {
                remainingTime = String.format(Locale.getDefault(), "%d분 %d초 남음", minutes, seconds);
            } else {
                remainingTime = "경기가 곧 시작됩니다";
            }

            // 업데이트된 시간을 텍스트 뷰에 설정
            timeRemainingTextView.setText(remainingTime);

            // 1초마다 업데이트
            handler.postDelayed(this, 1000);
        }
    };
    private LinearLayout linearLayoutOdd;
    private LinearLayout linearLayoutEven;
    private MainActivity2 mainActivity2;
    private HorizontalScrollView waitingListBox;
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
            if (!isGameStarted && selectedRoom.getRoomOwner().getId() == user.getId()) {
                isGameStarted = true;
                new StartRoomTask().execute();
            }
            // Runnable 종료
        }
    }

    private void showExitDialog() {
        @SuppressLint("InflateParams")
        View exitRoomDialog = getLayoutInflater().inflate(R.layout.dialog_multimode_wait_finish, null);
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(exitRoomDialog);
        Button buttonConfirmExitRoom = exitRoomDialog.findViewById(R.id.buttonConfirmExitRoom);
        buttonConfirmExitRoom.setOnClickListener(v -> {
            dialog.dismiss();
            new ExitRoomTask().execute();
        });
        dialog.show();
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
        linearLayoutOdd = view.findViewById(R.id.linear_layout_odd);
        linearLayoutEven = view.findViewById(R.id.linear_layout_even);

        waitingListBox = view.findViewById(R.id.waiting_list_box);

        selectedRoom = (MultiModeRoom) getArguments().getSerializable("room");
        // 여기에서 MultiModeRoom 객체(multiModeRoom)를 사용하여 UI에 표현되어야 하는 text 설정
        if (selectedRoom != null) {
            // MultiModeRoom 객체에 저장된 정보를 화면에 표시
            TextView titleTextView = view.findViewById(R.id.multi_room_wait_title);
            TextView startTimeTextView = view.findViewById(R.id.multi_room_wait_start_time);
            TextView gameDuration = view.findViewById(R.id.multi_room_duration);
            participantCountTextView = view.findViewById(R.id.participant_count);

            titleTextView.setText(selectedRoom.getTitle());
            Duration duration = selectedRoom.getDuration();

            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            String timeString;
            if (hours > 0) {
                timeString = String.format("경기 시간: %d시간 %d분", hours, minutes);
            } else {
                timeString = String.format("경기 시간: %d분", minutes);
            }

            gameDuration.setText(timeString);

            List<MultiModeUser> userList = selectedRoom.getUserList();
            updateParticipantCount(userList.size(), selectedRoom.getNumRunners());
            if (userList != null && !userList.isEmpty()) {
                for (MultiModeUser user : userList) {
                    addUserNameToWaitingList(user);
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
                if (SystemClock.elapsedRealtime() - leaveButtonLastClickTime < 1000) {
                    return;
                }
                leaveButtonLastClickTime = SystemClock.elapsedRealtime();
                showExitDialog();
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
    public void addUserNameToWaitingList(MultiModeUser user) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View userView = inflater.inflate(R.layout.waiting_user_item, null, false);

        ImageView profileImageView = userView.findViewById(R.id.multi_room_profile);
        TextView levelTextView = userView.findViewById(R.id.multi_room_level);
        TextView nicknameTextView = userView.findViewById(R.id.multi_room_nickname);

        int level = user.getLevel();
        String nickname = user.getNickname();
        String userId = String.valueOf(user.getId());
        String profileImage = user.getProfileImageUrl();

        levelTextView.setText("Lv. " + level);
        nicknameTextView.setText(nickname);
        AccountApi accountApi = RetrofitClient.getClient().create(AccountApi.class);

        accountApi.getUserProfile(userId).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imageUrl = response.body().getProfileImageUrl();
                    Log.d("prfile", "profile=" + imageUrl);
                    Glide.with(MultiModeWaitFragment.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.runus_logo)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profileImageView);
                } else {
                    Glide.with(MultiModeWaitFragment.this)
                            .load("").placeholder(R.drawable.runus_logo)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profileImageView);
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.e("UserProfile", "Failed to load user profile", t);
            }
        });

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120, getResources().getDisplayMetrics());
        layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
        layoutParams.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        userView.setLayoutParams(layoutParams);

        int childCount = linearLayoutOdd.getChildCount() + linearLayoutEven.getChildCount();
        LinearLayout targetLayout = (childCount % 2 == 0) ? linearLayoutOdd : linearLayoutEven;

        userView.setLayoutParams(layoutParams);

        targetLayout.addView(userView);
    }

    private void makeNotification() {
        notificatedOneMinuteLeft = true;

        Intent intent = new Intent(this.requireContext(), MainActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.requireContext(), "MultiModeWait")
                .setSmallIcon(R.drawable.runus_logo)
                .setContentTitle("게임이 1분 후에 시작됩니다!")
                .setContentText("앱에 접속하여 게임에 참여하세요!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this.requireContext());
        // notificationId is a unique int for each notification that you must define
        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1000);

            }
        }
        notificationManager.notify(10, builder.build());
    }


    public void removeUserNameFromWaitingList(String userName) {
        List<View> userViews = new ArrayList<>();

        for (int i = 0; i < linearLayoutOdd.getChildCount(); i++) {
            View view = linearLayoutOdd.getChildAt(i);
            TextView nicknameTextView = view.findViewById(R.id.multi_room_nickname);
            if (!userName.equals(nicknameTextView.getText().toString())) {
                userViews.add(view);
            }
        }

        for (int i = 0; i < linearLayoutEven.getChildCount(); i++) {
            View view = linearLayoutEven.getChildAt(i);
            TextView nicknameTextView = view.findViewById(R.id.multi_room_nickname);
            if (!userName.equals(nicknameTextView.getText().toString())) {
                userViews.add(view);
            }
        }

        linearLayoutOdd.removeAllViews();
        linearLayoutEven.removeAllViews();

        for (int i = 0; i < userViews.size(); i++) {
            LinearLayout targetLayout = (i % 2 == 0) ? linearLayoutOdd : linearLayoutEven;
            targetLayout.addView(userViews.get(i));
        }
    }

    public void exitGameInBackground() {
        new ExitGameInBackgroundTask().execute();
        navRoomListWhenResumed = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navRoomListWhenResumed) {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_multi_mode);
        } else {
            backPressedCallBack = new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (SystemClock.elapsedRealtime() - backButtonLastClickTime < 1000) {
                        return;
                    }
                    backButtonLastClickTime = SystemClock.elapsedRealtime();
                    showExitDialog();
                }
            };
            requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallBack);
            socketManager = SocketManager.getInstance();
            ois = socketManager.getOIS();
            socketListenerThread.addWaitFragment(this);
            socketListenerThread.setRoom(selectedRoom);
            isFragmentVisible = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("pause", "paused");
        isFragmentVisible = false;
        backPressedCallBack.remove();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("destroy", "destroyed");
        // 타이머가 더 이상 필요하지 않을 때 핸들러를 제거합니다.
        handler.removeCallbacks(updateTimeRunnable);
    }

    public void setRoom(MultiModeRoom room) {
        selectedRoom = room;
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

    private class ExitGameInBackgroundTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        public Boolean doInBackground(Void... voids) {
            boolean success = true;
            try {
                Packet requestPacket = new Packet(Protocol.EXIT_GAME, user, selectedRoom);
                ObjectOutputStream oos = socketManager.getOOS();
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
                //NavController navController = Navigation.findNavController(requireView());
                //navController.navigate(R.id.navigation_multi_mode);
                Log.d("ExitGameBackSendPacket", "Packet sent successfully!");
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