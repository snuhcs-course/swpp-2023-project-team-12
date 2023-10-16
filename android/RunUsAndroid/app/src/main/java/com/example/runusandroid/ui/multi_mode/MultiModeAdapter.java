package com.example.runusandroid.ui.multi_mode;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import MultiMode.MultiModeRoom;
import com.example.runusandroid.R;

import java.util.List;
import java.util.Random;

public class MultiModeAdapter extends RecyclerView.Adapter<MultiModeAdapter.ViewHolder> {

    private List<MultiModeRoom> roomList;

    void setRoomList(List<MultiModeRoom> roomList) {
        this.roomList = roomList;
    }
    private int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    public MultiModeAdapter(List<MultiModeRoom> items) {
        this.roomList = items;
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
            MultiModeRoom clickedRoom = roomList.get(position);

            // 방 정보를 전달하기 위해 Bundle을 생성
            Bundle bundle = new Bundle();
            bundle.putSerializable("room", clickedRoom);

            // NavController를 사용하여 다음 fragment로 이동
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.navigation_multi_room_wait, bundle);


//            // Fragment를 시작하고 Bundle을 전달
//            MultiModeWaitFragment fragment = new MultiModeWaitFragment(); // 방 정보를 표시하는 Fragment로 변경
//            fragment.setArguments(bundle);
//
//            // FragmentTransaction을 통해 원래의 Fragment를 완전히 대체
//            AppCompatActivity activity = unwrap(v.getContext());
//            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
//            transaction.replace(R.id.fragment_container, fragment);
//            transaction.addToBackStack(null);
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            transaction.commit();
        });

    }
    private static AppCompatActivity unwrap(Context context) {
        while (!(context instanceof Activity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }

        return (AppCompatActivity) context;
    }
    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewItem);
        }
    }
}
