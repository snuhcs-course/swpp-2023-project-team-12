package com.example.runusandroid.ui.multi_mode;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
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

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runusandroid.R;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

public class MultiModeFragment extends Fragment {


    public static MultiModeUser user = new MultiModeUser(1, "choco");
    //public static MultiModeUser user = new MultiModeUser(2, "berry"); // 유저 정보 임시로 더미데이터 활용
    //public static MultiModeUser user = new MultiModeUser(3, "apple");
    private final SocketManager socketManager = SocketManager.getInstance();  // SocketManager 인스턴스를 가져옴
    Dialog dialog;
    private Button createRoomButton;
    private RecyclerView recyclerView;
    private MultiModeAdapter adapter;
    private List<MultiModeRoom> roomList = new ArrayList<>();


    public MultiModeFragment() {
    }

    private void showModal(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.create_room_modal_layout);
        dialog.setCanceledOnTouchOutside(true);
        ImageButton closeButton = dialog.findViewById(R.id.buttonClose);
        EditText groupNameEditText = dialog.findViewById(R.id.editTextGroupName);
        //EditText distanceEditText = dialog.findViewById(R.id.editTextDistance);
        //EditText timeEditText = dialog.findViewById(R.id.editTextTime);
        TimePicker time_picker = dialog.findViewById(R.id.timePicker);
        EditText membersEditText = dialog.findViewById(R.id.editTextMembers);
        //EditText tagEditText = dialog.findViewById(R.id.editTextTag);
        NumberPicker numberPickerHour = dialog.findViewById(R.id.hourPicker);
        NumberPicker numberPickerMinute = dialog.findViewById(R.id.minutePicker);

        // 시간 설정: 0 ~ 23
        numberPickerHour.setMinValue(0);
        numberPickerHour.setMaxValue(23);
        // 분 설정: 0 ~ 59
        numberPickerMinute.setMinValue(0);
        numberPickerMinute.setMaxValue(59);

        // 초기값 설정
        groupNameEditText.setText("Test");
        //distanceEditText.setText("5");
        time_picker.setHour(0);
        time_picker.setMinute(0);
        membersEditText.setText("5");
        numberPickerHour.setValue(0);
        numberPickerMinute.setValue(1);

        Button completeButton = dialog.findViewById(R.id.buttonComplete);
        final String[] pickedTime = new String[1];
        time_picker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                pickedTime[0] = hour + ":" + minute;
            }
        });
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = groupNameEditText.getText().toString();
                //double distance = Double.parseDouble(distanceEditText.getText().toString());
                int numRunners = Integer.parseInt(membersEditText.getText().toString());
                int timePickerCurrentHour = time_picker.getCurrentHour();
                int timePickerCurrentMinute = time_picker.getCurrentMinute();

                Duration duration = Duration.ofHours(numberPickerHour.getValue()).plusMinutes(numberPickerMinute.getValue());
                //Duration duration = Duration.ofHours(0).plusMinutes(0).plusSeconds(30);
                LocalDate today = LocalDate.now();
                LocalDateTime startTime = LocalDateTime.of(today, LocalTime.of(timePickerCurrentHour, timePickerCurrentMinute));
                // 현재 시간보다 선택한 시간이 느린 경우 하루 뒤로 설정
                LocalDateTime now = LocalDateTime.now();
                if (startTime.isBefore(now)) {
                    startTime = startTime.plusDays(1);
                }
                RoomCreateInfo roomInfo = new RoomCreateInfo(groupName, 0, LocalDateTime.now().plusSeconds(5), numRunners, duration);
                //RoomCreateInfo roomInfo = new RoomCreateInfo(groupName, 0, startTime, numRunners, duration);
                new SendRoomInfoTask().execute(roomInfo); //소켓에 연결하여 패킷 전송

            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ImageButton을 클릭하면 모달 레이아웃의 가시성을 변경
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_multi_mode, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new MultiModeAdapter(roomList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        createRoomButton = view.findViewById(R.id.createRoomButton);
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModal(v.getContext());
            }
        });

        new GetRoomListTask().execute();

        return view;
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

    private class GetRoomListTask extends AsyncTask<Void, Void, List<MultiModeRoom>> {
        @Override
        protected List<MultiModeRoom> doInBackground(Void... voids) {
            Socket socket = null;
            try {
                socketManager.openSocket(); // 소켓 연결

                ObjectOutputStream oos = socketManager.getOOS();
                ObjectInputStream ois = socketManager.getOIS();


                int dataType = Protocol.ROOM_LIST;
                Packet requestPacket = new Packet(dataType, user);
                oos.writeObject(requestPacket);
                oos.flush();

                Object receivedObject = ois.readObject();
                if (receivedObject instanceof String) {
                    // Handle String response if needed
                    Log.d("Response", "Received response: " + receivedObject);

                } else if (receivedObject instanceof Packet) {
                    if (((Packet) receivedObject).getProtocol() == Protocol.ROOM_LIST) {
                        roomList = ((Packet) receivedObject).getRoomList();
                        Log.d("Response", "Received response: " + roomList.size());

                    } else {
                        Log.d("Response", "Received response: " + receivedObject);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    printRoomListInfo(roomList);
                    //socket.close();
                    socketManager.closeSocket();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return roomList;
        }

        @Override
        protected void onPostExecute(List<MultiModeRoom> result) {
            super.onPostExecute(result);
            adapter.setRoomList(roomList);
            adapter.notifyDataSetChanged();
        }
    }

    //소켓에 연결하여 방을 만들고, 해당 방에 입장
    private class SendRoomInfoTask extends AsyncTask<RoomCreateInfo, Void, Boolean> {

        Packet packet;

        @Override
        protected Boolean doInBackground(RoomCreateInfo... roomInfo) {
            boolean success = false;
            try {
                Log.d("response socketManager", socketManager.toString());
                socketManager.openSocket(); // 소켓 연결

                ObjectOutputStream oos = socketManager.getOOS();
                ObjectInputStream ois = socketManager.getOIS();
                int dataType = Protocol.CREATE_ROOM;
                Packet requestPacket = new Packet(dataType, user, roomInfo[0]); // 서버에 보내는 패킷
                oos.writeObject(requestPacket); //서버로 패킷 전송
                oos.flush();

                Object receivedObject = ois.readObject(); //서버로부터 패킷 수신
                if (receivedObject instanceof Packet) {
                    packet = (Packet) receivedObject;

                    success = true;
                    Log.d("Response", "Received response: " + receivedObject);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            return success;
        }

        //SendRoomInfoTask의 결과에 따라 작동
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            dialog.dismiss();
            adapter.setRoomList(packet.getRoomList());

            Bundle bundle = new Bundle();
            bundle.putSerializable("room", packet.getSelectedRoom()); //Bundle에 selectedRoom 저장
            View rootView = MultiModeFragment.this.getView();

            // NavController를 사용하여 다음 fragment로 이동. 이 때 Bundle에 담긴 정보 전달
            NavController navController = Navigation.findNavController(rootView);
            navController.navigate(R.id.navigation_multi_room_wait, bundle);

        }
    }


}