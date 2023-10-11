package com.example.runusandroid.ui.multi_mode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runusandroid.databinding.FragmentMultiModeBinding;

public class MultiModeFragment extends Fragment {

    private FragmentMultiModeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MultiModeViewModel multiModeViewModel =
                new ViewModelProvider(this).get(MultiModeViewModel.class);

        binding = FragmentMultiModeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textMultimode;
        multiModeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}