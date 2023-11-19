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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.runusandroid.AccountApi;
import com.example.runusandroid.ExpSystem;
import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.example.runusandroid.RetrofitClient;
import com.example.runusandroid.UserProfileResponse;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import MultiMode.MultiModeRoom;
import MultiMode.UserDistance;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    Button resultLeaveButton;
    Button recordButton;
    TextView distanceResultContentTextView; //API 사용해서 구한 나의 현재 이동 거리
    SocketListenerThread socketListenerThread = MultiModeFragment.socketListenerThread;
    RecyclerView recyclerView;
    RecordDialog dialog;
    boolean isDialogOpenedBefore = false;
    private ImageView goldProfileImageView, silverProfileImageView, bronzeProfileImageView;
    private TextView goldLevelTextView, silverLevelTextView, bronzeLevelTextView;
    private TextView goldNickNameTextView, silverNickNameTextView, bronzeNickNameTextView;

    private TextView goldDistanceTextView, silverDistanceTextView, bronzeDistanceTextView;
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
            View goldProfileBox = view.findViewById(R.id.firstPlaceProfileBox);
            goldProfileImageView = goldProfileBox.findViewById(R.id.multi_result_profile);
            goldLevelTextView = goldProfileBox.findViewById(R.id.multi_result_level);
            goldNickNameTextView = goldProfileBox.findViewById(R.id.multi_result_nickname);
            goldDistanceTextView = view.findViewById(R.id.firstPlaceKm);

            View silverProfileBox = view.findViewById(R.id.secondPlaceProfileBox);
            silverProfileImageView = silverProfileBox.findViewById(R.id.multi_result_profile);
            silverLevelTextView = silverProfileBox.findViewById(R.id.multi_result_level);
            silverNickNameTextView = silverProfileBox.findViewById(R.id.multi_result_nickname);
            silverDistanceTextView = view.findViewById(R.id.secondPlaceKm);

            View bronzeProfileBox = view.findViewById(R.id.thirdPlaceProfileBox);
            bronzeProfileImageView = bronzeProfileBox.findViewById(R.id.multi_result_profile);
            bronzeLevelTextView = bronzeProfileBox.findViewById(R.id.multi_result_level);
            bronzeNickNameTextView = bronzeProfileBox.findViewById(R.id.multi_result_nickname);
            bronzeDistanceTextView = view.findViewById(R.id.thirdPlaceKm);

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
        AccountApi accountApi = RetrofitClient.getClient().create(AccountApi.class);
        if (userDistances.length >= 1) {
            goldNickNameTextView.setText(userDistances[0].getUser().getNickName());
            goldLevelTextView.setText("Lv. " + userDistances[0].getUser().getLevel());
            String goldDistanceString = String.format("%.2fkm", userDistances[0].getDistance());
            goldDistanceTextView.setText(goldDistanceString);
            accountApi.getUserProfile(String.valueOf(userDistances[0].getUser().getId())).enqueue(new Callback<UserProfileResponse>() {
                @Override
                public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String imageUrl = response.body().getProfileImageUrl();
                        Glide.with(MultiModeResultFragment.this)
                                .load(imageUrl).placeholder(R.drawable.runus_logo)
                                .apply(RequestOptions.circleCropTransform())
                                .into(goldProfileImageView);

                    } else {
                        Glide.with(MultiModeResultFragment.this)
                                .load("").placeholder(R.drawable.runus_logo)
                                .apply(RequestOptions.circleCropTransform())
                                .into(goldProfileImageView);
                    }
                }

                @Override
                public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                    Log.e("UserProfile", "Failed to load user profile", t);
                }
            });

            if (userDistances.length >= 2) {
                silverNickNameTextView.setText(userDistances[1].getUser().getNickName());
                silverLevelTextView.setText("Lv. " + userDistances[1].getUser().getLevel());
                String silverDistanceString = String.format("%.2fkm", userDistances[1].getDistance());
                silverDistanceTextView.setText(silverDistanceString);
                accountApi.getUserProfile(String.valueOf(userDistances[1].getUser().getId())).enqueue(new Callback<UserProfileResponse>() {
                    @Override
                    public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String imageUrl = response.body().getProfileImageUrl();
                            Glide.with(MultiModeResultFragment.this)
                                    .load(imageUrl).placeholder(R.drawable.runus_logo)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(silverProfileImageView);

                        } else {
                            Glide.with(MultiModeResultFragment.this)
                                    .load("").placeholder(R.drawable.runus_logo)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(silverProfileImageView);
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                        Log.e("UserProfile", "Failed to load user profile", t);
                    }
                });

                if (userDistances.length >= 3) {
                    bronzeNickNameTextView.setText(userDistances[2].getUser().getNickName());
                    bronzeLevelTextView.setText("Lv. " + userDistances[2].getUser().getLevel());
                    String bronzeDistanceString = String.format("%.2fkm", userDistances[2].getDistance());
                    bronzeDistanceTextView.setText(bronzeDistanceString);
                    accountApi.getUserProfile(String.valueOf(userDistances[2].getUser().getId())).enqueue(new Callback<UserProfileResponse>() {
                        @Override
                        public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                String imageUrl = response.body().getProfileImageUrl();
                                Glide.with(MultiModeResultFragment.this)
                                        .load(imageUrl).placeholder(R.drawable.runus_logo)
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(bronzeProfileImageView);

                            } else {
                                Glide.with(MultiModeResultFragment.this)
                                        .load("").placeholder(R.drawable.runus_logo)
                                        .apply(RequestOptions.circleCropTransform())
                                        .into(bronzeProfileImageView);
                            }
                        }

                        @Override
                        public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                            Log.e("UserProfile", "Failed to load user profile", t);
                        }
                    });
                }
            }
        }

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
