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

public class MultiModeFragment extends Fragment {

    private Button createRoomButton;
    private RecyclerView recyclerView;
    private MultiModeAdapter adapter;

    private List<MultiModeRoom> roomList = new ArrayList<>();
    Dialog dialog;




    private void showModal(Context context) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.create_room_modal_layout);
        dialog.setCanceledOnTouchOutside(true);

        EditText groupNameEditText = dialog.findViewById(R.id.editTextGroupName);
        EditText distanceEditText = dialog.findViewById(R.id.editTextDistance);
        EditText timeEditText = dialog.findViewById(R.id.editTextTime);
        EditText membersEditText = dialog.findViewById(R.id.editTextMembers);
        EditText tagEditText = dialog.findViewById(R.id.editTextTag);

        Button completeButton = dialog.findViewById(R.id.buttonComplete);
        completeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = groupNameEditText.getText().toString();
                double distance = Double.parseDouble(distanceEditText.getText().toString());
                String time = timeEditText.getText().toString();
                int members = Integer.parseInt(membersEditText.getText().toString());


                RoomCreateInfo roomInfo = new RoomCreateInfo(groupName, distance, time, members);
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
                socket = new Socket("10.0.2.2", 5001); // 서버 IP와 포트를 서버와 일치시켜야 합니다

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

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
                    if (socket != null) {
                        socket.close();
                    }
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
        @Override
        protected Boolean doInBackground(RoomCreateInfo... roomInfo) {
            Socket socket = null;
            boolean success = false;
            try {
                socket = new Socket("10.0.2.2", 5001);

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                int dataType = Protocol.CREATE_ROOM;
                MultiModeUser user = new MultiModeUser(1, "chocochip");
                Packet requestPacket = new Packet(dataType, user, roomInfo[0]);
                oos.writeObject(requestPacket);
                oos.flush();

                Object receivedObject = ois.readObject();
                if (receivedObject instanceof String && "SUCCESS".equals(receivedObject)) {

                    success = true;
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
