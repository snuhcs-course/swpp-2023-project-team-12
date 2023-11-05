package com.example.runusandroid.ui.single_mode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.runusandroid.HistoryApi;
import com.example.runusandroid.HistoryData;
import com.example.runusandroid.RetrofitClient;

import junit.framework.TestCase;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;


public class SingleModeFragmentTest extends TestCase {
    HistoryApi historyApi;

    private SingleModeFragment fragment;

    @Before
    public void setUp() {

        fragment = new SingleModeFragment();
        historyApi = RetrofitClient.getClient().create(HistoryApi.class);
    }

    @Test
    public void testSaveHistoryDataOnSingleMode() {
        try {
            HistoryData requestData = new HistoryData(1, 10.0f, 3600, true, "2023-11-03T13:06:33", "2023-11-03T13:06:33", 500, false, 15.0f, 0, 10.0f, new ArrayList<>(), -1);
            Call<ResponseBody> call = historyApi.postHistoryData(requestData);
            Response<ResponseBody> response = call.execute();

            assertTrue(response.isSuccessful());

        } catch (JSONException e) {
            e.printStackTrace();
            fail("IOException occurred");

        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException occurred");
        }
    }

    @Test
    public void testCalculateMedianWithOddSize() {
        List<Float> numbers = new ArrayList<>();
        numbers.add(2.0f);
        numbers.add(4.0f);
        numbers.add(1.0f);
        numbers.add(5.0f);
        numbers.add(3.0f);

        float median = fragment.calculateMedian(numbers);

        assertEquals(3.0f, median, 0.01);
    }

    @Test
    public void testCalculateMedianWithEvenSize() {
        List<Float> numbers = new ArrayList<>();
        numbers.add(2.0f);
        numbers.add(4.0f);
        numbers.add(1.0f);
        numbers.add(5.0f);

        float median = fragment.calculateMedian(numbers);

        assertEquals(3.0f, median, 0.01);
    }

    @Test
    public void testConvertTimetoHour() {
        String timeString = "01:00:00";
        float testedValue = fragment.convertTimetoHour(timeString);
        float expectedValue = 1f;
        assertEquals(expectedValue, testedValue, 0.001);
    }
}