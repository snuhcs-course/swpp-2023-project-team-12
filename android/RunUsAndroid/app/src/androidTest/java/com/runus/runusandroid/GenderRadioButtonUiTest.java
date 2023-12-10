package com.runus.runusandroid;

import static org.junit.Assert.assertEquals;

import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class GenderRadioButtonUiTest {

    @Rule
    public ActivityScenarioRule<SignUpStep3Activity> activityRule = new ActivityScenarioRule<>(SignUpStep3Activity.class);

    @Test
    public void radioButtonSelectionTest() {
        activityRule.getScenario().onActivity(activity -> {
            RadioGroup radioGroup = activity.findViewById(R.id.radioGroupGender);
            RadioButton maleButton = activity.findViewById(R.id.radioButtonMale);
            RadioButton femaleButton = activity.findViewById(R.id.radioButtonFemale);

            // 남성 선택 시뮬레이션
            radioGroup.check(maleButton.getId());
            assertEquals("남성", activity.getSelectedGenderText());

            // 여성 선택 시뮬레이션
            radioGroup.check(femaleButton.getId());
            assertEquals("여성", activity.getSelectedGenderText());
        });
    }
}
