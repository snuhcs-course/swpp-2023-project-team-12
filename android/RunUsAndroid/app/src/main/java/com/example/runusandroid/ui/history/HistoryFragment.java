package com.example.runusandroid.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runusandroid.databinding.FragmentHistoryBinding;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HistoryViewModel historyViewModel =
                new ViewModelProvider(this).get(HistoryViewModel.class);

        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        final CalendarView calendarView = binding.historyCalendar;

        final TextView totalDistance = binding.totalDistance;
        final TextView totalTime = binding.totalTime;
        final TextView totalKcal = binding.totalKcal;

        final TextView dailyDistance = binding.dailyDistance;
        final TextView dailyTime = binding.dailyTime;
        final TextView dailyKcal = binding.dailyKcal;

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                // TODO: 월 변경에 따라 월별 데이터를 가져와서 해당 TextView들을 업데이트하는 코드를 추가합니다.
                totalDistance.setText(/*월별 거리 데이터*/ "Month Distance: XXX km");
                totalTime.setText(/*월별 시간 데이터*/ "Month Time: XX hrs");
                totalKcal.setText(/*월별 칼로리 데이터*/ "Month Kcal: XXX kcal");

                // TODO: 일자 변경에 따라 일별 데이터를 가져와서 해당 TextView들을 업데이트하는 코드를 추가합니다.
                dailyDistance.setText(/*일별 거리 데이터*/ "Day Distance: XX km");
                dailyTime.setText(/*일별 시간 데이터*/ "Day Time: X hrs");
                dailyKcal.setText(/*일별 칼로리 데이터*/ "Day Kcal: XX kcal");
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
