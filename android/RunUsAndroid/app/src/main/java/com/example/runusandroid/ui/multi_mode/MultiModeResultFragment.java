package com.example.runusandroid.ui.multi_mode;

import android.os.Bundle;
import android.util.Log;
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

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.UserDistance;

public class MultiModeResultFragment extends Fragment {

    MultiModeUser user = MultiModeFragment.user;
    SocketManager socketManager = SocketManager.getInstance();
    ObjectOutputStream oos;
    MultiModeRoom selectedRoom;
    float distance = 0;
    NavController navController;
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
    SocketListenerThread socketListenerThread = MultiModeWaitFragment.socketListenerThread;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        selectedRoom = (MultiModeRoom) getArguments().getSerializable("room");

        View view = inflater.inflate(R.layout.fragment_multi_room_result, container, false); //각종 view 선언
        if (selectedRoom != null) {
            goldNickNameTextView = view.findViewById(R.id.gold_nickname);
            goldDistanceTextView = view.findViewById(R.id.gold_distance);
            silverNickNameTextView = view.findViewById(R.id.silver_nickname);
            silverDistanceTextView = view.findViewById(R.id.silver_distance);
            bronzeNickNameTextView = view.findViewById(R.id.bronze_nickname);
            bronzeDistanceTextView = view.findViewById(R.id.bronze_distance);
            progressBar = view.findViewById(R.id.linear_progress_bar);
            playLeaveButton = view.findViewById(R.id.result_leaveButton);

        }

        playLeaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_multi_mode);
            }
        });


        return view;

    }

    public void updateTop3UserDistance(UserDistance[] userDistances) { // 화면에 표시되는 top3 유저 정보 업데이트. socketListenerThread에서 사용
        UserDistance[] top3UserDistance = userDistances;
        for (int i = 0; i < userDistances.length; i++) {

            Log.d("response", "In updateTop3UserDistance, top3user " + i + " : " + top3UserDistance[0].getUser().getNickName() + " , distance : " + userDistances[0].getDistance());
            Log.d("response", "In updateTop3UserDistance, user " + i + " : " + userDistances[0].getUser().getNickName() + " , distance : " + userDistances[0].getDistance());

        }
        double goldDistance = 0;

        if (top3UserDistance.length >= 1) {
            goldNickNameTextView.setText(top3UserDistance[0].getUser().getNickName());
            goldDistance = top3UserDistance[0].getDistance();
            String goldDistanceString = String.format("%.3fkm", goldDistance);
            goldDistanceTextView.setText(goldDistanceString);

            if (top3UserDistance.length >= 2) {

                silverNickNameTextView.setText(top3UserDistance[1].getUser().getNickName());
                double silverDistance = top3UserDistance[1].getDistance();
                String silverDistanceString = String.format("%.3fkm", silverDistance);
                silverDistanceTextView.setText(silverDistanceString);

                if (top3UserDistance.length >= 3) {
                    bronzeNickNameTextView.setText(top3UserDistance[2].getUser().getNickName());
                    double bronzeDistance = top3UserDistance[2].getDistance();
                    String bronzeDistanceString = String.format("%.3fkm", bronzeDistance);
                    bronzeDistanceTextView.setText(bronzeDistanceString);
                }
            }
        }


        progressBar.setProgress(100);

    }

    @Override
    public void onResume() {
        super.onResume();
        //socketListenerThread = (SocketListenerThread) getArguments().getSerializable("socketListenerThread"); //waitFragment의 socketListenrThread객체 가져와서 이어서 사용
        socketListenerThread.addResultFragment(this);
        socketListenerThread.resumeListening();
        UserDistance[] top3UserDistance = (UserDistance[]) getArguments().getSerializable("top3UserDistance");
        updateTop3UserDistance(top3UserDistance);
        distance = (float) getArguments().getSerializable("userDistance");
        Log.d("response", "here is room result screen");


    }
}
