package com.runus.runusandroid;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginTests {
    public static ActivityScenario<LoginActivity> loginActivityScenario;
    @Rule
    public ActivityScenarioRule<MainActivity2> mActivityRule =
            new ActivityScenarioRule<>(MainActivity2.class);
    @Rule
    public GrantPermissionRule permissionInternet = GrantPermissionRule.grant(android.Manifest.permission.INTERNET);
    @Rule
    public GrantPermissionRule permissionNetworkState = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_NETWORK_STATE);
    @Rule
    public GrantPermissionRule permissionActivityRecognition = GrantPermissionRule.grant(android.Manifest.permission.ACTIVITY_RECOGNITION);
    @Rule
    public GrantPermissionRule permissionCourseLocation = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_COARSE_LOCATION);
    @Rule
    public GrantPermissionRule permissionFineLocation = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    @Rule
    public GrantPermissionRule permissionBackgroundLocation = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION);
    @Rule
    public GrantPermissionRule permissionReadStorage = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule permissionWriteStorage = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule permissionPostNotification = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        Thread.sleep(1000);
        loginActivityScenario = ActivityScenario.launch(LoginActivity.class);
        onView(ViewMatchers.withId(R.id.IdInput))
                .perform(typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.PasswordInput))
                .perform(typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.LoginBtn))
                .perform(click());
        Thread.sleep(1000);
    }

    @Test
    public void logout_relogin() throws InterruptedException {
        Thread.sleep(1000);

        // move to usersetting fragment
        mActivityRule.getScenario().onActivity(activity -> {
            activity.navController.navigate(R.id.navigation_user_setting);
        });

        // perform logout
        onView(withId(R.id.logoutBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.logoutBtn)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.LoginBtn)).check(matches(isDisplayed()));

        // check relogin with other account

        onView(withId(R.id.IdInput))
                .perform(typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.PasswordInput))
                .perform(typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.LoginBtn))
                .perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.announcementText)).check(matches(isDisplayed()));

    }


}

