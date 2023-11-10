package com.example.runusandroid.ui.single_mode;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.runusandroid.HistoryApi;
import com.example.runusandroid.HistoryData;
import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.example.runusandroid.RetrofitClient;
import com.example.runusandroid.databinding.FragmentSingleModeBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import Logging.FileLogger;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SingleModeFragment extends Fragment {

    static final String START_LOCATION_SERVICE = "start";
    static final String STOP_LOCATION_SERVICE = "stop";
    private final float[][] modelInput = new float[5][3];
    private final float[][] modelOutput = new float[1][2];
    private final String[] background_location_permission = {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };
    private final String[] foreground_location_permission = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private final float[][] standard = {{2.41f, 2.38f, 2.32f, 2.21f}, {2.04f, 1.96f, 1.88f, 1.79f}};
    Chronometer currentTimeText;
    TextView currentDistanceText;
    TextView currentPaceText;
    SimpleDateFormat dateFormat;
    MainActivity2 mainActivity;
    LocalDateTime gameStartTime;
    LocalDateTime iterationStartTime;
    float calories = 0; // TODO: 칼로리 계산
    double distance = 0;
    OnBackPressedCallback backPressedCallBack;
    TextView goalDistanceStaticText;
    TextView goalDistanceText;
    TextView goalTimeStaticText;
    TextView goalTimeText;
    Button quitButton;
    Button startButton;
    private List<LatLng> pathPoints = new ArrayList<>();
    private List<Float> speedList = new ArrayList<>(); // 매 km 마다 속력 (km/h)
    private HistoryApi historyApi;
    private FragmentSingleModeBinding binding;
    private GoogleMap mMap;
    private Location lastLocation = null;
    private float minSpeed;
    private float maxSpeed;
    private float goalDistance;
    private float goalTime;
    private boolean runningNow;
    private final BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("location_update")) {
                double latitude = intent.getDoubleExtra("latitude", 0.0);
                double longitude = intent.getDoubleExtra("longitude", 0.0);
                LatLng newPoint = new LatLng(latitude, longitude);

                if (runningNow) {

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
                    if (newPoint != null && mainActivity.activityReceiver.getIsRunning()) {
                        // first few points might be noisy
                        if (pathPoints.size() > 5) {
                            Location lastLocation = new Location("");
                            lastLocation.setLatitude(pathPoints.get(pathPoints.size() - 2).latitude);
                            lastLocation.setLongitude(pathPoints.get(pathPoints.size() - 2).longitude);
                            // unit : meter -> kilometer
                            double last_distance_5s_kilometer = location.distanceTo(lastLocation) / (double) 1000;
                            distance += last_distance_5s_kilometer;
                            Log.d("test:distance:5sec", "Last 5 second Distance :" + location.distanceTo(lastLocation) / (double) 1000);
                            if (last_distance_5s_kilometer != 0) {
                                int paceMinute = (int) (1 / (last_distance_5s_kilometer / 5)) / 60;
                                int paceSecond = (int) (1 / (last_distance_5s_kilometer / 5)) % 60;
                                if (paceMinute <= 30) {
                                    String paceString = String.format("%02d'%02d\"", paceMinute, paceSecond);
                                    currentPaceText.setText(paceString);
                                } else {
                                    String paceString = "--'--\"";
                                    currentPaceText.setText(paceString);
                                }
                            } else {
                                String paceString = "--'--\"";
                                currentPaceText.setText(paceString);
                            }

                            // log distance into file
                            FileLogger.logToFileAndLogcat(mainActivity, "test:distance:5sec", "" + location.distanceTo(lastLocation) / (double) 1000);
                            // Below code seems to cause NullPointerException after 10 minutes or so (on Duration.between)
//                            if ((int) distance != lastDistanceInt) {
//                                LocalDateTime currentTime = LocalDateTime.now();
//                                Duration iterationDuration = Duration.between(iterationStartTime, currentTime);
//                                long secondsDuration = iterationDuration.getSeconds();
//                                float newPace = (float) (1.0 / (secondsDuration / 3600.0));
//                                if (newPace > maxSpeed)
//                                    maxSpeed = newPace;
//                                if (newPace < minSpeed)
//                                    minSpeed = newPace;
//                                speedList.add(newPace);
//                                iterationStartTime = currentTime;
//
//                            }
                            Log.d("test:distance:total", "Distance:" + distance);
                        }
                    }
                    currentDistanceText.setText(String.format(Locale.getDefault(), "%.2f " + "km", distance));

                    lastLocation = location;

                } else {
                    // Update UI (draw line, zoom in)
                    if (mMap != null) {
                        mMap.clear(); // Remove previous polylines
                        if (newPoint != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPoint, 16));
                        }
                    }
                }
            }
        }
    };
    private Interpreter tflite;
    private MappedByteBuffer tfliteModel;
    private boolean startlocation = false;
    private boolean timeLimitLess = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SingleModeViewModel singleModeViewModel = new ViewModelProvider(this).get(SingleModeViewModel.class);

        binding = FragmentSingleModeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainActivity = (MainActivity2) getActivity();
        historyApi = RetrofitClient.getClient().create(HistoryApi.class);

        quitButton = binding.quitButton;
        startButton = binding.startButton;
        currentDistanceText = binding.currentDistanceText;
        currentTimeText = binding.currentTimeText;
        currentPaceText = binding.currentPaceText;
        currentTimeText.setBase(SystemClock.elapsedRealtime());
        goalDistanceStaticText = binding.goalDistanceStaticText;
        goalDistanceText = binding.goalDistanceText;
        goalTimeStaticText = binding.goalTimeStaticText;
        goalTimeText = binding.goalTimeText;
        maxSpeed = 0;
        minSpeed = 999;
        runningNow = false;

        try {
            tfliteModel = FileUtil.loadMappedFile(mainActivity, "231103_model.tflite");
            tflite = new Interpreter(tfliteModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Arrays.stream(modelInput).forEach(row -> Arrays.fill(row, 0.0f));
        getMission();

        dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // for debugging purpose, hidden button on right bottom corner shows toast about lastly detected activity transition and isRunning value
        Button hiddenButton = binding.hiddenButton;
        hiddenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isRunning = mainActivity.activityReceiver.getIsRunning();
                String lastActivityType = mainActivity.activityReceiver.getLastActivityType();
                String lastTransitionType = mainActivity.activityReceiver.getLastTransitionType();

                Toast.makeText(mainActivity, "last detected : " + lastTransitionType + " " + lastActivityType +
                        " . isRunning " + isRunning, Toast.LENGTH_LONG).show();
            }
        });


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startButton.setVisibility(View.GONE);

                View dialogView = getLayoutInflater().inflate(R.layout.dialog_mission_start, null);
                AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();

                dialog.setView(dialogView);

                TextView missionInfo = dialogView.findViewById(R.id.textViewMissionInfo);

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
                String nickname = sharedPreferences.getString("nickname", "");

                boolean enoughHistory = modelInput[4][2] != 0;
                if (!enoughHistory) {
                    setStandard();


                    missionInfo.setText("5회 러닝 전에는 " + nickname + "님과 비슷한 그룹의 평균 러닝이 추천돼요!");
                }


                dialog.show();


                float lastDistance = modelInput[0][1] * 7.019781e+00f + 1.207809e+01f;
                float lastTime = modelInput[0][2] * 6.457635e-01f + 1.156572e+00f;

                if (goalDistance / goalTime > lastDistance * 1.1 / lastTime) {
                    missionInfo.setText(nickname + "님, 오늘은 더 바람을 느끼며 달려 보세요! \n 지난 기록보다 높은 목표를 추천해 드렸어요.");
                } else if (goalDistance / goalTime < lastDistance * 0.9 / lastTime) {
                    missionInfo.setText(nickname + "님, 오늘은 쉬어가는 러닝을 가져 보세요! \n 지난 기록보다 편한 목표를 추천해 드렸어요.");
                } else {
                    missionInfo.setText("러닝은 꾸준함이 생명! \n 지난 러닝의 감각을 계속해서 익혀 보세요.");
                }


                Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
                Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

                SeekBar distanceSeekBar = dialogView.findViewById(R.id.distanceSeekBar);
                TextView distanceTextView = dialogView.findViewById(R.id.textViewGoalDistance);

                distanceSeekBar.setProgress((int) (goalDistance * 10));

                String formattedDistance = String.format("%.1f", goalDistance);
                distanceTextView.setText(formattedDistance + " km");

                SeekBar timeSeekBar = dialogView.findViewById(R.id.timeSeekBar);
                TextView timeTextView = dialogView.findViewById(R.id.textViewGoalTime);

                timeSeekBar.setProgress((int) goalTime);
                timeTextView.setText((int) goalTime + " 분");

                Button buttonLess = dialogView.findViewById(R.id.buttonLess);

                Button buttonGreater = dialogView.findViewById(R.id.buttonGreater);

                buttonLess.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ColorStateList myColorStateList = ColorStateList.valueOf(Color.parseColor("#4AA570"));

                        ColorStateList otherColorStateList = ColorStateList.valueOf(Color.parseColor("#A3A3A3"));

                        // 뷰의 backgroundTint 변경
                        buttonLess.setBackgroundTintList(myColorStateList);
                        buttonGreater.setBackgroundTintList(otherColorStateList);
                        timeLimitLess = true;
                    }
                });

                buttonGreater.setOnClickListener(new Button.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ColorStateList myColorStateList = ColorStateList.valueOf(Color.parseColor("#4AA570"));

                        ColorStateList otherColorStateList = ColorStateList.valueOf(Color.parseColor("#A3A3A3"));

                        // 뷰의 backgroundTint 변경
                        buttonGreater.setBackgroundTintList(myColorStateList);
                        buttonLess.setBackgroundTintList(otherColorStateList);
                        timeLimitLess = false;
                    }
                });


                distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        distanceTextView.setText(progress / 10.0f + "km");

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        timeTextView.setText(progress + "분");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                buttonConfirm.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {

                        pathPoints = new ArrayList<>();
                        speedList = new ArrayList<>();
                        goalDistanceStaticText.setText("목표 거리");
                        goalDistance = distanceSeekBar.getProgress() / 10.0f;
                        goalTime = timeSeekBar.getProgress();
                        String formattedDistance = String.format("%.1f", goalDistance);
                        goalDistanceText.setText(formattedDistance + " km");
                        goalTimeStaticText.setText("목표 시간");
                        int roundedGoalTime = Math.round(goalTime);
                        goalTimeText.setText(roundedGoalTime + " 분");

                        if (ActivityCompat.checkSelfPermission(mainActivity,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(mainActivity, background_location_permission,
                                    200);
                        }

                        gameStartTime = LocalDateTime.now();
                        lastLocation = null;
                        distance = 0;
                        currentDistanceText.setText("0.00 km");
                        runningNow = true;
                        currentTimeText.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                            public void onChronometerTick(Chronometer chronometer) {
                                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                                chronometer.setText(dateFormat.format(time));
                            }
                        });
                        currentTimeText.setBase(SystemClock.elapsedRealtime());
                        currentTimeText.start();
                        startButton.setVisibility(View.GONE);
                        quitButton.setVisibility(View.VISIBLE);
                        hideBottomNavigation(true);

                        dialog.dismiss();
                    }
                });

                buttonCancel.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {

                        startButton.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });


            }
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View finishDialog = getLayoutInflater().inflate(R.layout.dialog_single_play_finish, null);
                AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();

                dialog.setView(finishDialog);

                Button buttonConfirmClose = finishDialog.findViewById(R.id.buttonConfirmClose);
                buttonConfirmClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishPlaySingleMode();
                        dialog.dismiss(); // 다이얼로그를 닫는 예제 동작
                    }
                });
                dialog.show();
            }
        });

        // Finding the visual component displaying the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        // Initialize the map
        mapFragment.getMapAsync(googleMap -> {
            mMap = googleMap;
            // Check the permission and enable the location marker
            if (ActivityCompat.checkSelfPermission(mainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        });

        // Viewmodel contains status, and when status changes (observe), the text will
        // change
        final TextView textView = binding.textSingleMode;
        singleModeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    private void setStandard() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        int gender = sharedPreferences.getInt("gender", 0) - 1;

        if (gender < 0) gender = 0;
        int age = sharedPreferences.getInt("age", 0) / 10;

        float height = sharedPreferences.getFloat("height", 0.0f);
        float weight = sharedPreferences.getFloat("weight", 0.0f);
        float bmi = height / (weight * weight);
        if (bmi >= 25) {
            age += 1;
        }
        if (age == 0) {
            age += 2;
        } else if (age == 1) {
            age += 1;
        }
        age = age > 5 ? 3 : age - 2;
        goalDistance = standard[gender][age];
        goalTime = 12.0f;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        currentTimeText.stop();
        Intent intent = new Intent(getContext(), BackGroundLocationService.class);
        intent.setAction(STOP_LOCATION_SERVICE);
        getActivity().startForegroundService(intent);
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationReceiver);

    }

    @Override
    public void onResume() {
        super.onResume();

        backPressedCallBack = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (runningNow) {


                    View finishDialog = getLayoutInflater().inflate(R.layout.dialog_single_play_finish, null);
                    AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();

                    dialog.setView(finishDialog);

                    Button buttonConfirmClose = finishDialog.findViewById(R.id.buttonConfirmClose);
                    buttonConfirmClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finishPlaySingleMode();
                            dialog.dismiss(); // 다이얼로그를 닫는 예제 동작
                        }
                    });

                    dialog.show();
                } else {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.navigation_home);
                }


            }
        };

        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallBack);
        if (!startlocation) {
            if (ActivityCompat.checkSelfPermission(mainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        1000);
            }
            if (ActivityCompat.checkSelfPermission(mainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1000);
            }
            Intent intent = new Intent(getContext(), BackGroundLocationService.class);
            intent.setAction(START_LOCATION_SERVICE);
            getActivity().startForegroundService(intent);
            startlocation = true;

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationReceiver, new IntentFilter("location_update"));

        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    void saveHistoryDataOnSingleMode() throws JSONException {
        if ((int) minSpeed == 999)
            minSpeed = 0;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        Long userId = sharedPreferences.getLong("userid", -1); // TODO: -1이면 안되긴하는데, catch해야 함.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String startTimeString = gameStartTime.format(formatter);
        String finishTimeString = LocalDateTime.now().format(formatter);
        long durationInSeconds = Duration.between(gameStartTime, LocalDateTime.now()).getSeconds();

        //NOTE: group_history_id에 null을 넣을 수 없어 싱글모드인 경우 -1로 관리

        HistoryData requestData = new HistoryData(userId, (float) distance, durationInSeconds,
                true, startTimeString, finishTimeString, calories, false, maxSpeed, minSpeed,
                calculateMedian(speedList), speedList, -1);

        historyApi.postHistoryData(requestData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("response", "Send History Success");
                } else {
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
        if (size == 0)
            return 0;

        if (size % 2 == 1) {
            return numbers.get(size / 2);
        } else {
            double middle1 = numbers.get((size - 1) / 2);
            double middle2 = numbers.get(size / 2);
            return (float) ((float) (middle1 + middle2) / 2.0);
        }
    }

    private String convertArrayToString(float[][] array) {
        StringBuilder result = new StringBuilder();

        for (float[] row : array) {
            for (float value : row) {
                result.append(value).append(" ");
            }
            result.append("\n");
        }

        return result.toString();
    }

    private void getMission() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);

        int gender = sharedPreferences.getInt("gender", 0) - 1;
        Call<ResponseBody> call = historyApi.getRecentHistoryData(sharedPreferences.getLong("userid", 0L));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();

                        JSONArray jsonArray = new JSONArray(responseData);
                        float wholeDistance = 0f;
                        float wholeTime = 0f;

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject historyObject = jsonArray.getJSONObject(i);

                            float recentDistance = (float) historyObject.getDouble("distance");
                            float recentDuration = convertTimetoHour(historyObject.getString("duration"));

                            modelInput[i][0] = (gender - 0.9111115f) / 0.3117750f;
                            modelInput[i][1] = (recentDistance - 1.207809e+01f) / 7.019781e+00f;
                            modelInput[i][2] = (recentDuration - 1.156572e+00f) / 6.457635e-01f;
                            wholeDistance += recentDistance;
                            wholeTime += recentDuration;

                        }

                        Log.e("model input", convertArrayToString(modelInput));
                        wholeTime /= 5;
                        wholeDistance /= 5;

                        String inputString = convertArrayToString(modelInput);
                        tflite.run(modelInput, modelOutput);
                        goalDistance = modelOutput[0][0] * 7.019781e+00f + 1.207809e+01f;
                        goalTime = (modelOutput[0][1] * 6.457635e-01f + 1.156572e+00f);


                        if (goalDistance / goalTime >= 1.3 * wholeDistance / wholeTime) {
                            goalDistance = goalTime * 1.3f * wholeDistance / wholeTime;
                        }
                        if (goalDistance >= 1.3f * wholeDistance) {
                            goalDistance /= 1.3f;
                            goalTime /= 1.3f;
                        }
                        goalTime *= 60;
                    } catch (Exception e) {
                        Log.e("modeloutput error", e.toString());
                        e.printStackTrace();
                    }
                } else {
                    Log.e("MainActivity", "Error: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("MainActivity", "Error: " + t.getMessage());
            }
        });

    }

    public float convertTimetoHour(String timeString) {
        LocalTime localTime = LocalTime.parse(timeString);
        return localTime.getHour() + (float) localTime.getMinute() / 60;
    }

    public void hideBottomNavigation(Boolean hide) {
        BottomNavigationView nav_view = getActivity().findViewById(R.id.nav_view);
        if (hide)
            nav_view.setVisibility(View.GONE);
        else
            nav_view.setVisibility(View.VISIBLE);
    }

    private void finishPlaySingleMode() {
        runningNow = false;
        quitButton.setVisibility(View.GONE);
        startButton.setVisibility(View.VISIBLE);
        mainActivity.getLastLocation();
        currentTimeText.stop();
        View dialogView;
        Button confirmButton;
        boolean missionCompleted = false;
        float wholeDistance = Float.valueOf((String) currentDistanceText.getText().subSequence(0, currentDistanceText.getText().length() - 2));
        float wholeTime = (float) Duration.between(gameStartTime, LocalDateTime.now()).getSeconds();
        if (timeLimitLess) {
            if (wholeDistance >= goalDistance && wholeTime < goalTime * 60) {
                missionCompleted = true;
            }
        } else {
            if (wholeDistance >= goalDistance && wholeTime >= goalTime * 60) {
                missionCompleted = true;
            }
        }

        goalDistanceStaticText.setText("");
        goalDistanceText.setText("");
        goalTimeStaticText.setText("");
        goalTimeText.setText("");

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        if (missionCompleted) {
            dialogView = inflater.inflate(R.layout.dialog_mission_success, null);
            confirmButton = dialogView.findViewById(R.id.buttonConfirm);
            TextView elapsedTimeTextView = dialogView.findViewById(R.id.textViewElapsedTimeonSuccess);
            TextView distanceTextView = dialogView.findViewById(R.id.textViewDistanceonSuccess);
            elapsedTimeTextView.setText("달린 시간: " + currentTimeText.getText());
            distanceTextView.setText("달린 거리: " + currentDistanceText.getText());
        } else {
            dialogView = inflater.inflate(R.layout.dialog_mission_failure, null);
            confirmButton = dialogView.findViewById(R.id.buttonConfirmFailure);
            TextView elapsedTimeTextView = dialogView.findViewById(R.id.textViewElapsedTimeonFailure);
            TextView distanceTextView = dialogView.findViewById(R.id.textViewDistanceonFailure);
            elapsedTimeTextView.setText("달린 시간: " + currentTimeText.getText());
            distanceTextView.setText("달린 거리: " + currentDistanceText.getText());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                lastLocation = null;
                distance = 0;
                currentDistanceText.setText("0.00 km");
                currentTimeText.setBase(SystemClock.elapsedRealtime());
            }
        });
        dialog.show();
        try {
            saveHistoryDataOnSingleMode();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        getMission();
        hideBottomNavigation(false);
    }

}
