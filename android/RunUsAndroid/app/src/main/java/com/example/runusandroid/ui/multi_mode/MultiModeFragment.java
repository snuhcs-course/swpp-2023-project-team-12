package com.example.runusandroid.ui.multi_mode;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runusandroid.AccountApi;
import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.example.runusandroid.RetrofitClient;
import com.example.runusandroid.UserProfileResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.Protocol;
import MultiMode.RoomCreateInfo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MultiModeFragment extends Fragment {
    public static MultiModeUser user; // 유저 정보 임시로 더미데이터 활용
    public static SocketListenerThread socketListenerThread;
    private final SocketManager socketManager = SocketManager.getInstance();
    private final Handler updateHandler = new Handler(Looper.getMainLooper());
    OnBackPressedCallback backPressedCallBack;
    SharedPreferences sharedPreferences;
    Dialog dialog;
    private long completeButtonLastClickTime = 0;
    private long createRoomButtonLastClickTime = 0;
    private MultiModeAdapter adapter;

    private String imageUrl = "";

    public MultiModeFragment() {
    }

    private void showModal(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.create_room_modal_layout);
        dialog.setCanceledOnTouchOutside(true);
        ImageButton closeButton = dialog.findViewById(R.id.buttonClose);
        EditText groupNameEditText = dialog.findViewById(R.id.editTextGroupName);
        TimePicker time_picker = dialog.findViewById(R.id.timePicker);
        EditText membersEditText = dialog.findViewById(R.id.editTextMembers);
        NumberPicker numberPickerHour = dialog.findViewById(R.id.hourPicker);
        NumberPicker numberPickerMinute = dialog.findViewById(R.id.minutePicker);

        // 시간 설정: 0 ~ 4
        numberPickerHour.setMinValue(0);
        numberPickerHour.setMaxValue(4);
        // 분 설정: 0 ~ 59
        numberPickerMinute.setMinValue(0);
        numberPickerMinute.setMaxValue(59);

        // 초기값 설정
        groupNameEditText.setText("함께 달려요!");
        LocalTime now = LocalTime.now().plusMinutes(10);
        time_picker.setHour(now.getHour());
        time_picker.setMinute(now.getMinute());
        membersEditText.setText("10");
        numberPickerHour.setValue(0);
        numberPickerMinute.setValue(30);

        Button completeButton = dialog.findViewById(R.id.buttonComplete);
        final String[] pickedTime = new String[1];
        time_picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                pickedTime[0] = hour + ":" + minute;
            }
        });
        completeButton.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - completeButtonLastClickTime < 1000) {
                return;
            }
            completeButtonLastClickTime = SystemClock.elapsedRealtime();

            String groupName = groupNameEditText.getText().toString();
            int numRunners = Integer.parseInt(membersEditText.getText().toString());
            int timePickerCurrentHour = time_picker.getCurrentHour();
            int timePickerCurrentMinute = time_picker.getCurrentMinute();
            Duration duration = Duration.ofHours(numberPickerHour.getValue())
                    .plusMinutes(numberPickerMinute.getValue());
            LocalDate today = LocalDate.now();
            LocalDateTime startTime = LocalDateTime.of(today,
                    LocalTime.of(timePickerCurrentHour, timePickerCurrentMinute));
            // 현재 시간보다 선택한 시간이 느린 경우 하루 뒤로 설정
            if (startTime.isBefore(LocalDateTime.now())) {
                startTime = startTime.plusDays(1);
            }
            RoomCreateInfo roomCreateInfo = new RoomCreateInfo(groupName, 0, startTime, numRunners, duration);

            new SendRoomInfoTask().execute(roomCreateInfo);
        });

        closeButton.setOnClickListener(v -> {
            // ImageButton을 클릭하면 모달 레이아웃의 가시성을 변경
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AccountApi accountApi = RetrofitClient.getClient().create(AccountApi.class);
        sharedPreferences = getContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("userid", 99999);
        String nickName = sharedPreferences.getString("nickname", "guest");
        user = new MultiModeUser((int) userId, nickName, "");
        accountApi.getUserProfile(String.valueOf(userId)).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    imageUrl = response.body().getProfileImageUrl();
                    user = new MultiModeUser((int) userId, nickName, imageUrl);
                }

            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.e("UserProfile", "Failed to load user profile", t);
            }
        });

        Log.d("Profile_image", user.getNickName() + "'s profile_image=" + user.getProfileImageUrl());

        View view = inflater.inflate(R.layout.fragment_multi_mode, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new MultiModeAdapter(new ArrayList<MultiModeRoom>());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity2 mainActivity2 = (MainActivity2) getActivity();
        BottomNavigationView navView = mainActivity2.findViewById(R.id.nav_view);
        navView.setVisibility(View.VISIBLE);

        Button createRoomButton = view.findViewById(R.id.createRoomButton);
        createRoomButton.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - createRoomButtonLastClickTime < 1000) {
                return;
            }
            createRoomButtonLastClickTime = SystemClock.elapsedRealtime();
            showModal(v.getContext());
        });

    }

    private void printRoomListInfo(List<MultiModeRoom> roomList) {
        for (MultiModeRoom room : roomList) {
            printRoomInfo(room);
        }
    }

    private void printRoomInfo(MultiModeRoom room) {
        if (room == null) {
            Log.d("response", "room is null");
        } else {
            List<MultiModeUser> userList = room.getUserList();
            Log.d("response", "room name is " + room.getTitle());
            for (MultiModeUser user : userList) {
                Log.d("response", "username : " + user.getNickname());
            }
        }
    }

    public void setAdapter(List<MultiModeRoom> roomList) {
        adapter.setRoomList(roomList);
        adapter.notifyDataSetChanged();
    }

    public void navigateRoomWait(MultiModeRoom room) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("room", room);
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.navigation_multi_room_wait, bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        backPressedCallBack = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.navigation_home);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallBack);
        if (socketListenerThread == null) {
            socketListenerThread = new SocketListenerThread(socketManager.getOIS());
            socketListenerThread.addMultiModeFragment(this);
            socketListenerThread.addHandler(updateHandler);
            socketListenerThread.start();
        } else {
            socketListenerThread.addMultiModeFragment(this);
            socketListenerThread.addHandler(updateHandler);
        }
        new GetRoomListTask().execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        backPressedCallBack.remove();
    }

    private class GetRoomListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = true;
            try {
                ObjectOutputStream oos = socketManager.getOOS();
                Packet requestPacket = new Packet(Protocol.ROOM_LIST, user);
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
        }
    }

    // 소켓에 연결하여 방을 만들고, 해당 방에 입장
    private class SendRoomInfoTask extends AsyncTask<RoomCreateInfo, Void, Boolean> {
        @Override
        protected Boolean doInBackground(RoomCreateInfo... roomInfo) {
            boolean success = false;
            try {
                ObjectOutputStream oos = socketManager.getOOS();
                Packet requestPacket = new Packet(Protocol.CREATE_ROOM, user, roomInfo[0]); // 서버에 보내는 패킷
                oos.reset();
                oos.writeObject(requestPacket); // 서버로 패킷 전송
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            dialog.dismiss();
        }
    }
}