package com.example.runusandroid;

import static org.junit.Assert.assertEquals;

import com.example.runusandroid.ui.multi_mode.MultiModeWaitFragment;

import org.junit.Before;
import org.junit.Test;

public class MultiModeWaitFragmentUnitTest {
    private MultiModeWaitFragment fragment;

    @Before
    public void setUp() {
        fragment = new MultiModeWaitFragment();
    }

    @Test
    public void testUpdateParticipantCount() {
        // 테스트 데이터 설정
        int size = 3;
        int total = 5;

        // 메서드 호출
        fragment.updateParticipantCount(size, total);

        // 예상 결과와 실제 결과 비교
        String expectedText = "3/5";
        assertEquals(expectedText, fragment.getParticipantCountTextView().getText().toString());
    }

//    @Test
//    public void testAddUserNameToWaitingList() {
//        // 테스트 데이터 설정
//        String userName = "TestUser";
//
//        // 메서드 호출
//        fragment.addUserNameToWaitingList(userName);
//
//        // 대기 목록에 해당 유저 이름이 추가되었는지 확인
//        TextView lastTextView = (TextView) fragment.waitingListBox.getChildAt(fragment.waitingListBox.getChildCount() - 1);
//        assertEquals(userName, lastTextView.getText().toString());
//    }
//
//    @Test
//    public void testRemoveUserNameFromWaitingList() {
//        // 테스트 데이터 설정
//        String userNameToRemove = "TestUser";
//        String userName1 = "User1";
//        String userName2 = "User2";
//
//        // 대기 목록에 유저 이름 추가
//        fragment.addUserNameToWaitingList(userName1);
//        fragment.addUserNameToWaitingList(userNameToRemove);
//        fragment.addUserNameToWaitingList(userName2);
//
//        // 메서드 호출
//        fragment.removeUserNameFromWaitingList(userNameToRemove);
//
//        // 대기 목록에서 해당 유저 이름이 제거되었는지 확인
//        boolean userNameFound = false;
//        int childCount = fragment.waitingListBox.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            TextView userNameTextView = (TextView) fragment.waitingListBox.getChildAt(i);
//            if (userNameToRemove.equals(userNameTextView.getText().toString())) {
//                userNameFound = true;
//                break;
//            }
//        }
//
//        assertFalse(userNameFound);
//    }
}
