package com.runus.runusandroid;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.actionWithAssertions;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.PickerActions.setTime;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.icu.util.Calendar;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.espresso.action.Tap;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MultiModeTests {
    public static ActivityScenario<LoginActivity> loginActivityScenario;
    private final ViewAction clickTopCentre = actionWithAssertions(new GeneralClickAction(
            Tap.SINGLE, GeneralLocation.TOP_CENTER, Press.FINGER, InputDevice.SOURCE_UNKNOWN, MotionEvent.BUTTON_PRIMARY));
    private final ViewAction clickBottomCentre = actionWithAssertions(new GeneralClickAction(
            Tap.SINGLE, GeneralLocation.BOTTOM_CENTER, Press.FINGER, InputDevice.SOURCE_UNKNOWN, MotionEvent.BUTTON_PRIMARY));
    private final ViewAction swipeDown = actionWithAssertions(new GeneralSwipeAction(
            Swipe.FAST, GeneralLocation.VISIBLE_CENTER, GeneralLocation.BOTTOM_CENTER, Press.FINGER));
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
//    @Rule
//    public ActivityScenarioRule<MainActivity2> mActivityRule =
//            new ActivityScenarioRule<>(MainActivity2.class);
    @Rule
    public GrantPermissionRule permissionReadStorage = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule permissionWriteStorage = GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
    @Rule
    public GrantPermissionRule permissionPostNotification = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        loginActivityScenario = ActivityScenario.launch(LoginActivity.class);
        onView(ViewMatchers.withId(R.id.IdInput))
                .perform(typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.PasswordInput))
                .perform(typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.LoginBtn))
                .perform(click());
        Thread.sleep(1000);
    }

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

    @Before
    public void before() {

        mainActivityScenario = ActivityScenario.launch(MainActivity2.class);
        mainActivityScenario.onActivity(activity -> {
            activity.navController.navigate(R.id.navigation_multi_mode);
        });
    }

    @After
    public void after() {
        mainActivityScenario.close();
    }

    @Test
    public void roomCreation_play() throws InterruptedException {
        String roomName = "UITestRoom";
        String enterMemberNum = "5.5";
        String checkMemberNum = "55";
        Calendar calendar;

        String firstHistoryTime;
        // check history before running -> then move to desired fragment
        mainActivityScenario.onActivity(activity -> {
            activity.navController.navigate(R.id.navigation_history);
        });
        Thread.sleep(1000);
        onView(withId(R.id.dailyTime)).check(matches(not(withText(""))));
        firstHistoryTime = getText(withId(R.id.dailyTime));
        Log.d("History_log_test", "firstHistoryTime: " + firstHistoryTime);
        assertNotNull(firstHistoryTime);
        mainActivityScenario.onActivity(activity -> {
            activity.navController.navigate(R.id.navigation_multi_mode);
        });

        // 방 생성
        onView(withId(R.id.createRoomButton)).perform(click());
        onView(withId(R.id.roomCreateTitle)).check(matches(isDisplayed()));
        // swipe as hell
        for (int i = 0; i < 29; i++) {
            onView(withId(R.id.minutePicker)).perform(swipeDown);
            Thread.sleep(50);
        }
        onView(withId(R.id.editTextGroupName)).perform(replaceText(roomName));
        onView(withId(R.id.editTextMembers)).perform(clearText());
        onView(withId(R.id.editTextMembers)).perform(typeText(enterMemberNum)).perform(closeSoftKeyboard());
        onView(withId(R.id.editTextMembers)).check(matches(withText(checkMemberNum)));
        calendar = Calendar.getInstance();
        onView(isAssignableFrom(TimePicker.class)).perform(setTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE) + 2));
        onView(withId(R.id.buttonComplete)).perform(click());

        // 대기중 화면에서 생성된 정보 확인
        Thread.sleep(1000 * 60);
        onView(withId(R.id.time_remaining)).check(matches(not(withText("잔여 시간"))));
        onView(withId(R.id.participant_count)).check(matches(withText(endsWith(checkMemberNum))));
        onView(withId(R.id.multi_room_wait_title)).check(matches(withText(roomName)));

        // 게임중 화면에서 확인
        Thread.sleep(1000 * 60);
        onView(withId(R.id.play_leaveButton)).check(matches(isDisplayed()));
        onView(withId(R.id.remain_time)).check(matches(not(withText("00:00:00"))));

        // TODO: 게임결과 화면 확인되지 않음

//        // check history change after running
//        mainActivityScenario.onActivity(activity -> {
//            activity.navController.navigate(R.id.navigation_history);
//        });
//        Thread.sleep(1000);
//        onView(withId(R.id.dailyTime)).check(matches(not(withText(""))));
//        onView(withId(R.id.dailyTime)).check(matches(not(withText(firstHistoryTime))));

    }


}

