package com.example.runusandroid.ui.history;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.runusandroid.HistoryApi;
import com.example.runusandroid.HistoryDataforRendering;
import com.example.runusandroid.R;
import com.example.runusandroid.RetrofitClient;
import com.example.runusandroid.databinding.FragmentHistoryBinding;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private final List<HistoryDataforRendering.DailyData> monthlyData = new ArrayList<>();
    private FragmentHistoryBinding binding;
    private HistoryApi historyApi;
    private TextView totalDistance;
    private TextView totalTime;
    private TextView totalKcal;
    private TextView dailyDistance;
    private TextView dailyTime;
    private TextView dailyKcal;
    private CalendarView calendarView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HistoryViewModel historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("userid", -1);
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        calendarView = binding.historyCalendar;

        totalDistance = binding.totalDistance;
        totalTime = binding.totalTime;
        totalKcal = binding.totalKcal;

        dailyDistance = binding.dailyDistance;
        dailyTime = binding.dailyTime;
        dailyKcal = binding.dailyKcal;
        historyApi = RetrofitClient.getClient().create(HistoryApi.class);

        Calendar currentCalendar = Calendar.getInstance();
        int currentYear = currentCalendar.get(Calendar.YEAR);
        int currentMonth = currentCalendar.get(Calendar.MONTH) + 1; // Java의 Calendar는 월이 0부터 시작
        int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

        loadMonthlyData(currentYear, currentMonth, userId, () -> {
            loadDailyData(currentYear, currentMonth, currentDay); // 현재 날짜의 데일리 데이터 로드
        });

        calendarView.setOnForwardPageChangeListener(() -> {
            Calendar nextMonth = calendarView.getCurrentPageDate();
            loadMonthlyData(nextMonth.get(Calendar.YEAR), nextMonth.get(Calendar.MONTH) + 1, userId, null);
        });

        calendarView.setOnPreviousPageChangeListener(() -> {
            Calendar previousMonth = calendarView.getCurrentPageDate();
            loadMonthlyData(previousMonth.get(Calendar.YEAR), previousMonth.get(Calendar.MONTH) + 1, userId, null);
        });

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                loadDailyData(eventDay.getCalendar().get(Calendar.YEAR),
                        eventDay.getCalendar().get(Calendar.MONTH) + 1,
                        eventDay.getCalendar().get(Calendar.DAY_OF_MONTH));
            }
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadMonthlyData(int year, int month, long userId, Runnable onLoaded) {
        historyApi.getMonthlyData(year, month, userId).enqueue(new Callback<HistoryDataforRendering>() {
            @Override
            public void onResponse(Call<HistoryDataforRendering> call, Response<HistoryDataforRendering> response) {
                if (response.isSuccessful() && response.body() != null) {
                    HistoryDataforRendering data = response.body();
                    updateUIWithMonthlyData(data);
                    monthlyData.clear();
                    if (data.getDailyData() != null) {
                        monthlyData.addAll(data.getDailyData());
                        addMarkerToDaysWithHistory(monthlyData);
                    }
                    if (onLoaded != null) {
                        onLoaded.run(); // 추가적인 작업 수행
                    }
                }
            }

            @Override
            public void onFailure(Call<HistoryDataforRendering> call, Throwable t) {
                Log.e("HistoryFragment", "Error loading monthly data", t);
            }
        });
    }

    private void updateUIWithMonthlyData(HistoryDataforRendering data) {
        // 이번 달의 총 거리, 시간, 칼로리 업데이트
        totalDistance.setText("총 거리\n" + String.format("%.1f", data.getTotalDistance()) + " km");
        totalTime.setText("총 시간\n" + formatDuration(data.getTotalTime()));
        totalKcal.setText("총 칼로리\n" + (int) data.getTotalCalories() + " kcal");
    }

    private void loadDailyData(int year, int month, int day) {
        HistoryDataforRendering.DailyData dailyData = findDailyData(year, month, day);
        if (dailyData != null) {
            updateUIWithDailyData(dailyData);
        } else {
            resetDailyUI(); // 히스토리가 없는 날짜에 대한 UI 리셋
        }
    }

    private Calendar parseStringToCalendar(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = format.parse(dateString);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void updateUIWithDailyData(HistoryDataforRendering.DailyData data) {
        if (data != null) {
            dailyDistance.setText("거리\n" + String.format("%.1f", data.getDistance()) + " km");
            dailyTime.setText("시간\n" + formatDuration(data.getTime()));
            dailyKcal.setText("칼로리\n" + (int) data.getCalories() + " kcal");
        } else {
            resetDailyUI();
        }
    }


    private HistoryDataforRendering.DailyData findDailyData(int year, int month, int day) {
        if (monthlyData == null) {
            return null;
        }
        for (HistoryDataforRendering.DailyData data : monthlyData) {
            Calendar dataCalendar = parseStringToCalendar(data.getDate());
            if (dataCalendar != null &&
                    dataCalendar.get(Calendar.YEAR) == year &&
                    dataCalendar.get(Calendar.MONTH) == (month - 1) && // 월 비교 조정
                    dataCalendar.get(Calendar.DAY_OF_MONTH) == day) {
                return data;
            }
        }
        return null;
    }

    private String formatDuration(String duration) {
        // "HH:mm:ss" 형식의 문자열을 "시간 분"으로 변환
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(duration);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            return hours + "시간 " + minutes + "분";
        } catch (ParseException e) {
            e.printStackTrace();
            return "0시간 0분";
        }
    }

    private void addMarkerToDaysWithHistory(List<HistoryDataforRendering.DailyData> dailyData) {
        List<EventDay> events = new ArrayList<>();
        for (HistoryDataforRendering.DailyData dayData : dailyData) {
            Calendar calendar = parseStringToCalendar(dayData.getDate());
            if (calendar != null) {
                events.add(new EventDay(calendar, R.drawable.marker_default));
            }
        }
        calendarView.setEvents(events);
    }


    private void resetDailyUI() {
        dailyDistance.setText("거리\n0.0 km");
        dailyTime.setText("시간\n0시간 0분");
        dailyKcal.setText("칼로리\n0 kcal");
    }
}
