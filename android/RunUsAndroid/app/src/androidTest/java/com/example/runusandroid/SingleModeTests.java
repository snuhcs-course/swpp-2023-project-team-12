package com.example.runusandroid;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static org.hamcrest.Matchers.allOf;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static java.util.EnumSet.allOf;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.example.runusandroid.ActivityRecognition.UserActivityBroadcastReceiver;
import com.example.runusandroid.ActivityRecognition.UserActivityTransitionManager;
import com.google.android.gms.location.LocationServices;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SingleModeTests {
    public static ActivityScenario<LoginActivity> loginActivityScenario;
    public ActivityScenario<MainActivity2> mainActivityScenario;
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
//    @Rule
//    public ActivityScenarioRule<MainActivity2> mActivityRule =
//            new ActivityScenarioRule<>(MainActivity2.class);


    @BeforeClass
    public static void beforeClass() throws InterruptedException{
        loginActivityScenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.IdInput))
                .perform(typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.PasswordInput))
                .perform(typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.LoginBtn))
                .perform(click());
        Thread.sleep(1000);
    }


    @Before
    public void before() {

        mainActivityScenario = ActivityScenario.launch(MainActivity2.class);
        mainActivityScenario.onActivity(activity -> {
            activity.navController.navigate(R.id.navigation_single_mode);
        });
    }

    @After
    public void after() {
        mainActivityScenario.close();
    }

    @Test
    public void startButton_dialog_selection() throws InterruptedException {
        Thread.sleep(1000);
        // perform action and check (click on start)
        onView(withId(R.id.startButton)).perform(click());
        onView(withId(R.id.buttonTimeAttack)).check(matches(isDisplayed()));

        // check time attack button
        onView(withId(R.id.buttonTimeAttack)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.buttonConfirm)).check(matches(isDisplayed()));
        Thread.sleep(1000);
        onView(withId(R.id.buttonClose)).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return allOf(isEnabled(), isClickable());
                    }

                    @Override
                    public String getDescription() {
                        return "click";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        view.performClick();
                    }
                }
        );

        // TODO: 다른 dialog select 시

//        // check time attack button ...
//        onView(withId(R.id.buttonTimeAttack)).perform(click());
//        onView(withId(R.id.buttonConfirm)).check(matches(isDisplayed()));
//        onView(withId(R.id.buttonClose)).perform(click());
    }

    @Test
    public void playing() throws InterruptedException {
        String firstHistoryTime;
        // check history before running -> then move to desired fragment
        mainActivityScenario.onActivity(activity -> {
            activity.navController.navigate(R.id.navigation_history);
        });
        Thread.sleep(20000);
        onView(withId(R.id.dailyTime)).check(matches(not(withText(""))));
        firstHistoryTime = getText(withId(R.id.dailyTime));
        Log.d("History_log_test","firstHistoryTime: " + firstHistoryTime);
        assertNotNull(firstHistoryTime);
        mainActivityScenario.onActivity(activity -> {
            activity.navController.navigate(R.id.navigation_single_mode);
        });

        Thread.sleep(1000);
        // enter playing screen
        onView(withId(R.id.startButton)).perform(click());
        onView(withId(R.id.buttonTimeAttack)).perform(click());
        onView(withId(R.id.buttonConfirm)).perform(click());
        onView(withId(R.id.quitButton)).check(matches(isDisplayed()));
        Thread.sleep(2000);
        // check if time is running
        onView(withId(R.id.currentTimeText)).check(matches(not(withText("00:00:00"))));
        // check if back button warning works
        onView(isRoot()).perform(pressBack());
        onView(withId(R.id.buttonConfirmClose)).check(matches(isDisplayed()));
        onView(isRoot()).perform(pressBack());
        // 1분 뒤 종료
        Thread.sleep(1000 * 60);
        onView(withId(R.id.quitButton)).perform(click());
        onView(withId(R.id.buttonConfirmClose)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonConfirmClose)).perform(click());
        Thread.sleep(1000);
        onView(anyOf(withId(R.id.buttonConfirmFailure),withId(R.id.buttonConfirm))).perform(click());
        Thread.sleep(1000);

        // 종료시 레벨업할수도 있음
        try{
            onView(withText("레벨 업!")).check(matches(isDisplayed()));
            onView(withId(R.id.buttonConfirm)).perform(click());
        } catch (Exception e) {
        }

        // 종료하고 처음 화면으로 돌아감
        onView(withId(R.id.resultText)).check(matches(isDisplayed()));
        onView(withId(R.id.quitButton)).perform(click());
        onView(withId(R.id.buttonConfirmPlayExit)).perform(click());
        onView(withId(R.id.startButton)).check(matches(isDisplayed()));

        // check history change after running
        mainActivityScenario.onActivity(activity -> {
            activity.navController.navigate(R.id.navigation_history);
        });
        Thread.sleep(20000);
        onView(withId(R.id.dailyTime)).check(matches(not(withText(""))));
        onView(withId(R.id.dailyTime)).check(matches(not(withText(firstHistoryTime))));
    }

    // helper function to extract text content from viewInteraction
    public static String getText(final Matcher<View> matcher) {
        try {
            final String[] stringHolder = {null};
            onView(matcher).perform(new ViewAction() {
                @Override
                public Matcher<View> getConstraints() {
                    return isAssignableFrom(TextView.class);
                }

                @Override
                public String getDescription() {
                    return "get text";
                }

                @Override
                public void perform(UiController uiController, View view) {
                    TextView tv = (TextView) view;
                    stringHolder[0] = tv.getText().toString();
                }
            });
            if (stringHolder[0] == null || stringHolder[0] == "") {
                fail("no text found");
            }
            return stringHolder[0];
        } catch (Exception e) {
            fail("null found");
            return null;
        }

    }

}
