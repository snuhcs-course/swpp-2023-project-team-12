package com.example.runusandroid.ui.multi_mode;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.runusandroid.R;

import MultiMode.MultiModeRoom;

public class MultiModeWaitFragment extends Fragment {

    private MultiModeRoom multiModeRoom; // MultiModeRoom 객체를 저장할 멤버 변수
    private TextView titleTextView;
    private TextView startTimeTextView;
    private TextView remainingTimeTextView;
    public MultiModeWaitFragment() {
        // 빈 생성자는 기본 생성자와 함께 필요합니다.
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // XML 레이아웃 파일을 inflate
        View view = inflater.inflate(R.layout.fragment_multi_room_wait, container, false);
        titleTextView = view.findViewById(R.id.multi_room_wait_title);
        startTimeTextView = view.findViewById(R.id.game_start_time);
        remainingTimeTextView = view.findViewById(R.id.time_remaining);


        MultiModeRoom multiModeRoom = (MultiModeRoom) getArguments().getSerializable("room");
        // 여기에서 MultiModeRoom 객체(multiModeRoom)를 사용하여 필요한 작업 수행
        if (multiModeRoom != null) {
            // MultiModeRoom 객체에 저장된 정보를 화면에 표시
            TextView titleTextView = view.findViewById(R.id.multi_room_wait_title);
            TextView startTimeTextView = view.findViewById(R.id.game_start_time);

            titleTextView.setText(multiModeRoom.getTitle());
            startTimeTextView.setText(multiModeRoom.getStartTime());
        }else{
            Log.d("Response", "no multiroom object");

        }
        Button leaveButton = view.findViewById(R.id.leaveButton);
        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 떠나기 버튼을 눌렀을 때 실행할 동작 추가
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_multi_mode);
            }
        });
        // 필요한 초기화 및 작업 수행

        return view;
    }
}
