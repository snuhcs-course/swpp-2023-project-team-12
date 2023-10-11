package com.example.runusandroid.ui.user_setting;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runusandroid.databinding.FragmentUserSettingBinding;

public class UserSettingFragment extends Fragment {

    private FragmentUserSettingBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        UserSettingViewModel userSettingViewModel =
                new ViewModelProvider(this).get(UserSettingViewModel.class);

        binding = FragmentUserSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textUserSetting;
        userSettingViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}