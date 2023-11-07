package com.example.runusandroid.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.runusandroid.HistoryApi;
import com.example.runusandroid.HistoryDataforRendering;
import com.example.runusandroid.R;
import com.example.runusandroid.RetrofitClient;
import com.example.runusandroid.databinding.FragmentHomeBinding;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private HistoryApi historyApi;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("username", "사용자");
        long userId = sharedPreferences.getLong("userid", -1);
        TextView welcomeText = root.findViewById(R.id.welcomeText);
        welcomeText.setText(userName + "님 RunUs에 오신 것을 환영해요!");

        ViewPager viewPager = root.findViewById(R.id.viewPager);
        HomePageAdapter adapter = new HomePageAdapter();
        viewPager.setAdapter(adapter);
        TextView pageIndicator = root.findViewById(R.id.pageIndicator);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // 스크롤할 때의 동작
            }

            @Override
            public void onPageSelected(int position) {
                String indicatorText = (position + 1) + "/" + adapter.getCount();
                pageIndicator.setText(indicatorText);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        pageIndicator.setText("1/" + adapter.getCount());

        CardView runningRecordCard = root.findViewById(R.id.runningRecordCard);
        TextView todayRunningHistoryTextView = root.findViewById(R.id.todayRunningHistoryTextView);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        historyApi = RetrofitClient.getClient().create(HistoryApi.class);

        // NOTE: 디폴트로 오늘 날짜 보여주기 위함
        historyApi.getMonthlyData(year, month + 1, userId).enqueue(new Callback<HistoryDataforRendering>() {
            @Override
            public void onResponse(Call<HistoryDataforRendering> call, Response<HistoryDataforRendering> response) {
                if (response.isSuccessful()) {
                    Log.d("HistoryApi", "Response Success");
                    HistoryDataforRendering data = response.body();
                    todayRunningHistoryTextView.setText("오늘의 달리기 기록\n\n" + "거리: " + data.getDistance() + " km\n\n시간: " + data.getTime());
                }
            }

            @Override
            public void onFailure(Call<HistoryDataforRendering> call, Throwable t) {
                Log.d("HistoryApi", "Response Failed");
                // 오류 처리
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