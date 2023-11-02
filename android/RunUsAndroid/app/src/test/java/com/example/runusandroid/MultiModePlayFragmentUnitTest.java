package com.example.runusandroid;

import static org.junit.Assert.assertEquals;

import com.example.runusandroid.ui.multi_mode.MultiModePlayFragment;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class MultiModePlayFragmentUnitTest {
    private MultiModePlayFragment fragment;

    @Before
    public void setUp() throws Exception {
        // 테스트 전에 필요한 초기화를 수행합니다.
        fragment = new MultiModePlayFragment();
    }

    @Test
    public void testCalculateMedian() {
        List<Float> numbers = new ArrayList<>();
        numbers.add(2.0f);
        numbers.add(4.0f);
        numbers.add(1.0f);
        numbers.add(5.0f);
        numbers.add(3.0f);

        float median = fragment.calculateMedian(numbers);

        assertEquals(3.0f, median, 0.01); // 예상 결과와 실제 결과를 비교하여 테스트합니다.
    }
}
