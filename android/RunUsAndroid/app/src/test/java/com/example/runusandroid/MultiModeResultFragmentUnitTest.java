//package com.example.runusandroid;
//
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//
//import androidx.navigation.NavController;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import com.example.runusandroid.ui.multi_mode.MultiModeResultFragment;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//@RunWith(AndroidJUnit4.class)
//public class MultiModeResultFragmentTest {
//
//    @Mock
//    private NavController navController;
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//        // NavController 모의 객체 생성
//        navController = mock(NavController.class);
//    }
//
//    @Test
//    public void testPlayLeaveButtonClick() {
//        // 테스트 대상 프래그먼트 생성
//        MultiModeResultFragment fragment = new MultiModeResultFragment();
//        fragment.setArguments(new Bundle());
//
//        // NavController 모의 객체 설정
//        fragment.navController = navController;
//
//        // "playLeaveButton" 버튼 클릭 시
//        fragment.playLeaveButton.performClick();
//
//        // NavController가 올바른 목적지로 이동하는지 검증
//        verify(navController).navigate(R.id.navigation_multi_mode);
//    }
//}