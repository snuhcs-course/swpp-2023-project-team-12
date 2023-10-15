package com.example.runusandroid.ui.single_mode;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.databinding.FragmentSingleModeBinding;

public class SingleModeFragment extends Fragment {

    private FragmentSingleModeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SingleModeViewModel singleModeViewModel =
                new ViewModelProvider(this).get(SingleModeViewModel.class);

        binding = FragmentSingleModeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MainActivity2 mainActivity = (MainActivity2) getActivity();

        Button quitButton = (Button) binding.quitButton;

        // clicking the quit button will print location in log. for testing
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.getLastLocation();
            }
        });


        // Viewmodel contains status, and when status changes (observe), the text will change
        final TextView textView = binding.textSingleMode;
        singleModeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}