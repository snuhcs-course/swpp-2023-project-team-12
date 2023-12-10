package com.runus.runusandroid.ui.home;

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

import com.runus.runusandroid.HistoryApi;
import com.runus.runusandroid.R;
import com.runus.runusandroid.RetrofitClient;
import com.runus.runusandroid.databinding.FragmentHomeBinding;
import com.runus.runusandroid.ui.multi_mode.SocketManager;

import java.util.Calendar;
import java.util.Random;

public class HomeFragment extends Fragment {

    private final SocketManager socketManager = SocketManager.getInstance();
    private FragmentHomeBinding binding;
    private HistoryApi historyApi;

    private TextView todayQuotesText;
    private TextView todayQuotesAuthorText;

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

        todayQuotesText = root.findViewById(R.id.todayQuotesText);
        todayQuotesAuthorText = root.findViewById(R.id.todayQuotesAuthorText);
        String[] quote = getRandomQuotes().split("/");
        todayQuotesText.setText(quote[0]);
        todayQuotesAuthorText.setText(quote[1]);

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
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        historyApi = RetrofitClient.getClient().create(HistoryApi.class);

        return root;
    }

    /*
    @Override
    public void onResume() {
        super.onResume();
        if (socketManager.getOIS() == null) {
            new Thread(() -> {
                try {
                    socketManager.openSocket();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }).start();
        }
    }

     */

    private String getRandomQuotes() {
        // Random 객체 생성
        Random random = new Random();

        // 1부터 10까지의 랜덤 숫자 선택
        int randomNumber = random.nextInt(10);

        String[] quotes = new String[10];
        quotes[0] = "\n\n내가 잘 뛰는 것은 타고났다기 보다는 노력했기 때문이다. /\n\n\n\n\n- 이봉주";
        quotes[1] = "\n\n달리기는 내게 명상이며, 순화된 정신이고, 우주와의 교류, 기분 전환제이며 영혼의 교감이다. /\n\n\n\n\n- 로레인 몰러";
        quotes[2] = "\n\n물고기는 헤엄치고 새는 날고 인간은 달린다. /\n\n\n\n\n- 에밀 자토펙";
        quotes[3] = "\n\n운동화 한 켤레 후다닥 신고 문 밖으로 달려 나가면, 당신이 있는 곳이 바로 여기, 자유. /\n\n\n\n\n- 좀 제론";
        quotes[4] = "\n\n달리기는 인생에 대한 가장 위대한 비유이다. 당신이 달리기에 쏟아붓는 것을 결국 다 돌려받기 때문이다. /\n\n\n\n\n- 오프라 윈프리";
        quotes[5] = "\n\n내가 인생에서 배워야 할 모든 것을 마라톤에서 배웠다. 마라톤은 인생 이상이다. /\n\n\n\n\n- 김봉조";
        quotes[6] = "\n\n대회는 빠른 주자만을 위한 것이 아니다. 그것은 달리기를 지속하는 사람을 위한 것이다. /\n\n\n\n\n- 나이키";
        quotes[7] = "\n\n그 어떤 쾌락도 \"변화 또는 다양성\"이 없다면 참기 힘들 것이다. /\n\n\n\n\n- 막심";
        quotes[8] = "\n\n꿈을 가져라, 계획을 세워라. 그리고 그것을 향해 나아가라. 약속하건대, 그러면 당신은 거기에 이를 것이다. /\n\n\n\n\n- 조 코플로비츠";
        quotes[9] = "\n\n기억하라. 잘 달린 뒤의 기분이 달리기를 할 생각만 하면서 하는 일 없이 지낸 뒤의 기분보다 훨씬 좋다는 것을. /\n\n\n\n\n- 사라 콘도르";

        return quotes[randomNumber];

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}