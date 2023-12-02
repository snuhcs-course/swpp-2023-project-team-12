package com.example.runusandroid.ui.user_setting;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.runusandroid.AccountApi;
import com.example.runusandroid.ActivityRecognition.RunningState;
import com.example.runusandroid.ExpSystem;
import com.example.runusandroid.ImageResponse;
import com.example.runusandroid.LoginActivity;
import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.example.runusandroid.RetrofitClient;
import com.example.runusandroid.UserProfileResponse;
import com.example.runusandroid.databinding.FragmentUserSettingBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserSettingFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    MainActivity2 mainActivity;
    TextView userNicknameTextView;
    TextView userLevelTextView;
    TextView userExpPercentTextView;
    TextView userExpPresentTextView;
    TextView userExpNextTextView;
    ImageView fiveGameClearImageView;
    ImageView firstGoldMedalImageView;
    ImageView tenMissionsClearImageView;
    ImageView steadyRunnerImageView;
    ImageView tenGoldMedalImageImageView;
    ImageView speedRunnerImageView;
    ImageView halfMarathonerImageView;
    ImageView marathonerImageView;
    ImageView marathonWinnerImageView;
    ProgressBar userExpProgressbar;
    Button creditButton;

    CreditDialog dialog;

    private FragmentUserSettingBinding binding;
    private Uri imageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;


    private Button testButton;
    private int badgeCollection;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //UserSettingViewModel userSettingViewModel = new ViewModelProvider(this).get(UserSettingViewModel.class);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("username", "");
        int userLevel = sharedPreferences.getInt("level", 1);
        String userLevelText = Integer.toString(userLevel);
        int exp = sharedPreferences.getInt("exp", 0);
        int presentExp = ExpSystem.getPresentExp(exp);
        int nextExp = ExpSystem.getNextExp(userLevel);
        String presentExpText = Integer.toString(presentExp);
        String nextExpText = Integer.toString(nextExp);
        int expPercentage = (presentExp * 100 / nextExp);

        Log.d("expPercentage", Integer.toString(expPercentage));
        String expPercentageText = Integer.toString(expPercentage);
        badgeCollection = sharedPreferences.getInt("badge_collection", 1000000000);
        Log.d("badge", Integer.toString(badgeCollection));
        String userId = String.valueOf(sharedPreferences.getLong("userid", 99999));
        binding = FragmentUserSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mainActivity = (MainActivity2) getActivity();

        userNicknameTextView = binding.userNickname;
        userLevelTextView = binding.userLevel;
        userExpPercentTextView = binding.userExpPercent;
        userExpPresentTextView = binding.userExpPresent;
        userExpNextTextView = binding.userExpNext;
        userExpProgressbar = binding.userExpProgressbar;
        fiveGameClearImageView = binding.fiveGameClearImage;
        firstGoldMedalImageView = binding.firstGoldMedalImage;
        tenMissionsClearImageView = binding.tenMissionsClearImage;
        steadyRunnerImageView = binding.steadyRunnerImage;
        tenGoldMedalImageImageView = binding.tenGoldMedalImage;
        speedRunnerImageView = binding.speedRunnerImage;
        halfMarathonerImageView = binding.halfMarathonerImage;
        marathonerImageView = binding.marathonerImage;
        marathonWinnerImageView = binding.marathonWinnerImage;
        creditButton = binding.creditButton;

        dialog = new CreditDialog(requireContext());

        AppCompatButton logoutButton = root.findViewById(R.id.logoutBtn);

        AccountApi accountApi = RetrofitClient.getClient().create(AccountApi.class);

        ImageView profileImageView = root.findViewById(R.id.profileImage);


        userNicknameTextView.setText(userName);
        userLevelTextView.setText(userLevelText);
        if (userLevel < 10) {
            userExpPercentTextView.setText(expPercentageText + "%");
            userExpPresentTextView.setText(presentExpText);
            userExpNextTextView.setText(nextExpText);
            userExpProgressbar.setProgress(expPercentage);
        } else {
            userExpPercentTextView.setText("--");
            userExpPresentTextView.setText("Max");
            userExpNextTextView.setText("Max");
            userExpProgressbar.setProgress(100);

        }
        updateBadge(badgeCollection);


        accountApi.getUserProfile(userId).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    badgeCollection = response.body().getBadgeCollection();
                    updateBadge(badgeCollection);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("badge_collection", badgeCollection);
                    Log.d("badgeFromServer", Integer.toString(badgeCollection));
                    editor.apply();
                    String imageUrl = response.body().getProfileImageUrl();
                    Log.d("prfile", "profile=" + imageUrl);
                    updateProfileImageInView(imageUrl);
                } else {
                    Glide.with(UserSettingFragment.this)
                            .load("").placeholder(R.drawable.runus_logo)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profileImageView);
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Log.e("UserProfile", "Failed to load user profile", t);
            }
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        uploadImageToServer(imageUri);
                    }
                });

        ImageButton changeProfileImageButton = root.findViewById(R.id.changeProfileImageButton);
        changeProfileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        creditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        // TO CHECK ACTIVITY STATE
        AppCompatButton checkButton = root.findViewById(R.id.check);
        checkButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String activityType = RunningState.getLastActivityType();
                String transitionType = RunningState.getLastTransitionType();
                boolean isRunning = RunningState.getIsRunning();

                Toast.makeText(mainActivity, "last state:" + transitionType + " " + activityType + " " + isRunning,
                        Toast.LENGTH_LONG).show();

            }

        });
        return root;
    }

    private void logoutUser() {
        // SharedPreferences에서 모든 데이터 삭제
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // LoginActivity로 이동
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void uploadImageToServer(Uri fileUri) {
        Log.d("UploadImage", "Attempting to upload image Uri: " + fileUri);
        try {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
            String userName = sharedPreferences.getString("username", "");
            InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
            byte[] byteArray = toByteArray(inputStream);
            Log.d("UploadImage", "ByteArray length: " + byteArray.length);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
            MultipartBody.Part body = MultipartBody.Part.createFormData("profile_image", userName + "_image.jpg",
                    requestFile);
            AccountApi accountApi = RetrofitClient.getClient().create(AccountApi.class);
            Call<ImageResponse> call = accountApi.uploadProfileImage(body);

            call.enqueue(new Callback<ImageResponse>() {
                @Override
                public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                    if (response.isSuccessful()) {
                        Log.d("UploadImage", "Response body: " + response.body());
                        ImageResponse imageResponse = response.body();
                        if (imageResponse != null) {
                            String newImageUrl = imageResponse.getImageUrl();
                            Log.d("UploadImage", "Image URL: " + newImageUrl);
                            SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs",
                                    MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("profile_image", newImageUrl);
                            editor.apply();

                            updateProfileImageInView(newImageUrl);
                            Log.d("UploadImage", "Image Updated with: " + newImageUrl);
                        } else {
                            Log.d("UploadImage", "ImageResponse is null");
                        }
                    } else {
                        Toast.makeText(getContext(), "업로드 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ImageResponse> call, Throwable t) {
                    Log.e("UploadImage", "Upload failed", t);
                    Toast.makeText(getContext(), "업로드 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("UploadImage", "IOException", e);
        }
    }

    private void openImageChooser() {
        imagePickerLauncher.launch("image/jpeg");
    }

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

        int width = originalBitmap.getWidth() / 2;
        int height = originalBitmap.getHeight() / 2;
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 10, bos);
        return bos.toByteArray();
    }


    private void updateProfileImageInView(String imageUrl) {
        Glide.with(UserSettingFragment.this)
                .load(imageUrl).placeholder(R.drawable.runus_logo)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profileImage);
    }

    private void updateBadge(int badgeCollection) {
        if (badgeCollection % 10 == 1) {
            fiveGameClearImageView.setImageResource(R.drawable.five_game_clear_image);
        } else {
            fiveGameClearImageView.setImageResource(R.drawable.padlock);
        }

        if ((badgeCollection / 10) % 10 == 1) {
            firstGoldMedalImageView.setImageResource(R.drawable.first_gold_medal_image);
        } else {
            firstGoldMedalImageView.setImageResource(R.drawable.padlock);
        }

        if ((badgeCollection / 100) % 10 == 1) {
            tenMissionsClearImageView.setImageResource(R.drawable.ten_missions_clear_image);
        } else {
            tenMissionsClearImageView.setImageResource(R.drawable.padlock);
        }

        if ((badgeCollection / 1000) % 10 == 1) {
            steadyRunnerImageView.setImageResource(R.drawable.steady_runner_image_view);
        } else {
            steadyRunnerImageView.setImageResource(R.drawable.padlock);
        }

        if ((badgeCollection / 10000) % 10 == 1) {
            tenGoldMedalImageImageView.setImageResource(R.drawable.ten_gold_medal_image);
        } else {
            tenGoldMedalImageImageView.setImageResource(R.drawable.padlock);
        }

        if ((badgeCollection / 100000) % 10 == 1) {
            speedRunnerImageView.setImageResource(R.drawable.speed_runner_image);
        } else {
            speedRunnerImageView.setImageResource(R.drawable.padlock);
        }

        if ((badgeCollection / 1000000) % 10 == 1) {
            halfMarathonerImageView.setImageResource(R.drawable.half_marathoner_image);
        } else {
            halfMarathonerImageView.setImageResource(R.drawable.padlock);
        }

        if ((badgeCollection / 10000000) % 10 == 1) {
            marathonerImageView.setImageResource(R.drawable.marathoner);
        } else {
            marathonerImageView.setImageResource(R.drawable.padlock);
        }
        if ((badgeCollection / 100000000) % 10 == 1) {
            marathonWinnerImageView.setImageResource(R.drawable.marathon_winner_image);
        } else {
            marathonWinnerImageView.setImageResource(R.drawable.padlock);
        }

    }

}
