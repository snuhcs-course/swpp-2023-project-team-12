package com.example.runusandroid;

import android.util.Log;

import java.time.Duration;

public class ExpSystem {
    //현재 남성 여성 나이 키 몸무게 등은 고려하지 않고,
    //달린 거리와 시간을 통해 구한 달린 속도, 총 거리, 싱글모드의 경우 미션 성공 여부(커스텀은 미션 실패로 봄), 멀티모드의 경우 등수(3등 이내만)를 가지고 가중치 부여
    //새롭게 구성된 UI를 보면 멀티모드에서 유저들의 레벨이 뜨기 때문에 레벨은 결국 절대적인 이 사람의 실력을 반영해야 한다고 생각하였음
    //러닝의 경우 나이나 몸무게가 절대적이지 않은 종목 중 하나라 유저의 성실도 + 절대적인 실력만 고려하면 되지 않을까 생각하여 성별, 키, 몸무게 등의 요소는 일단 배제

    ExpSystem() {
    }

    public static int getExp(String singleMode, double distance, Duration duration, int extraCheckValue) {
        double exp = 0;

        if (singleMode.equals("single")) {
            int MissonSuccessed = extraCheckValue;
            exp = 100;//달리기 평균 속도 10km/h(약 2.8m/s)를 default 속도로 설정
            long seconds = duration.getSeconds();
            double distanceInMeter = distance * 1000;
            if (seconds == 0) return (int) exp;

            //속도를 기준으로 값 설정
            double avgerage_speed = distanceInMeter / seconds;
            exp = exp * (avgerage_speed / 2.8);

            //거리는 1km를 기준으로 하여 1km 추가로 더 뛸 때마다 km당 1.05배의 가중치를 부여하여 합산. 등비수열의 합공식 이용
            exp = geometricSeries(exp, distance);


            //미션모드를 통해 러닝을 진행하여 성공했을 경우 추가 경험치(미션모드 많이 쓰라고)
            if (MissonSuccessed > 0) {
                exp *= 1.1;
            }
            Log.d("UAT:exp", "single " + distanceInMeter + " " + seconds + " " + avgerage_speed + " " + exp);
            return (int) exp;
        } else {
            int place = extraCheckValue;
            exp = 100;//달리기 평균 속도 10km/h(약 2.8m/s)를 default 속도로 설정
            long seconds = duration.getSeconds();
            double distanceInMeter = distance * 1000;
            if (seconds == 0) return (int) exp;

            //속도를 기준으로 값 설정
            double avgerage_speed = distanceInMeter / seconds;
            exp = exp * (avgerage_speed / 2.8);

            //거리는 1km를 기준으로 하여 1km 추가로 더 뛸 때마다 km당 1.05배의 가중치
            exp = geometricSeries(exp, distance);

            if (place == 1) {
                exp *= 1.6;
            } else if (place == 2) {
                exp *= 1.4;
            } else if (place == 3) {
                exp *= 1.2;
            }
            return (int) exp;
        }


    }


    //등비수열 합공식
    static double geometricSeries(double exp, double distance) {
        double processedExp = 0;
        for (int i = 0; i < (int) distance; i++) {
            processedExp += exp * Math.pow(1.05, i);
        }

        processedExp += (distance - (int) distance) * exp * Math.pow(1.05, (int) distance);

        return processedExp;
    }

    public static int getLevel(int exp) {
        int level = 0;

        if (exp > 300000) {
            level = 10;
        } else if (exp > 200000) {
            level = 9;
        } else if (exp > 120000) {
            level = 8;
        } else if (exp > 70000) {
            level = 7;
        } else if (exp > 40000) {
            level = 6;
        } else if (exp > 20000) {
            level = 5;
        } else if (exp > 10000) {
            level = 4;
        } else if (exp > 4000) {
            level = 3;
        } else if (exp > 1000) {
            level = 2;
        } else {
            level = 1;
        }

        return level;
    }

    public static int getPresentExp(int exp) {
        int presentExp = 0;

        if (exp > 300000) {
            presentExp = exp - 300000;
        } else if (exp > 200000) {
            presentExp = exp - 200000;
        } else if (exp > 120000) {
            presentExp = exp - 120000;
        } else if (exp > 70000) {
            presentExp = exp - 70000;
        } else if (exp > 40000) {
            presentExp = exp - 40000;
        } else if (exp > 20000) {
            presentExp = exp - 20000;
        } else if (exp > 10000) {
            presentExp = exp - 10000;
        } else if (exp > 4000) {
            presentExp = exp - 4000;
        } else if (exp > 1000) {
            presentExp = exp - 1000;
        } else {
            presentExp = exp;
        }

        return presentExp;
    }

    public static int getNextExp(int level) {
        int nextExp = 0;

        if (level == 10) {
            nextExp = 999999;
        } else if (level == 9) {
            nextExp = 100000;
        } else if (level == 8) {
            nextExp = 80000;
        } else if (level == 7) {
            nextExp = 50000;
        } else if (level == 6) {
            nextExp = 30000;
        } else if (level == 5) {
            nextExp = 20000;
        } else if (level == 4) {
            nextExp = 10000;
        } else if (level == 3) {
            nextExp = 6000;
        } else if (level == 2) {
            nextExp = 3000;
        } else {
            nextExp = 1000;
        }

        return nextExp;
    }


}

