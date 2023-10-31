package com.example.runusandroid.ui.multi_mode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;

import java.io.ObjectOutputStream;
import java.util.Locale;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;

public class MultiModeResultFragment extends Fragment {

    MultiModeUser user = MultiModeFragment.user;
    SocketManager socketManager = SocketManager.getInstance();
    ObjectOutputStream oos;
    MultiModeRoom selectedRoom;
    double distance = 0;
    TextView paceGoalContentTextView;
    MainActivity2 mainActivity;
    TextView timeGoalContentTextView;
    TextView goldDistanceTextView;
    TextView goldNickNameTextView;
    TextView silverDistanceTextView;
    TextView silverNickNameTextView;
    TextView bronzeDistanceTextView;
    TextView bronzeNickNameTextView;
    ProgressBar progressBar;
    Button playLeaveButton;
    SocketListenerThread socketListenerThread = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_multi_room_play, container, false); //각종 view 선언
        if (selectedRoom != null) {
            timeGoalContentTextView = view.findViewById(R.id.time_goal_content);
            goldNickNameTextView = view.findViewById(R.id.gold_nickname);
            goldDistanceTextView = view.findViewById(R.id.gold_distance);
            silverNickNameTextView = view.findViewById(R.id.silver_nickname);
            silverDistanceTextView = view.findViewById(R.id.silver_distance);
            bronzeNickNameTextView = view.findViewById(R.id.bronze_nickname);
            bronzeDistanceTextView = view.findViewById(R.id.bronze_distance);
            progressBar = view.findViewById(R.id.linear_progress_bar);
            playLeaveButton = view.findViewById(R.id.play_leaveButton);
            //목표 시간 계산하기 위한 코드
            long secondsRemaining = selectedRoom.getDuration().getSeconds();

            // 시간, 분으로 변환
            long hours = secondsRemaining / 3600;
            long minutes = (secondsRemaining % 3600) / 60;
            long seconds = secondsRemaining % 60;
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                    hours, minutes, seconds);

            timeGoalContentTextView.setText(formattedTime);

        }

        playLeaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_multi_mode);
            }
        });


        return view;

    }
}
