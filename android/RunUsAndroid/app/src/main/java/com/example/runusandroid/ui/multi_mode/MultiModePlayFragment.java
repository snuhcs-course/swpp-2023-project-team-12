package com.example.runusandroid.ui.multi_mode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.runusandroid.GroupHistoryData;
import com.example.runusandroid.HistoryApi;
import com.example.runusandroid.HistoryData;
import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.example.runusandroid.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.Protocol;
import MultiMode.UserDistance;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MultiModePlayFragment extends Fragment {
    private final List<LatLng> pathPoints = new ArrayList<>();
    private final List<Float> speedList = new ArrayList<>(); // 매 km 마다 속력 (km/h)
    LocalDateTime iterationStartTime;
    MultiModeUser user = MultiModeFragment.user;
    SocketManager socketManager = SocketManager.getInstance();
    //MultiModeUser user = new MultiModeUser(1, "choco");
    //MultiModeUser user = new MultiModeUser(2, "berry"); // 유저 정보 임시로 더미데이터 활용
    //MultiModeUser user = new MultiModeUser(3, "apple");
    ObjectOutputStream oos;
    MultiModeRoom selectedRoom;
    float distance = 0;

    float calories = 0;
    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    TextView paceGoalContentTextView;
    MainActivity2 mainActivity;
    TextView timeGoalContentTextView;
    TextView goldDistanceTextView;
    TextView goldNickNameTextView;
    TextView silverDistanceTextView;
    TextView silverNickNameTextView;
    TextView bronzeDistanceTextView;
    TextView bronzeNickNameTextView;
    ProgressBar progressBar;
    LocalDateTime gameStartTime;
    TextView distancePresentContentTextView; //API 사용해서 구한 나의 현재 이동 거리
    TextView pacePresentContentTextView; //API 사용해서 구한 나의 현재 페이스
    Button playLeaveButton;
    SocketListenerThread socketListenerThread = MultiModeWaitFragment.socketListenerThread;
    Handler timeHandler;
    Runnable timeRunnable;
    Handler sendDataHandler;
    Runnable sendDataRunnable;
    UserDistance[] userDistances;
    private float minSpeed;
    private float maxSpeed;
    private float medianSpeed;
    private HistoryApi historyApi;
    private TextView timePresentContentTextView;
    private int isFinished;
    private ObjectInputStream ois;
    private int groupHistoryId = 999;
    private SendFinishedTask finishedTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socketListenerThread.addPlayFragment(this);
        socketListenerThread.resumeListening();
        gameStartTime = (LocalDateTime) getArguments().getSerializable("startTime");
        //Log.d("currentTime", gameStartTime + "");
        //경과 시간 업데이트
        timeHandler = new Handler(Looper.getMainLooper());

        // Runnable을 사용하여 매 초마다 시간 업데이트
        isFinished = 0;
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                if (selectedRoom != null && isFinished==0) {
                    LocalDateTime currentTime = LocalDateTime.now();
                    Duration present = Duration.between(gameStartTime, currentTime);
                    long secondsElapsed = present.getSeconds();

                    // 시간, 분, 초로 변환
                    long hours = secondsElapsed / 3600;
                    long minutes = (secondsElapsed % 3600) / 60;
                    long seconds = secondsElapsed % 60;

                    if (present.getSeconds() > selectedRoom.getDuration().getSeconds()) {
                        isFinished = 1;
                    } else {
                        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                                hours, minutes, seconds);
                        timePresentContentTextView.setText(formattedTime);
                    }
                }
                if (isFinished == 0) {
                    timeHandler.postDelayed(this, 1000);
                } else if(isFinished == 1){
                    Packet requestPacket = new Packet(Protocol.FINISH_GAME, user, selectedRoom);
                    finishedTask = new SendFinishedTask();
                    finishedTask.execute(requestPacket);
                    isFinished = 2;
                }

            }
        };
        timeHandler.post(timeRunnable);
        selectedRoom = (MultiModeRoom) getArguments().getSerializable("room");
        iterationStartTime = gameStartTime;
        historyApi = RetrofitClient.getClient().create(HistoryApi.class);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("response", "createPlayFragment");


        mainActivity = (MainActivity2) getActivity();
        locationRequest = LocationRequest.create();
        // TODO: let's find out which interval has best tradeoff
        locationRequest.setInterval(1000); // Update interval in milliseconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        maxSpeed = 0;
        minSpeed = 999;
        View view = inflater.inflate(R.layout.fragment_multi_room_play, container, false); //각종 view 선언
        if (selectedRoom != null) {
            timeGoalContentTextView = view.findViewById(R.id.time_goal_content);
            timePresentContentTextView = view.findViewById(R.id.time_present_content);
            goldNickNameTextView = view.findViewById(R.id.gold_nickname);
            goldDistanceTextView = view.findViewById(R.id.gold_distance);
            silverNickNameTextView = view.findViewById(R.id.silver_nickname);
            silverDistanceTextView = view.findViewById(R.id.silver_distance);
            bronzeNickNameTextView = view.findViewById(R.id.bronze_nickname);
            bronzeDistanceTextView = view.findViewById(R.id.bronze_distance);
            distancePresentContentTextView = view.findViewById(R.id.distance_present_content);
            pacePresentContentTextView = view.findViewById(R.id.pace_present_content);
            progressBar = view.findViewById(R.id.linear_progress_bar);
            playLeaveButton = view.findViewById(R.id.play_leaveButton);
            //목표 시간 계산하기 위한 코드
            long secondsRemaining = selectedRoom.getDuration().getSeconds();

            // 시간, 분으로 변환
            long hours = secondsRemaining / 3600;
            long minutes = (secondsRemaining % 3600) / 60;
            long seconds = secondsRemaining % 60;
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                    hours, minutes, seconds);

            timeGoalContentTextView.setText(formattedTime);

        }

        playLeaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Packet requestPacket = new Packet(Protocol.EXIT_GAME, user, selectedRoom);
                new ExitGameTask().execute(requestPacket);
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_multi_mode);
            }
        });

        //TODO: only draw lines if running is started
        //TODO: doesn't update location when app is in background -> straight lines are drawn from the last location when app is opened again
        //TODO: lines are ugly and noisy -> need to filter out some points or smoothed
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
                    pathPoints.add(newPoint);

                    int lastDistanceInt = (int) distance;
                    // TODO: check calculate distance
                    if (newPoint != null) {
                        // first few points might be noisy && while activity is running (or walking)
                        if (pathPoints.size() > 5 && mainActivity.activityReceiver.getIsRunning()) {
                            Location lastLocation = new Location("");
                            lastLocation.setLatitude(pathPoints.get(pathPoints.size() - 2).latitude);
                            lastLocation.setLongitude(pathPoints.get(pathPoints.size() - 2).longitude);
                            // unit : meter -> kilometer
                            if (location.distanceTo(lastLocation) != 0) {
                                int paceMinute = (int) (1 / ((location.distanceTo(lastLocation) / (double) 1000) / 5)) / 60;
                                int paceSecond = (int) (1 / ((location.distanceTo(lastLocation) / (double) 1000) / 5)) % 60;
                                String paceString = String.format("%02d:%02d", paceMinute, paceSecond);
                                pacePresentContentTextView.setText(paceString);
                            } else {
                                String paceString = "--:--";
                                pacePresentContentTextView.setText(paceString);
                            }


                            distance += location.distanceTo(lastLocation) / (double) 1000;
                            if ((int) distance != lastDistanceInt) {
                                LocalDateTime currentTime = LocalDateTime.now();
                                Duration iterationDuration = Duration.between(iterationStartTime, currentTime);
                                long secondsDuration = iterationDuration.getSeconds();
                                float newPace = (float) (1.0 / (secondsDuration / 3600.0));
                                if (newPace > maxSpeed) maxSpeed = newPace;
                                if (newPace < minSpeed) minSpeed = newPace;
                                speedList.add(newPace);
                                iterationStartTime = currentTime;

                            }
                        }
                    }
                    // update distance text
                    distancePresentContentTextView.setText(String.format(Locale.getDefault(), "%.1f" + "km", distance));
                }
            }
        };

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity);


        //5초마다 현재 이동 거리 전송
        sendDataHandler = new Handler(Looper.getMainLooper());
        sendDataRunnable = new Runnable() {
            @Override
            public void run() {
                Packet requestPacket = new Packet(Protocol.UPDATE_USER_DISTANCE, user, distance);
                new SendDistanceTask().execute(requestPacket);
                if (isFinished == 0) {
                    // 1초마다 Runnable 실행
                    sendDataHandler.postDelayed(this, 5000); // 5초마다 전송. 처음에 socketlistnerthread 설정 때문에 약간의 딜레이가 필요할 듯 함
                }
            }
        };

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start sendDataHandler after 5 seconds
                sendDataHandler.post(sendDataRunnable);
            }
        }, 5000); // 최초에 경기 시작하고 5초 뒤에 전송.

        return view;

    }


    public void updateTop3UserDistance(UserDistance[] userDistances) { // 화면에 표시되는 top3 유저 정보 업데이트. socketListenerThread에서 사용
        UserDistance[] top3UserDistance = userDistances;
        for (int i = 0; i < userDistances.length; i++) {
            //Log.d("response", "In updateTop3UserDistance, top3user " + i + " : " + top3UserDistance[0].getUser().getNickName() + " , distance : " + userDistances[0].getDistance());
            //Log.d("response", "In updateTop3UserDistance, user " + i + " : " + userDistances[0].getUser().getNickName() + " , distance : " + userDistances[0].getDistance());
        }
        double goldDistance = 0;

        if (top3UserDistance.length >= 1) {
            goldNickNameTextView.setText(top3UserDistance[0].getUser().getNickName());
            goldDistance = top3UserDistance[0].getDistance();
            String goldDistanceString = String.format("%.3fkm", goldDistance);
            goldDistanceTextView.setText(goldDistanceString);

            if (top3UserDistance.length >= 2) {

                silverNickNameTextView.setText(top3UserDistance[1].getUser().getNickName());
                double silverDistance = top3UserDistance[1].getDistance();
                String silverDistanceString = String.format("%.3fkm", silverDistance);
                silverDistanceTextView.setText(silverDistanceString);

                if (top3UserDistance.length >= 3) {

                    bronzeNickNameTextView.setText(top3UserDistance[2].getUser().getNickName());
                    double bronzeDistance = top3UserDistance[2].getDistance();
                    String bronzeDistanceString = String.format("%.3fkm", bronzeDistance);
                    bronzeDistanceTextView.setText(bronzeDistanceString);
                }
            }
        }

        int progress = 0;
        if (goldDistance != 0) {
            progress = (int) ((int) distance / goldDistance) * 100;
        }
        progressBar.setProgress(progress);

    }

    @Override
    public void onResume() {
        super.onResume();
        //아래 코드에서 resume때 result fragment로 가는 이유?
        transitionToRusultFragment();
//        socketListenerThread = (SocketListenerThread) getArguments().getSerializable("socketListenerThread"); //waitFragment의 socketListenrThread객체 가져와서 이어서 사용

        Log.d("response", "start play screen");
        //top3UpdateHandler.postDelayed(sendDataRunnable, 5000);

        fusedLocationClient.removeLocationUpdates(locationCallback);

        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        }
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    @Override
    public void onPause() {
        super.onPause();
//        if (socketListenerThread != null) {
//            socketListenerThread.interrupt();
//        }
        fusedLocationClient.removeLocationUpdates(locationCallback);
        finishedTask.cancel(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 타이머가 더 이상 필요하지 않을 때 핸들러를 제거합니다.
        timeHandler.removeCallbacks(timeRunnable);
        sendDataHandler.removeCallbacks(sendDataRunnable);
    }

    void saveHistoryData(long groupHistoryId) throws JSONException {
        if ((int) minSpeed == 999) minSpeed = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String startTimeString = selectedRoom.getStartTime().format(formatter);
        String finishTimeString = LocalDateTime.now().format(formatter);
        long durationInSeconds = selectedRoom.getDuration().getSeconds();
        HistoryData requestData = new HistoryData(user.getId(), distance, durationInSeconds,
                true, startTimeString, finishTimeString, calories, true, maxSpeed, minSpeed, calculateMedian(speedList), speedList, groupHistoryId);

        historyApi.postHistoryData(requestData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("response", "Send History Success");
                    try {
                        socketManager.closeSocket();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    transitionToRusultFragment();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    void saveGroupHistoryData(UserDistance[] userDistances) throws JSONException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String startTimeString = selectedRoom.getStartTime().format(formatter);
        long durationInSeconds = selectedRoom.getDuration().getSeconds();
        GroupHistoryData requestData;
        if (userDistances.length == 3) {
            long firstPlaceUserId = userDistances[0].getUser().getId();
            float firstPlaceUserDistance = userDistances[0].getDistance();
            long secondPlaceUserId = userDistances[1].getUser().getId();
            float secondPlaceUserDistance = userDistances[1].getDistance();
            long thirdPlaceUserId = userDistances[2].getUser().getId();
            float thirdPlaceUserDistance = userDistances[2].getDistance();
            requestData = new GroupHistoryData(selectedRoom.getTitle(), startTimeString, durationInSeconds, selectedRoom.getUserList().size(), firstPlaceUserId, firstPlaceUserDistance, secondPlaceUserId, secondPlaceUserDistance, thirdPlaceUserId, thirdPlaceUserDistance);

        } else if (userDistances.length == 2) {
            long firstPlaceUserId = userDistances[0].getUser().getId();
            float firstPlaceUserDistance = userDistances[0].getDistance();
            long secondPlaceUserId = userDistances[1].getUser().getId();
            float secondPlaceUserDistance = userDistances[1].getDistance();
            requestData = new GroupHistoryData(selectedRoom.getTitle(), startTimeString, durationInSeconds, selectedRoom.getUserList().size(), firstPlaceUserId, firstPlaceUserDistance, secondPlaceUserId, secondPlaceUserDistance);

        } else {
            long firstPlaceUserId = userDistances[0].getUser().getId();
            float firstPlaceUserDistance = userDistances[0].getDistance();
            requestData = new GroupHistoryData(selectedRoom.getTitle(), startTimeString, durationInSeconds, selectedRoom.getUserList().size(), firstPlaceUserId, firstPlaceUserDistance);

        }

        historyApi.postGroupHistoryData(requestData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("response", "Send group History Success");
                    JSONObject responseBody = null;
                    String responseBodyString = null;
                    try {
                        responseBodyString = response.body().string();
                        responseBody = new JSONObject(responseBodyString);
                        Log.d("response", responseBodyString);
                        groupHistoryId = (int) responseBody.getLong("id");
                        Packet requestPacket = new Packet(Protocol.SAVE_GROUP_HISTORY, user, selectedRoom, groupHistoryId);
                        new SendSavedInfoTask().execute(requestPacket);
                    } catch (IOException | JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    public float calculateMedian(List<Float> numbers) {
        // 리스트를 정렬합니다.
        Collections.sort(numbers);

        int size = numbers.size();
        if (size == 0) return 0;

        // 리스트의 크기가 홀수인 경우 중간값을 반환합니다.
        if (size % 2 == 1) {
            return numbers.get(size / 2);
        } else {
            // 리스트의 크기가 짝수인 경우 중간 두 값을 더한 후 2로 나눈 값을 반환합니다.
            double middle1 = numbers.get((size - 1) / 2);
            double middle2 = numbers.get(size / 2);
            return (float) ((float) (middle1 + middle2) / 2.0);
        }
    }

    public void setSocketManager(SocketManager socketManager) {
        this.socketManager = socketManager;
    }

    public void setTimeHandler(Handler timeHandler) {
        this.timeHandler = timeHandler;
    }

    public void setSendDataHandler(Handler sendDataHandler) {
        this.sendDataHandler = sendDataHandler;
    }

    private void transitionToRusultFragment() {
        if (userDistances != null) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("room", selectedRoom);
            bundle.putSerializable("top3UserDistance", userDistances);
            bundle.putSerializable("userDistance", distance);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_multi_room_result, bundle);
        }
    }

    public class SendDistanceTask extends AsyncTask<Packet, Void, Boolean> { // 서버에 업데이트할 거리 정보 전송
        @Override
        public Boolean doInBackground(Packet... packets) {
            boolean success = true;
            try {
                if (packets.length > 0) {
                    // Get the first Packet to send from the parameters
                    Packet packetToSend = packets[0];

                    // Get the ObjectOutputStream from the socket manager
                    ObjectOutputStream oos = socketManager.getOOS();

                    // Write the Packet object to the output stream
                    oos.writeObject(packetToSend);
                    oos.flush();
                } else {
                    // Handle the case when no packets are provided
                    success = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
            return success;
        }

        // Handle the result of sending the packet
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                //Log.d("SendPacket", "Packet sent successfully! distance : " + distance);
            } else {
                //Log.d("SendPacket", "Failed to send packet! distance : " + distance);
            }
        }
    }

    //경기 시간이 종료되었을 경우 해당 유저의 레이스가 종료되었다는 패킷을 보냄
    public class SendFinishedTask extends AsyncTask<Packet, Void, Boolean> {
        Packet packet;

        @Override
        public Boolean doInBackground(Packet... packets) {
            boolean success = true;
            try {
                if (packets.length > 0) {
                    Packet requestPacket = packets[0];
                    ObjectOutputStream oos = socketManager.getOOS();
                    if(!isCancelled() && oos!=null) {
                        oos.writeObject(requestPacket);
                        oos.flush();
                    }
                    else {
                        success = false;
                        Log.d("Sendfinishedpacket", "task is cancelled or oos is null");
                    }
                } else {
                    success = false;
                }

            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
            return success;
        }

        //ExitGameTask 실행 결과에 따라 수행
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Log.d("SendfinishedPacket", "Packet sent successfully!");

            } else {
                Log.d("SendfinishedPacket", "Failed to send packet!");
            }
        }

    }

    public class SendSavedInfoTask extends AsyncTask<Packet, Void, Boolean> {
        Packet packet;

        @Override
        public Boolean doInBackground(Packet... packets) {
            boolean success = true;
            try {
                if (packets.length > 0) {
                    Packet requestPacket = packets[0];
                    ObjectOutputStream oos = socketManager.getOOS();
                    oos.writeObject(requestPacket);
                    oos.flush();

                } else {
                    success = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
            return success;
        }

        //ExitGameTask 실행 결과에 따라 수행
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Log.d("SavePacket", "Packet  savegroupinfo sent successfully!");

            } else {
                Log.d("SavePacket", "Failed to send packet!");
            }
        }

    }

    public class ExitGameTask extends AsyncTask<Packet, Void, Boolean> {
        Packet packet;
        @Override
        public Boolean doInBackground(Packet... packets) {
            boolean success = true;
            try {
                if (packets.length > 0) {
                    Packet requestPacket = packets[0];
                    ObjectOutputStream oos = socketManager.getOOS();
                    oos.writeObject(requestPacket);
                    oos.flush();
                } else {
                    success = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            } finally {
                try {
                    socketManager.closeSocket();
                } catch (IOException e) {
                    success = false;
                }
            }
            return success;
        }

        //ExitGameTask 실행 결과에 따라 수행
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Log.d("ExitSendPacket", "Packet sent successfully!");

            } else {
                Log.d("ExitSendPacket", "Failed to send packet!");
            }
        }

    }
}
