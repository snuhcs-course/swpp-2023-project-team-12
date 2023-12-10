package com.runus.runusandroid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import android.os.Handler;

import com.runus.runusandroid.ui.multi_mode.MultiModePlayFragment;
import com.runus.runusandroid.ui.multi_mode.SocketManager;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.PacketBuilder;
import MultiMode.Protocol;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class MultiModePlayFragmentUnitTest {

    HistoryApi historyApi;

    private MultiModePlayFragment fragment;
    //@Mock
    private SocketManager socketManager;
    @Mock
    private ObjectOutputStream oos;
    private MultiModeUser user;
    private float distance;
    private MultiModeRoom selectedRoom;


    @Before
    public void setUp() throws Exception {
        fragment = new MultiModePlayFragment();
        user = new MultiModeUser(1, "testUser", 0, "");
        distance = 10;
        socketManager = SocketManager.getInstance();
        selectedRoom = new MultiModeRoom();

        historyApi = RetrofitClient.getClient().create(HistoryApi.class);


    }

    @Test
    public void testCalculateMedianWithOddSize() {
        List<Float> numbers = new ArrayList<>();
        numbers.add(2.0f);
        numbers.add(4.0f);
        numbers.add(1.0f);
        numbers.add(5.0f);
        numbers.add(3.0f);

        float median = fragment.calculateMedian(numbers);

        assertEquals(3.0f, median, 0.01);
    }

    @Test
    public void testCalculateMedianWithEvenSize() {
        List<Float> numbers = new ArrayList<>();
        numbers.add(2.0f);
        numbers.add(4.0f);
        numbers.add(1.0f);
        numbers.add(5.0f);

        float median = fragment.calculateMedian(numbers);

        assertEquals(3.0f, median, 0.01);
    }

    @Test
    public void testSendDistanceTask() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder().buildProtocol(Protocol.UPDATE_USER_DISTANCE).buildUser(user).buildDistance(distance);
        Packet packet = packetBuilder.getPacket();
        //Packet packet = new Packet(Protocol.UPDATE_USER_DISTANCE, user, distance);

        // Mock 객체의 동작 정의
        //SocketManager socketManager = mock(SocketManager.class); // SocketManager의 Mock 객체 생성
        socketManager.openSocket();
        //when(socketManager.getOOS()).thenReturn(oos); // getOOS()가 호출될 때 oos(Mock 객체) 반환

        // fragment에 Mock 객체 주입
        fragment.setSocketManager(socketManager);

        // SendDistanceTask 실행
        boolean success = fragment.new SendDistanceTask().doInBackground(packet);

        // doInBackground의 결과를 검증
        assertTrue(success);

        // Packet이 ObjectOutputStream에 제대로 쓰였는지 검증
        //verify(oos).writeObject(packet);
        //verify(oos).flush();
        socketManager.closeSocket();
    }

    @Test
    public void testSendFinishedTask() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder().buildProtocol(Protocol.FINISH_GAME).buildUser(user).buildSelectedRoom(selectedRoom);
        Packet packet = packetBuilder.getPacket();
        //Packet packet = new Packet(Protocol.FINISH_GAME, user, selectedRoom);
        Handler timeHandlerMock = mock(Handler.class);
        Handler sendDataHandlerMock = mock(Handler.class);
        fragment.setTimeHandler(timeHandlerMock);
        fragment.setSendDataHandler(sendDataHandlerMock);
        socketManager.openSocket();
        fragment.setSocketManager(socketManager);
        boolean success = fragment.new SendFinishedTask().doInBackground(packet);
        assertTrue(success);
        socketManager.closeSocket();
    }

    @Test
    public void testSendSavedInfoTask() throws IOException {
        int groupHistoryId = 1;
        PacketBuilder packetBuilder = new PacketBuilder().buildProtocol(Protocol.SAVE_GROUP_HISTORY).buildUser(user).buildSelectedRoom(selectedRoom).buildGroupHistoryId(groupHistoryId);
        Packet packet = packetBuilder.getPacket();
        //Packet packet = new Packet(Protocol.SAVE_GROUP_HISTORY, user, selectedRoom, groupHistoryId);
        socketManager.openSocket();
        fragment.setSocketManager(socketManager);
        boolean success = fragment.new SendSavedInfoTask().doInBackground(packet);
        assertTrue(success);
        socketManager.closeSocket();
    }

    @Test
    public void testExitGameTask() throws IOException {
        PacketBuilder packetBuilder = new PacketBuilder().buildProtocol(Protocol.EXIT_GAME).buildUser(user).buildSelectedRoom(selectedRoom);
        Packet packet = packetBuilder.getPacket();
        //Packet packet = new Packet(Protocol.EXIT_GAME, user, selectedRoom);
        socketManager.openSocket();
        fragment.setSocketManager(socketManager);
        Handler timeHandlerMock = mock(Handler.class);
        Handler sendDataHandlerMock = mock(Handler.class);
        fragment.setTimeHandler(timeHandlerMock);
        fragment.setSendDataHandler(sendDataHandlerMock);
        boolean success = fragment.new ExitGameTask().doInBackground(packet);
        assertTrue(success);
        socketManager.closeSocket();

    }

    @Test
    public void testPostGroupHistoryData() {

        GroupHistoryData requestData = new GroupHistoryData("Test Room", "2023-11-03T13:06:33", 3600, 3, 1, 15.0f, 2, 12.0f, 3, 10.0f);
        Call<ResponseBody> call = historyApi.postGroupHistoryData(requestData);
        try {
            Response<ResponseBody> response = call.execute();

            assertTrue(response.isSuccessful());
        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException occurred");
        }

    }

    @Test
    public void testPostHistoryData() {

        try {
            HistoryData requestData = new HistoryData(1, 10.0f, 3600, true, "2023-11-03T13:06:33", "2023-11-03T13:06:33", 500, true, 15.0f, 0, 10.0f, new ArrayList<>(), 1, 0, 5);
            Call<ResponseBody> call = historyApi.postHistoryData(requestData);
            Response<ResponseBody> response = call.execute();

            assertTrue(response.isSuccessful());

        } catch (JSONException e) {
            e.printStackTrace();
            fail("IOException occurred");

        } catch (IOException e) {
            e.printStackTrace();
            fail("IOException occurred");
        }
    }
}
