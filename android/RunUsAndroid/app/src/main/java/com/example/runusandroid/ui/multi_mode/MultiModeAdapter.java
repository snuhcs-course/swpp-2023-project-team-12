package com.example.runusandroid.ui.multi_mode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.runusandroid.R;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.PacketBuilder;
import MultiMode.Protocol;

public class MultiModeAdapter extends RecyclerView.Adapter<MultiModeAdapter.ViewHolder> {
    private final SocketManager socketManager = SocketManager.getInstance(); // SocketManager 인스턴스를 가져옴
    MultiModeUser user = MultiModeFragment.user;
    MultiModeRoom selectedRoom;
    private long enterButtonLastClickTime = 0;
    // MultiMode List 화면에서 각 Room Button과 관련된 Adapter
    private List<MultiModeRoom> roomList;

    public MultiModeAdapter(List<MultiModeRoom> items) {
        this.roomList = items;
    }

    private static AppCompatActivity unwrap(Context context) {
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        assert context instanceof AppCompatActivity;
        return (AppCompatActivity) context;
    }

    @SuppressLint("NotifyDataSetChanged")
    void setRoomList(List<MultiModeRoom> roomList) {
        this.roomList = roomList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MultiModeRoom room = roomList.get(position);
        MultiModeUser roomOwner = room.getRoomOwner();

        TextView titleTextView = holder.itemView.findViewById(R.id.titleTextView);
        TextView participantTextView = holder.itemView.findViewById(R.id.participantTextView);
        TextView timeTextView = holder.itemView.findViewById(R.id.timeTextView);
        ImageView eventImageView = holder.itemView.findViewById(R.id.eventImageView);
        Button enterButton = holder.itemView.findViewById(R.id.confirmButton);

        titleTextView.setText(room.getTitle());
        String participantInfo = room.getUserSize() + "/" + room.getNumRunners();
        participantTextView.setText(participantInfo);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String startTime = room.getStartTime().format(formatter);
        Duration duration = room.getDuration();
        String endTime = room.getStartTime().plus(duration).format(formatter);
        String timeInfo = startTime + " ~ " + endTime;

        timeTextView.setText(timeInfo);
        Log.d("profile_image", roomOwner.getNickName());
        Log.d("profile_image", String.format("%d", roomOwner.getId()));
        Log.d("profile_image", roomOwner.getProfileImageUrl() != null ? roomOwner.getProfileImageUrl() : "");
        Glide.with(holder.itemView.getContext())
                .load(roomOwner.getProfileImageUrl())
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.runus_logo) // 프로필 이미지가 없는 경우 디폴트 이미지
                .into(holder.eventImageView);


        enterButton.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - enterButtonLastClickTime < 1000) {
                return;
            }
            enterButtonLastClickTime = SystemClock.elapsedRealtime();
            selectedRoom = roomList.get(position);
            NavController navController = Navigation.findNavController(v);
            new EnterRoomTask().execute();
        });
    }

    @Override
    public int getItemCount() {
        return roomList.size();
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
        public TextView titleTextView;
        public TextView participantTextView;
        public TextView timeTextView;
        public ImageView eventImageView;
        public Button enterButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            participantTextView = itemView.findViewById(R.id.participantTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            eventImageView = itemView.findViewById(R.id.eventImageView);
            enterButton = itemView.findViewById(R.id.confirmButton);
        }
    }

    //방 입장시 socket을 통해 서버와 연결
    @SuppressLint("StaticFieldLeak")
    private class EnterRoomTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = true;
            try {
                ObjectOutputStream oos = socketManager.getOOS(); // 서버로 바이트스트림을 직렬화하기 위해 필요.
                PacketBuilder packetBuilder = new PacketBuilder().protocol(Protocol.ENTER_ROOM).user(user).selectedRoom(selectedRoom);
                Packet requestPacket = packetBuilder.getPacket();
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
        protected void onPostExecute(Boolean success) { // doInBackground()의 return값에 따라 작업 수행. 룸 리스트 업데이트, 입장하는 방 정보
            super.onPostExecute(success);
        }

    }

}