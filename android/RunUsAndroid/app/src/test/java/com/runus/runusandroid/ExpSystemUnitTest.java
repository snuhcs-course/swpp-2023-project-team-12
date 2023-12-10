package com.runus.runusandroid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.Duration;

public class ExpSystemUnitTest {

    @Test
    public void testGetExpSingleMode() {
        int exp = ExpSystem.getExp("single", 5.0, Duration.ofMinutes(30), 1);
        assertEquals(602, exp); // 예상되는 경험치 값과 비교
    }

    @Test
    public void testGetExpMultiMode() {
        int exp = ExpSystem.getExp("multi", 10.0, Duration.ofMinutes(60), 1);
        assertEquals(1996, exp); // 예상되는 경험치 값과 비교
    }

    @Test
    public void testGetLevel() {
        int level = ExpSystem.getLevel(30000);
        assertEquals(5, level); // 예상되는 레벨 값과 비교
    }

    @Test
    public void testGeometricSeries() {
        double result = ExpSystem.geometricSeries(100, 3.0);
        assertEquals(315.25, result, 0.001); // 예상되는 결과 값과 비교
    }
}
