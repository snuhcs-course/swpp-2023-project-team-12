package com.example.runusandroid.ui.home;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.runusandroid.R;
import com.example.runusandroid.RetrofitClient;
import com.example.runusandroid.databinding.FragmentHomeBinding;
import com.example.runusandroid.ui.multi_mode.SocketManager;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private final SocketManager socketManager = SocketManager.getInstance();
    private FragmentHomeBinding binding;
    private HistoryApi historyApi;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        String nickname = sharedPreferences.getString("nickname", "사용자");
        long userId = sharedPreferences.getLong("userid", -1);
        TextView welcomeText = root.findViewById(R.id.welcomeText);
        welcomeText.setText(nickname + "님, 오늘의 운동을 시작해 보세요!");

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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}