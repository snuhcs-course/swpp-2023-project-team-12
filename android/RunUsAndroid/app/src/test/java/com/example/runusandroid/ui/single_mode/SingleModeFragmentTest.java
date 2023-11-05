package com.example.runusandroid.ui.single_mode;
import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;



public class SingleModeFragmentTest extends TestCase {

    private SingleModeFragment fragment;

    @Before
    public void setUp() {
        fragment = new SingleModeFragment();
    }


    public void testSaveHistoryDataOnSingleMode() {

    }

    @Test
    public void testConvertTimetoHour() {
        String timeString = "01:00:00";
        float testedValue = fragment.convertTimetoHour(timeString);
        float expectedValue = 1f;
        assertEquals(expectedValue, testedValue, 0.001);
    }
}