package com.example.runusandroid;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginTests {

    @Rule
    public ActivityScenarioRule<LoginActivity> mActivityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Before
    public void before() throws InterruptedException {
        onView(withId(R.id.IdInput))
                .perform(typeText("test"));
        onView(withId(R.id.PasswordInput))
                .perform(typeText("test"));
        onView(withId(R.id.LoginBtn))
                .perform(click());
        Thread.sleep(1000);
    }


}

