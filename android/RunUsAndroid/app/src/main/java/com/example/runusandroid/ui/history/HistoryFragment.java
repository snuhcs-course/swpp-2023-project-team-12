package com.example.runusandroid.ui.history;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runusandroid.HistoryApi;
import com.example.runusandroid.HistoryDataforRendering;
import com.example.runusandroid.RetrofitClient;
import com.example.runusandroid.databinding.FragmentHistoryBinding;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private HistoryApi historyApi;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HistoryViewModel historyViewModel =
                new ViewModelProvider(this).get(HistoryViewModel.class);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("userid", -1);
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final CalendarView calendarView = binding.historyCalendar;

        final TextView totalDistance = binding.totalDistance;
        final TextView totalTime = binding.totalTime;
        final TextView totalKcal = binding.totalKcal;

        final TextView dailyDistance = binding.dailyDistance;
        final TextView dailyTime = binding.dailyTime;
        final TextView dailyKcal = binding.dailyKcal;
        historyApi = RetrofitClient.getClient().create(HistoryApi.class);

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        //NOTE: 디폴트로 오늘 날짜 보여주기 위함
        historyApi.getMonthlyData(year, month + 1, userId).enqueue(new Callback<HistoryDataforRendering>() {
            @Override
            public void onResponse(Call<HistoryDataforRendering> call, Response<HistoryDataforRendering> response) {
                if (response.isSuccessful()) {
                    Log.d("HistoryApi", "Response Success");
                    HistoryDataforRendering data = response.body();
                    totalDistance.setText("이달의 거리\n" + data.getDistance() + " km");
                    totalTime.setText("이달의 시간\n" + data.getTime());
                    totalKcal.setText("이달의 칼로리\n" + data.getCalories() + " kcal");
                }
            }

            @Override
            public void onFailure(Call<HistoryDataforRendering> call, Throwable t) {
                Log.d("HistoryApi", "Response Failed");
                // 오류 처리
            }
        });

        //NOTE: 디폴트로 오늘 날짜 보여주기 위함
        historyApi.getDailyData(year, month + 1, dayOfMonth, userId).enqueue(new Callback<HistoryDataforRendering>() {
            @Override
            public void onResponse(Call<HistoryDataforRendering> call, Response<HistoryDataforRendering> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("HistoryAPI", "Response received");
                    HistoryDataforRendering data = response.body();
                    dailyDistance.setText("거리\n" + data.getDistance() + " km");
                    dailyTime.setText("시간\n" + data.getTime());
                    dailyKcal.setText("칼로리\n" + data.getCalories() + " kcal");
                }
            }

            @Override
            public void onFailure(Call<HistoryDataforRendering> call, Throwable t) {
                Log.d("HistoryAPI", "Response Failed");
                // 오류 처리
            }
        });

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Log.d("CalendarView", "Date changed: Year: " + year + " Month: " + month + " Day: " + dayOfMonth);
                // 월별 데이터 요청
                historyApi.getMonthlyData(year, month + 1, userId).enqueue(new Callback<HistoryDataforRendering>() {
                    @Override
                    public void onResponse(Call<HistoryDataforRendering> call, Response<HistoryDataforRendering> response) {
                        if (response.isSuccessful()) {
                            Log.d("HistoryApi", "Response Success");
                            HistoryDataforRendering data = response.body();
                            totalDistance.setText("이달의 거리\n" + data.getDistance() + " km");
                            totalTime.setText("이달의 시간\n" + data.getTime());
                            totalKcal.setText("이달의 칼로리\n" + data.getCalories() + " kcal");
                        }
                    }

                    @Override
                    public void onFailure(Call<HistoryDataforRendering> call, Throwable t) {
                        Log.d("HistoryApi", "Response Failed");
                        // 오류 처리
                    }
                });

                // 일별 데이터 요청
                historyApi.getDailyData(year, month + 1, dayOfMonth, userId).enqueue(new Callback<HistoryDataforRendering>() {
                    @Override
                    public void onResponse(Call<HistoryDataforRendering> call, Response<HistoryDataforRendering> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("HistoryAPI", "Response received");
                            HistoryDataforRendering data = response.body();
                            dailyDistance.setText("거리\n" + data.getDistance() + " km");
                            dailyTime.setText("시간\n" + data.getTime());
                            dailyKcal.setText("칼로리\n" + data.getCalories() + " kcal");
                        }
                    }

                    @Override
                    public void onFailure(Call<HistoryDataforRendering> call, Throwable t) {
                        Log.d("HistoryAPI", "Response Failed");
                        // 오류 처리
                    }
                });
            }
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
