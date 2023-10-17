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

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.Protocol;
import MultiMode.RoomCreateInfo;

import com.example.runusandroid.R;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;

public class MultiModeFragment extends Fragment {

    private Button createRoomButton;
    private RecyclerView recyclerView;
    private MultiModeAdapter adapter;

    private List<MultiModeRoom> roomList = new ArrayList<>();
    Dialog dialog;

    private SocketManager socketManager = SocketManager.getInstance();  // SocketManager 인스턴스를 가져옴





    private void showModal(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.create_room_modal_layout);
        dialog.setCanceledOnTouchOutside(true);

        EditText groupNameEditText = dialog.findViewById(R.id.editTextGroupName);
        EditText distanceEditText = dialog.findViewById(R.id.editTextDistance);
        //EditText timeEditText = dialog.findViewById(R.id.editTextTime);
        TimePicker time_picker = dialog.findViewById(R.id.timePicker);
        EditText membersEditText = dialog.findViewById(R.id.editTextMembers);
        EditText tagEditText = dialog.findViewById(R.id.editTextTag);
        NumberPicker numberPickerHour = dialog.findViewById(R.id.hourPicker);
        NumberPicker numberPickerMinute = dialog.findViewById(R.id.minutePicker);

        // 시간 설정: 0 ~ 23
        numberPickerHour.setMinValue(0);
        numberPickerHour.setMaxValue(23);

// 분 설정: 0 ~ 59
        numberPickerMinute.setMinValue(0);
        numberPickerMinute.setMaxValue(59);

// 초기값 설정
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
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = groupNameEditText.getText().toString();
                double distance = Double.parseDouble(distanceEditText.getText().toString());
                String time = pickedTime[0];
                int numRunners = Integer.parseInt(membersEditText.getText().toString());
                int selectedHour = time_picker.getCurrentHour();
                int selectedMinute = time_picker.getCurrentMinute();

                // NumberPicker에서 시간과 분 추출
                int pickedHour = numberPickerHour.getValue();
                int pickedMinute = numberPickerMinute.getValue();

                RoomCreateInfo roomInfo = new RoomCreateInfo(groupName, distance, time, numRunners, pickedHour, pickedMinute);
                Log.d("response roomcreate", Integer.toString(numRunners));
                new SendRoomInfoTask().execute(roomInfo);

            }
        });

        dialog.show();
    }

    public MultiModeFragment() {
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

    private class GetRoomListTask extends AsyncTask<Void, Void, List<MultiModeRoom>> {
        @Override
        protected List<MultiModeRoom> doInBackground(Void... voids) {
            Socket socket = null;
            try {
//                socket = new Socket("10.0.2.2", 5001); // 서버 IP와 포트를 서버와 일치시켜야 합니다
//
//                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
//                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                socketManager.openSocket(); // 소켓 연결

                ObjectOutputStream oos = socketManager.getOOS();
                ObjectInputStream ois = socketManager.getOIS();



                int dataType = Protocol.ROOM_LIST;
                MultiModeUser user = new MultiModeUser(1, "chocochip");
                Packet requestPacket = new Packet(dataType, user);
                //oos.writeObject("hi");
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

    private class SendRoomInfoTask extends AsyncTask<RoomCreateInfo, Void, Boolean> {

        Packet packet;
        @Override
        protected Boolean doInBackground(RoomCreateInfo... roomInfo) {
            Socket socket = null;
            boolean success = false;
            try {
//                socket = new Socket("10.0.2.2", 5001);
//
//                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
//                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Log.d("response socketManager", socketManager.toString());
                socketManager.openSocket(); // 소켓 연결

                ObjectOutputStream oos = socketManager.getOOS();
                ObjectInputStream ois = socketManager.getOIS();
                Log.d("response", "cliked complete button");
                int dataType = Protocol.CREATE_ROOM;
                MultiModeUser user = new MultiModeUser(1, "chocochip");
                Packet requestPacket = new Packet(dataType, user, roomInfo[0]);
                oos.writeObject(requestPacket);
                oos.flush();

                //Object firstreceivedObject = ois.readObject(); //server의 broadcastNewClientInfo를
                Object receivedObject = ois.readObject();
                if (receivedObject instanceof Packet) {
                    packet = (Packet) receivedObject;
                    Log.d("Response", "packet Protocol is " + packet.getProtocol());
                    if(packet.getRoomList() != null){
                        Log.d("Response", "roomList Size is " + Integer.toString(packet.getRoomList().size()));
                    }
                    if(packet.getSelectedRoom() == null){
                        Log.d("response", "null selectedRoom");

                    }else {
                        Log.d("response", packet.getSelectedRoom().toString());
                    }
                    success = true;
                    Log.d("Response", "Received response: " + receivedObject);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
//            finally {
//                try {
//                    if (socket != null) {
//                        socket.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            dialog.dismiss();
            Log.d("response", Integer.toString(packet.getRoomList().size()));
            adapter.setRoomList(packet.getRoomList());

            Bundle bundle = new Bundle();
            bundle.putSerializable("room", packet.getSelectedRoom());
            if(packet.getSelectedRoom() == null){
                Log.d("response", "null selectedRoom");

            }else {
                Log.d("response", packet.getSelectedRoom().toString());
            }
            View rootView = MultiModeFragment.this.getView();

            // NavController를 사용하여 다음 fragment로 이동
            NavController navController = Navigation.findNavController(rootView);
            navController.navigate(R.id.navigation_multi_room_wait, bundle);

        }
    }
}
