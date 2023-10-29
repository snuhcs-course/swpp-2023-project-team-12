package com.example.runusandroid.ui.multi_mode;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runusandroid.R;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Random;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.Protocol;

public class MultiModeAdapter extends RecyclerView.Adapter<MultiModeAdapter.ViewHolder> {

    //MultiModeUser user = new MultiModeUser(3, "apple");
    private final SocketManager socketManager = SocketManager.getInstance();  // SocketManager 인스턴스를 가져옴
    MultiModeUser user = new MultiModeUser(2, "berry"); // 유저 정보 임시로 더미데이터 활용
    //MultiModeUser user = new MultiModeUser(1, "choco"); // 유저 정보 임시로 더미데이터 활용
    MultiModeRoom selectedRoom;
    //MultiMode List 화면에서 각 Room Button과 관련된 Adapter
    private List<MultiModeRoom> roomList;

    public MultiModeAdapter(List<MultiModeRoom> items) {
        this.roomList = items;
    }

    private static AppCompatActivity unwrap(Context context) {
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        return (AppCompatActivity) context;
    }

    void setRoomList(List<MultiModeRoom> roomList) {
        this.roomList = roomList;
        notifyDataSetChanged();
    }

    private int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int color = getRandomColor();
        Button button = holder.itemView.findViewById(R.id.textViewItem);
        button.setBackgroundTintList(ColorStateList.valueOf(color));
        MultiModeRoom item = roomList.get(position);
        holder.textView.setText(item.getTitle());

        button.setOnClickListener(v -> {
            // 클릭된 버튼의 MultiModeRoom 정보 가져오기
            selectedRoom = roomList.get(position);
            NavController navController = Navigation.findNavController(v);
            new EnterRoomTask(v.getContext(), navController).execute();

            // 방 정보를 전달하기 위해 Bundle을 생성
            //Bundle bundle = new Bundle();
            //bundle.putSerializable("room", selectedRoom);
            // NavController를 사용하여 다음 fragment로 이동
            //NavController navController = Navigation.findNavController(v);
            //navController.navigate(R.id.navigation_multi_room_wait, bundle);

        });

    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    private void showFullRoomToast(Context context) {
        Toast.makeText(context, "인원이 초과되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void printRoomInfo(MultiModeRoom room) {
        if (room == null) {
            Log.d("response", "room is null");
        } else {
            List<MultiModeUser> userList = room.getUserList();
            for (MultiModeUser user : userList) {
                Log.d("response", "username : " + user.getNickname());
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewItem);
        }
    }

    //방 입장시 socket을 통해 서버와 연결
    private class EnterRoomTask extends AsyncTask<Void, Void, Boolean> {
        private final Context mContext;
        private final NavController navcon;
        Packet packet;

        public EnterRoomTask(Context context, NavController navcon) {
            this.mContext = context;
            this.navcon = navcon;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Socket socket = null;
            boolean success = true;
            try {
                socketManager.openSocket(); // 소켓 연결

                ObjectOutputStream oos = socketManager.getOOS(); //서버로 바이트스트림을 직렬화하기 위해 필요.
                ObjectInputStream ois = socketManager.getOIS(); //서버로부터 받는 바이트스트림을 역직렬화하기 위해 필요.


                if (selectedRoom.getUserList().size() < selectedRoom.getNumRunners()) {
                    Packet requestPacket = new Packet(Protocol.ENTER_ROOM, user, selectedRoom);
                    Log.d("response enterroom", "protocol : " + requestPacket.getProtocol() + " user is " + user.getNickname());
                    oos.writeObject(requestPacket); //서버로 패킷 전송
                    oos.flush();

                    Object receivedObject = ois.readObject(); //서버로부터 패킷 수신
                    if (receivedObject instanceof Packet) {
                        packet = (Packet) receivedObject;
                        Log.d("response", "userlist size is " + packet.getSelectedRoom().getUserList().size());
                        selectedRoom.enterUser(user);
                        printRoomInfo(packet.getSelectedRoom());
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                success = false;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) { //doInBackground()의 return값에 따라 작업 수행. 룸 리스트 업데이트, 입장하는 방 정보 업데이트
            super.onPostExecute(success);
            setRoomList(packet.getRoomList());
            selectedRoom = packet.getSelectedRoom();
            if (success) {
                Log.d("SendPacket", "Packet sent successfully!");
                setRoomList(packet.getRoomList());
                selectedRoom = packet.getSelectedRoom();

                Bundle bundle = new Bundle();
                bundle.putSerializable("room", selectedRoom);
                // NavController를 사용하여 다음 fragment로 이동
                navcon.navigate(R.id.navigation_multi_room_wait, bundle);
            } else {
                showFullRoomToast(mContext); // 방이 이미 꽉 찼다는 Toast 보내기
                Log.e("SendPacket", "Failed to send packet!");
            }
        }

    }

}