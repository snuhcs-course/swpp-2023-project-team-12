package com.example.runusandroid.ui.multi_mode;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runusandroid.ExpSystem;
import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import MultiMode.MultiModeRoom;
import MultiMode.UserDistance;

public class MultiModeResultFragment extends Fragment {
    OnBackPressedCallback backPressedCallBack;
    MultiModeRoom selectedRoom;
    float calories = 0;
    float distance = 0;
    ArrayList<Float> speedList;
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
    Button resultLeaveButton;
    Button recordButton;
    TextView distanceResultContentTextView; //API 사용해서 구한 나의 현재 이동 거리
    SocketListenerThread socketListenerThread = MultiModeFragment.socketListenerThread;
    RecyclerView recyclerView;
    RecordDialog dialog;
    boolean isDialogOpenedBefore = false;
    private long resultLeaveButtonLastClickTime = 0;
    private long backButtonLastClickTime = 0;
    private TextView timeResultContentTextView;
    private int updatedExp;

    private void showExitResultDialog() {
        @SuppressLint("InflateParams")
        View exitResultDialog = getLayoutInflater().inflate(R.layout.dialog_multimode_play_finish, null);
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(exitResultDialog);
        Button buttonConfirmPlayExit = exitResultDialog.findViewById(R.id.buttonConfirmPlayExit);
        buttonConfirmPlayExit.setOnClickListener(v -> {
            dialog.dismiss();
            navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_multi_mode);
        });
        TextView textViewExitResult = exitResultDialog.findViewById(R.id.textViewExitGame);
        textViewExitResult.setText(R.string.MultiModeResultExitMessage);
        dialog.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updatedExp = (int) getArguments().getSerializable("updatedExp");

    }

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
            resultLeaveButton = view.findViewById(R.id.result_leaveButton);
            distanceResultContentTextView = view.findViewById(R.id.distance_present_content);
            timeResultContentTextView = view.findViewById(R.id.time_present_content);
            dialog = new RecordDialog(requireContext()); // requireContext()를 사용하여 컨텍스트 가져옴
            updatedExp = (int) getArguments().getSerializable("updatedExp");
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("exp", updatedExp);
            int updatedLevel = ExpSystem.getLevel(updatedExp);
            int pastLevel = sharedPreferences.getInt("level", -1);
            if (pastLevel < updatedLevel) {
                editor.putInt("level", updatedLevel);
                showLevelUpDialog(pastLevel, updatedLevel);
            }
            editor.apply();
            Log.d("response", "update_exp is + " + sharedPreferences.getInt("exp", -1));
            recordButton = view.findViewById(R.id.record_button);
            recordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.show();
                    if (!isDialogOpenedBefore) {

                        dialog.caloriesText.setText(calories + " kcal");
                        speedList = (ArrayList<Float>) getArguments().getSerializable("userSpeedList");
                        Log.d("speedList", speedList.size() + "");
                        double section = 1.0;
                        //for test
//                        distance = 4.579f;
//                        speedList = new ArrayList<>();
//                        speedList.add(12.0f);
//                        speedList.add(10.0f);
//                        speedList.add(15.0f);
//                        speedList.add(20.0f);
//                        speedList.add(12.0f);

                        while (true) {
                            if (distance - section >= 0) {
                                float speed = speedList.get((int) section - 1);
                                dialog.adapter.addItem(new RecordItem(section, speed));
                                Log.d("speedList", "first if : " + section + " " + speed);

                                section++;
                            } else {
                                if (speedList.size() > 0) {
                                    float speed = speedList.get((int) section - 1);
                                    dialog.adapter.addItem(new RecordItem(distance - (section - 1), speed));
                                    Log.d("speedList", "second if : " + (section - 1) + " " + speed);
                                }
                                section = 1.0;
                                break;
                            }
                        }
                        dialog.adapter.notifyDataSetChanged();
                        isDialogOpenedBefore = true;
                    }
                }
            });

        }

        resultLeaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - resultLeaveButtonLastClickTime < 1000) {
                    return;
                }
                resultLeaveButtonLastClickTime = SystemClock.elapsedRealtime();
                showExitResultDialog();
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
            String goldDistanceString = String.format("%.2fkm", goldDistance);
            goldDistanceTextView.setText(goldDistanceString);

            if (top3UserDistance.length >= 2) {

                silverNickNameTextView.setText(top3UserDistance[1].getUser().getNickName());
                double silverDistance = top3UserDistance[1].getDistance();
                String silverDistanceString = String.format("%.2fkm", silverDistance);
                silverDistanceTextView.setText(silverDistanceString);

                if (top3UserDistance.length >= 3) {
                    bronzeNickNameTextView.setText(top3UserDistance[2].getUser().getNickName());
                    double bronzeDistance = top3UserDistance[2].getDistance();
                    String bronzeDistanceString = String.format("%.2fkm", bronzeDistance);
                    bronzeDistanceTextView.setText(bronzeDistanceString);
                }
            }
        }
        progressBar.setProgress(100);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("create callback", "create callback");
        backPressedCallBack = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (SystemClock.elapsedRealtime() - backButtonLastClickTime < 1000) {
                    return;
                }
                backButtonLastClickTime = SystemClock.elapsedRealtime();
                showExitResultDialog();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallBack);
        socketListenerThread.addResultFragment(this);
        socketListenerThread.resumeListening();

        UserDistance[] top3UserDistance = (UserDistance[]) getArguments().getSerializable("top3UserDistance");
        updateTop3UserDistance(top3UserDistance);
        distance = (float) getArguments().getSerializable("userDistance");
        distanceResultContentTextView.setText(String.format(Locale.getDefault(), "%.2f" + "km", distance));
        long seconds = selectedRoom.getDuration().getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        // "00:00:00" 형태의 문자열로 변환
        String formattedDuration = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        timeResultContentTextView.setText(formattedDuration);
        Log.d("response", "here is room result screen");
    }

    @Override
    public void onPause() {
        super.onPause();
        backPressedCallBack.remove();
    }

    public void showLevelUpDialog(int pastLevel, int updatedLevel) {
        View levelUpDialogView = getLayoutInflater().inflate(R.layout.dialog_level_up, null);
        Dialog levelUpDialog = new Dialog(requireContext());
        levelUpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(levelUpDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        levelUpDialog.setContentView(levelUpDialogView);
        Button buttonConfirm = levelUpDialog.findViewById(R.id.buttonConfirmLevelUp);
        buttonConfirm.setOnClickListener(v -> {
            levelUpDialog.dismiss();
        });
        TextView textViewExitResult = levelUpDialogView.findViewById(R.id.textViewLevelUp);
        textViewExitResult.setText("Level " + pastLevel + "  ->  Level " + updatedLevel);
        levelUpDialog.show();
    }
}
