package com.example.runusandroid.ui.multi_mode;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.runusandroid.AccountAPIFactory;
import com.example.runusandroid.ActivityRecognition.RunningState;
import com.example.runusandroid.ExpSystem;
import com.example.runusandroid.GroupHistoryData;
import com.example.runusandroid.HistoryApi;
import com.example.runusandroid.HistoryData;
import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.example.runusandroid.RetrofitClient;
import com.example.runusandroid.ui.single_mode.BackGroundLocationService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.PacketBuilder;
import MultiMode.Protocol;
import MultiMode.UserDistance;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MultiModePlayFragment extends Fragment {
    static final String START_LOCATION_SERVICE = "start";
    static final String STOP_LOCATION_SERVICE = "stop";
    private final List<LatLng> pathPoints = new ArrayList<>();
    private final List<Float> speedList = new ArrayList<>(); // 매 km 마다 속력 (km/h)
    private final Location lastLocation = null;
    LocalDateTime iterationStartTime;
    MultiModeUser user;
    SocketManager socketManager = SocketManager.getInstance();
    OnBackPressedCallback backPressedCallBack;
    MultiModeRoom selectedRoom;
    float distance = 0;
    float calories = 0;
    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    MainActivity2 mainActivity;
    TextView timeGoalContentTextView;
    TextView goldDistanceTextView;
    TextView goldNickNameTextView;
    TextView goldLevelTextView;
    TextView silverDistanceTextView;
    TextView silverNickNameTextView;
    TextView silverLevelTextView;
    TextView bronzeDistanceTextView;
    TextView bronzeNickNameTextView;
    TextView bronzeLevelTextView;
    ProgressBar firstRunnerProgressBar;
    ProgressBar secondRunnerProgressBar;
    ProgressBar thirdRunnerProgressBar;
    LocalDateTime gameStartTime;
    TextView distancePresentContentTextView; //API 사용해서 구한 나의 현재 이동 거리
    TextView pacePresentContentTextView; //API 사용해서 구한 나의 현재 페이스
    Button playLeaveButton;
    SocketListenerThread socketListenerThread = MultiModeFragment.socketListenerThread;
    Handler timeHandler;
    Runnable timeRunnable;
    Handler sendDataHandler;
    Runnable sendDataRunnable;
    UserDistance[] userDistances;
    double distance_for_pace;
    private Handler remainTimeHandler;
    private long playLeaveButtonLastClickTime = 0;
    private long backButtonLastClickTime = 0;
    private boolean userExit = false;
    private float minSpeed;
    private float maxSpeed;
    private long secondsRemaining;
    private int updatedExp;
    private HistoryApi historyApi;
    private TextView timePresentContentTextView;
    private int isFinished;
    private int groupHistoryId = 999;
    private SendFinishedTask finishedTask;
    private Runnable remainTimeRunnable;
    private GoogleMap mMap;
    private Queue<Double> pace_distance_queue;
    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("location_update")) {
                double latitude = intent.getDoubleExtra("latitude", 0.0);
                double longitude = intent.getDoubleExtra("longitude", 0.0);
                LatLng newPoint = new LatLng(latitude, longitude);
                pathPoints.add(newPoint);
                Location location = new Location("");
                location.setLatitude(latitude);
                location.setLongitude(longitude);
                int lastDistanceInt = (int) distance;

                // Update UI (draw line, zoom in)
                if (mMap != null) {
                    mMap.clear(); // Remove previous polylines
                    mMap.addPolyline(new PolylineOptions().addAll(pathPoints).color(Color.parseColor("#4AA570")).width(10));
                    if (newPoint != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPoint, 16));
                    }
                }

                // get distance
                if (newPoint != null && RunningState.getIsRunning()) {
                    // first few points might be noisy
                    if (pathPoints.size() > 1) {
                        Location lastLocation = new Location("");
                        lastLocation.setLatitude(pathPoints.get(pathPoints.size() - 2).latitude);
                        lastLocation.setLongitude(pathPoints.get(pathPoints.size() - 2).longitude);
                        // unit : meter -> kilometer
                        double last_distance_5s_kilometer = location.distanceTo(lastLocation) / (double) 1000;

                        distance += last_distance_5s_kilometer;
                        if (pace_distance_queue.size() < 3) {
                            pace_distance_queue.add(last_distance_5s_kilometer);
                        } else {
                            pace_distance_queue.poll();
                            pace_distance_queue.add(last_distance_5s_kilometer);
                        }
                        distance_for_pace = sumQueue();
                        //Log.d("test:distance:5sec", "Last 5 second Distance :" + location.distanceTo(lastLocation) / (double) 1000);
                        if (last_distance_5s_kilometer != 0) {
                            int paceMinute = (int) (1 / (distance_for_pace / (5 * pace_distance_queue.size()))) / 60;
                            int paceSecond = (int) (1 / (distance_for_pace / (5 * pace_distance_queue.size()))) % 60;
                            Log.d("test:distance:5sec", "distance : " + distance_for_pace + " and queue size is " + pace_distance_queue.size() + " pace :" + paceMinute + "' " + paceSecond + "''");
                            if (paceMinute < 60) {
                                String paceString = String.format("%02d'%02d\"", paceMinute, paceSecond);
                                pacePresentContentTextView.setText(paceString);
                            } else {
                                String paceString = "59'59\"";
                                pacePresentContentTextView.setText(paceString);
                            }
                        }
                        if ((int) distance != lastDistanceInt) {
                            LocalDateTime currentTime = LocalDateTime.now();
                            Duration iterationDuration = Duration.between(iterationStartTime, currentTime);
                            long secondsDuration = iterationDuration.getSeconds();
                            float newPace = (float) (1.0 / (secondsDuration / 3600.0));
                            if (newPace > maxSpeed)
                                maxSpeed = newPace;
                            if (newPace < minSpeed)
                                minSpeed = newPace;
                            speedList.add(newPace);
                            iterationStartTime = currentTime;

                        }
                    }
                } else {
                    String paceString = "--'--\"";
                    pacePresentContentTextView.setText(paceString);
                }
                distancePresentContentTextView.setText(String.format(Locale.getDefault(), "%.2f" + "km", distance));
                //lastLocation = location;
            }
        }
    };
    private int updatedBadgeCollection;

    private double sumQueue() {
        double sum = 0;
        Iterator<Double> iterator = pace_distance_queue.iterator();
        while (iterator.hasNext()) {
            sum += iterator.next();
        }
        return sum;
    }

    private void showExitGameDialog() {
        @SuppressLint("InflateParams")
        View exitGameDialog = getLayoutInflater().inflate(R.layout.dialog_multimode_play_finish, null);
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(exitGameDialog);
        Button buttonConfirmPlayExit = exitGameDialog.findViewById(R.id.buttonConfirmPlayExit);
        buttonConfirmPlayExit.setOnClickListener(v -> {
            dialog.dismiss();
            userExit = true;
            PacketBuilder packetBuilder = new PacketBuilder().protocol(Protocol.EXIT_GAME).user(user).selectedRoom(selectedRoom);
            Packet requestPacket = packetBuilder.getPacket();
            new ExitGameTask().execute(requestPacket);
        });
        dialog.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = MultiModeFragment.user;
        distance_for_pace = 0;
        pace_distance_queue = new LinkedList<>();
        finishedTask = new SendFinishedTask();
        gameStartTime = (LocalDateTime) getArguments().getSerializable("startTime");
        //경과 시간 업데이트
        timeHandler = new Handler(Looper.getMainLooper());
        remainTimeHandler = new Handler(Looper.getMainLooper());

        // Runnable을 사용하여 매 초마다 시간 업데이트
        isFinished = 0;
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                if (selectedRoom != null && isFinished == 0) {
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
                        if (timePresentContentTextView != null)
                            timePresentContentTextView.setText(formattedTime);
                    }
                }
                if (isFinished == 0) {
                    timeHandler.postDelayed(this, 1000);
                } else if (isFinished == 1) {
                    if (isVisible() && isAdded()) {
                        PacketBuilder packetBuilder = new PacketBuilder().protocol(Protocol.FINISH_GAME).user(user).selectedRoom(selectedRoom);
                        Packet requestPacket = packetBuilder.getPacket();
                        AccountAPIFactory accountFactory = AccountAPIFactory.getInstance();
                        accountFactory.refreshToken(MultiModePlayFragment.this.requireContext());
                        finishedTask.execute(requestPacket);
                    }
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
        maxSpeed = 0;
        minSpeed = 999;
        View view = inflater.inflate(R.layout.fragment_multi_room_play, container, false); //각종 view 선언
        distancePresentContentTextView = view.findViewById(R.id.distance_present_content);
        pacePresentContentTextView = view.findViewById(R.id.pace_present_content);
        timeGoalContentTextView = view.findViewById(R.id.remain_time);
        goldNickNameTextView = view.findViewById(R.id.first_runner_name);
        goldLevelTextView = view.findViewById(R.id.first_runner_level);
        goldDistanceTextView = view.findViewById(R.id.first_runner_km);
        silverNickNameTextView = view.findViewById(R.id.second_runner_name);
        silverLevelTextView = view.findViewById(R.id.second_runner_level);
        silverDistanceTextView = view.findViewById(R.id.second_runner_km);
        bronzeNickNameTextView = view.findViewById(R.id.third_runner_name);
        bronzeLevelTextView = view.findViewById(R.id.third_runner_level);
        bronzeDistanceTextView = view.findViewById(R.id.third_runner_km);

        // ProgressBars
        firstRunnerProgressBar = view.findViewById(R.id.first_runner_progress_bar);
        secondRunnerProgressBar = view.findViewById(R.id.second_runner_progress_bar);
        thirdRunnerProgressBar = view.findViewById(R.id.third_runner_progress_bar);
        if (selectedRoom != null) {
            timeGoalContentTextView = view.findViewById(R.id.remain_time);
            distancePresentContentTextView = view.findViewById(R.id.distance_present_content);
            pacePresentContentTextView = view.findViewById(R.id.pace_present_content);
            playLeaveButton = view.findViewById(R.id.play_leaveButton);
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.multi_mode_map);

            secondsRemaining = selectedRoom.getDuration().getSeconds(); // 목표 시간

            startRemainTimer();

            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                if (ActivityCompat.checkSelfPermission(mainActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
                // set initial point to on saved already in mainActivity, if null, set to default location (남산타워)
                LatLng initialPoint;
                if (mainActivity.initialLocation != null) {
                    initialPoint = new LatLng(mainActivity.initialLocation.getLatitude(), mainActivity.initialLocation.getLongitude());
                } else {
                    initialPoint = new LatLng(37.55225, 126.9873);
                }
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPoint, 16));
            });
        }

        // for debugging purpose, hidden button on right bottom corner shows toast about lastly detected activity transition and isRunning value
        Button hiddenButton = view.findViewById(R.id.hiddenButton);
        hiddenButton.setOnClickListener(v -> {
            boolean isRunning = RunningState.getIsRunning();
            String lastActivityType = RunningState.getLastActivityType();
            String lastTransitionType = RunningState.getLastTransitionType();

            //Toast.makeText(mainActivity, "last detected : " + lastTransitionType + " " + lastActivityType +
            //        " . isRunning " + isRunning, Toast.LENGTH_LONG).show();
        });

        playLeaveButton.setOnClickListener(v -> {
            if (SystemClock.elapsedRealtime() - playLeaveButtonLastClickTime < 1000) {
                return;
            }
            playLeaveButtonLastClickTime = SystemClock.elapsedRealtime();
            showExitGameDialog();
        });

        //5초마다 현재 이동 거리 전송
        sendDataHandler = new Handler(Looper.getMainLooper());
        sendDataRunnable = new Runnable() {
            @Override
            public void run() {
                PacketBuilder packetBuilder = new PacketBuilder().protocol(Protocol.UPDATE_USER_DISTANCE).user(user).distance(distance);
                Packet requestPacket = packetBuilder.getPacket();
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

    public void updateTop3UserDistance(UserDistance[] userDistances) {
        // 기본 설정: 모든 텍스트 뷰를 비웁니다.
        goldNickNameTextView.setText("");
        goldLevelTextView.setText("");
        goldDistanceTextView.setText("");
        silverNickNameTextView.setText("");
        silverLevelTextView.setText("");
        silverDistanceTextView.setText("");
        bronzeNickNameTextView.setText("");
        bronzeLevelTextView.setText("");
        bronzeDistanceTextView.setText("");

        // 기본 설정: 모든 프로그레스 바를 0으로 설정합니다.
        firstRunnerProgressBar.setProgress(0);
        secondRunnerProgressBar.setProgress(0);
        thirdRunnerProgressBar.setProgress(0);

        double firstDistance = 0, secondDistance = 0, thirdDistance = 0;

        // 1등 사용자 정보 업데이트
        if (userDistances.length >= 1) {
            MultiModeUser firstUser = userDistances[0].getUser();
            goldNickNameTextView.setText(firstUser.getNickName());
            goldLevelTextView.setText("Lv. " + firstUser.getLevel());
            firstDistance = userDistances[0].getDistance();
            goldDistanceTextView.setText(String.format("%.2fkm", firstDistance));
            firstRunnerProgressBar.setProgress(100); // 1등은 프로그레스 바를 항상 100%로 설정합니다.
        }

        // 2등 사용자 정보 업데이트
        if (userDistances.length >= 2) {
            MultiModeUser secondUser = userDistances[1].getUser();
            silverNickNameTextView.setText(secondUser.getNickName());
            silverLevelTextView.setText("Lv. " + secondUser.getLevel());
            secondDistance = userDistances[1].getDistance();
            silverDistanceTextView.setText(String.format("%.2fkm", secondDistance));
            // 2등의 프로그레스 바는 1등 대비 상대적 비율로 설정합니다.
            if (firstDistance > 0) {
                secondRunnerProgressBar.setProgress((int) (secondDistance / firstDistance * 100));
            }
        }

        // 3등 사용자 정보 업데이트
        if (userDistances.length >= 3) {
            MultiModeUser thirdUser = userDistances[2].getUser();
            bronzeNickNameTextView.setText(thirdUser.getNickName());
            bronzeLevelTextView.setText("Lv. " + thirdUser.getLevel());
            thirdDistance = userDistances[2].getDistance();
            bronzeDistanceTextView.setText(String.format("%.2fkm", thirdDistance));
            // 3등의 프로그레스 바는 1등 대비 상대적 비율로 설정합니다.
            if (firstDistance > 0) {
                thirdRunnerProgressBar.setProgress((int) (thirdDistance / firstDistance * 100));
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        //아래 코드에서 resume때 result fragment로 가는 이유?
        backPressedCallBack = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (SystemClock.elapsedRealtime() - backButtonLastClickTime < 1000) {
                    return;
                }
                backButtonLastClickTime = SystemClock.elapsedRealtime();
                showExitGameDialog();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallBack);
        socketListenerThread.addPlayFragment(this);
        socketListenerThread.resumeListening();

        transitionToResultFragment();

        Log.d("response", "start play screen");

        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
        }
        Intent intent = new Intent(getContext(), BackGroundLocationService.class);
        intent.setAction(START_LOCATION_SERVICE);
        getActivity().startForegroundService(intent);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationReceiver, new IntentFilter("location_update"));

        AccountAPIFactory accountFactory = AccountAPIFactory.getInstance();
        accountFactory.refreshToken(this.requireContext());
        //fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timeHandler.removeCallbacks(timeRunnable);
        sendDataHandler.removeCallbacks(sendDataRunnable);
        backPressedCallBack.remove();
        if (remainTimeHandler != null && remainTimeRunnable != null) {
            remainTimeHandler.removeCallbacks(remainTimeRunnable);
        }
        Intent intent = new Intent(getContext(), BackGroundLocationService.class);
        intent.setAction(STOP_LOCATION_SERVICE);
        getActivity().startForegroundService(intent);
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationReceiver);
    }

    void saveHistoryData(long groupHistoryId) throws JSONException {
        if ((int) minSpeed == 999) minSpeed = 0;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String startTimeString = selectedRoom.getStartTime().format(formatter);
        String finishTimeString = LocalDateTime.now().format(formatter);
        long durationInSeconds = selectedRoom.getDuration().getSeconds();
        int place = 0;
        if (userDistances[0].getUser().getId() == user.getId()) {
            place = 1;
        } else if (userDistances.length >= 2 && userDistances[1].getUser().getId() == user.getId()) {
            place = 2;
        } else if (userDistances.length >= 3 && userDistances[2].getUser().getId() == user.getId()) {
            place = 3;
        }
        int exp = ExpSystem.getExp("multi", distance, selectedRoom.getDuration(), place);
        Toast.makeText(getActivity(), "경험치 " + exp + "를 획득하셨습니다.", Toast.LENGTH_SHORT).show();
        HistoryData requestData = new HistoryData(user.getId(), distance, durationInSeconds,
                true, startTimeString, finishTimeString, calories, true, maxSpeed, minSpeed, calculateMedian(speedList), speedList, groupHistoryId, 0, exp);

        historyApi.postHistoryData(requestData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {

                    try {
                        Log.d("response", "Send History Success");
                        String responseBodyString = response.body().string();
                        Log.d("responseData", responseBodyString);
                        JSONObject jsonObject = null;
                        jsonObject = new JSONObject(responseBodyString);
                        // "exp" 키의 값을 가져오기
                        JSONObject expObject = jsonObject.getJSONObject("exp");
                        updatedExp = expObject.getInt("exp");
                        JSONObject badgeCollectionObject = jsonObject.getJSONObject("badge_collection");
                        updatedBadgeCollection = badgeCollectionObject.getInt("badge_collection");

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    transitionToResultFragment();
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
                    JSONObject responseBody = null;
                    String responseBodyString = null;
                    try {
                        responseBodyString = response.body().string();
                        responseBody = new JSONObject(responseBodyString);
                        Log.d("response", responseBodyString);
                        groupHistoryId = (int) responseBody.getLong("id");
                        Log.d("groupHistoryId", Integer.toString(groupHistoryId));
                        PacketBuilder packetBuilder = new PacketBuilder().protocol(Protocol.SAVE_GROUP_HISTORY).user(user).selectedRoom(selectedRoom).groupHistoryId(groupHistoryId);
                        Packet requestPacket = packetBuilder.getPacket();
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
        Collections.sort(numbers);

        int size = numbers.size();
        if (size == 0) return 0;

        if (size % 2 == 1) {
            return numbers.get(size / 2);
        } else {
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

    private void transitionToResultFragment() {
        if (userDistances != null) {
            if (speedList != null) {
                LocalDateTime currentTime = LocalDateTime.now();
                Duration iterationDuration = Duration.between(iterationStartTime, currentTime);
                long secondsDuration = iterationDuration.getSeconds();
                float newPace = (float) ((distance - (int) distance) / (secondsDuration / 3600.0));
                speedList.add(newPace);
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable("room", selectedRoom);
            bundle.putSerializable("top3UserDistance", userDistances);
            bundle.putSerializable("userDistance", distance);
            bundle.putSerializable("userSpeedList", (Serializable) speedList);
            bundle.putSerializable("updatedExp", updatedExp);
            bundle.putSerializable("updatedBadgeCollection", updatedBadgeCollection);
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_multi_room_result, bundle);
        }
    }

    public void startRemainTimer() {
        remainTimeRunnable = new Runnable() {
            @Override
            public void run() {
                if (secondsRemaining > 0) {
                    secondsRemaining--;

                    // 시간, 분, 초로 변환
                    long hours = secondsRemaining / 3600;
                    long minutes = (secondsRemaining % 3600) / 60;
                    long seconds = secondsRemaining % 60;

                    // 포맷에 맞게 문자열로 변환
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

                    // 텍스트 뷰 업데이트
                    timeGoalContentTextView.setText(formattedTime);

                    // 다음 초를 위해 다시 runnable을 post합니다.
                    remainTimeHandler.postDelayed(this, 1000);
                }
            }
        };

        // Runnable 시작
        remainTimeHandler.postDelayed(remainTimeRunnable, 1000);
    }

    public void stopRemainTimer() {
        // 타이머 정지
        remainTimeHandler.removeCallbacks(remainTimeRunnable);
    }

    public class SendDistanceTask extends AsyncTask<Packet, Void, Boolean> { // 서버에 업데이트할 거리 정보 전송
        @Override
        public Boolean doInBackground(Packet... packets) {
            boolean success = true;
            try {
                if (!userExit && packets.length > 0) {
                    // Get the first Packet to send from the parameters
                    Packet packetToSend = packets[0];
                    // Get the ObjectOutputStream from the socket manager
                    ObjectOutputStream oos = socketManager.getOOS();
                    // Write the Packet object to the output stream
                    oos.reset();
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

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
        }
    }

    //경기 시간이 종료되었을 경우 해당 유저의 레이스가 종료되었다는 패킷을 보냄
    public class SendFinishedTask extends AsyncTask<Packet, Void, Boolean> {
        Packet packet;

        @Override
        public Boolean doInBackground(Packet... packets) {
            boolean success = true;
            try {
                if (!userExit && packets.length > 0) {
                    Packet requestPacket = packets[0];
                    ObjectOutputStream oos = socketManager.getOOS();
                    if (!isCancelled() && oos != null) {
                        oos.reset();
                        oos.writeObject(requestPacket);
                        oos.flush();
                    } else {
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
                if (!userExit && packets.length > 0) {
                    Packet requestPacket = packets[0];
                    ObjectOutputStream oos = socketManager.getOOS();
                    oos.reset();
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
        private final Handler handler = new Handler();
        private Runnable runnable;

        @Override
        public Boolean doInBackground(Packet... packets) {
            boolean success = true;
            try {
                if (packets.length > 0) {
                    Packet requestPacket = packets[0];
                    ObjectOutputStream oos = socketManager.getOOS();
                    oos.reset();
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
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.navigation_multi_mode);
                Log.d("ExitSendPacket", "Packet sent successfully!");
            } else {
                Log.d("ExitSendPacket", "Failed to send packet!");
            }
        }
    }
}
