package com.example.runusandroid.ui.user_setting;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.runusandroid.AccountApi;
import com.example.runusandroid.ActivityRecognition.RunningState;
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
    private FragmentUserSettingBinding binding;
    private Uri imageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UserSettingViewModel userSettingViewModel = new ViewModelProvider(this).get(UserSettingViewModel.class);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("username", "");
        String userId = String.valueOf(sharedPreferences.getLong("userid", 99999));
        binding = FragmentUserSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mainActivity = (MainActivity2) getActivity();

        final TextView textView = binding.TextUserName;
        userSettingViewModel.setText(userName + "님 환영해요!");
        userSettingViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        AppCompatButton logoutButton = root.findViewById(R.id.LogoutBtn);

        AccountApi accountApi = RetrofitClient.getClient().create(AccountApi.class);

        ImageView profileImageView = root.findViewById(R.id.profileImage);

        accountApi.getUserProfile(userId).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
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

        AppCompatButton changeProfileImageButton = root.findViewById(R.id.changeProfileImageButton);
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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, read);
        }
        bos.close();
        return bos.toByteArray();
    }

    private void updateProfileImageInView(String imageUrl) {
        Glide.with(UserSettingFragment.this)
                .load(imageUrl).placeholder(R.drawable.runus_logo)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profileImage);
    }

}
