package com.example.runusandroid;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.MatcherAssert.assertThat;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivity2Tests {

    // Launch desired activity (what for fragment?)
    @Rule
    public ActivityScenarioRule<MainActivity2> mActivityRule =
            new ActivityScenarioRule<>(MainActivity2.class);

    public void navSingle_click_displayed() {
        // perform action (click)
        onView(withId(R.id.navigation_single_mode)).perform(click());

        //check if UI changed
        onView(withId(R.id.nav_host_fragment_activity_main2)).check(matches(isDisplayed()));
        onView(withId(R.id.navigation_single_mode)).check(matches(isDisplayed()));
    }

    @Test
    public void draft(){
        // perform action (click)
        onView(withId(R.id.startButton)).perform(click());

        //check if UI changed
        onView(withId(R.id.buttonTimeAttack)).check(matches(isDisplayed()));
    }
}
