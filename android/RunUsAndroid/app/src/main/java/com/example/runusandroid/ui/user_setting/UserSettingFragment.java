package com.example.runusandroid.ui.user_setting;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runusandroid.LoginActivity;
import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.example.runusandroid.databinding.FragmentUserSettingBinding;

public class UserSettingFragment extends Fragment {

    private FragmentUserSettingBinding binding;
    MainActivity2 mainActivity;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UserSettingViewModel userSettingViewModel =
                new ViewModelProvider(this).get(UserSettingViewModel.class);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("username", "");
        binding = FragmentUserSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mainActivity = (MainActivity2) getActivity();

        final TextView textView = binding.textUserSetting;
        userSettingViewModel.setText(userName + "님 환영해요!");
        userSettingViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        AppCompatButton logoutButton = root.findViewById(R.id.LogoutBtn);

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

                String activityType = mainActivity.activityReceiver.getLastActivityType();
                String transitionType = mainActivity.activityReceiver.getLastTransitionType();
                boolean isRunning = mainActivity.activityReceiver.getIsRunning();

                Toast.makeText(mainActivity, "last state:" + transitionType+ " " + activityType + " " + isRunning, Toast.LENGTH_LONG).show();

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
}
