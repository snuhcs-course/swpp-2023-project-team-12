package com.example.runusandroid.ui.single_mode;

import static android.content.Context.MODE_PRIVATE;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import com.example.runusandroid.ActivityRecognition.RunningState;
import com.example.runusandroid.ExpSystem;
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
import java.io.Serializable;
import java.nio.MappedByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Queue;

import Logging.FileLogger;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SingleModeFragment extends Fragment {

    static final String START_LOCATION_SERVICE = "start";
    static final String STOP_LOCATION_SERVICE = "stop";
    private final String[] background_location_permission = {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };
    private final String[] foreground_location_permission = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private final float[][] standard = {{2.41f, 2.38f, 2.32f, 2.21f}, {2.04f, 1.96f, 1.88f, 1.79f}};
    private final boolean timeLimitLess = true;
    Chronometer currentTimeText;
    TextView currentDistanceText;
    TextView currentPaceText;
    SimpleDateFormat dateFormat;
    MainActivity2 mainActivity;
    LocalDateTime gameStartTime;
    LocalDateTime iterationStartTime;
    float calories = 0; // TODO: 칼로리 계산
    double distance = 0;
    long currentTime;
    OnBackPressedCallback backPressedCallBack;
    TextView goalDistanceStaticText;
    TextView goalDistanceText;
    TextView goalTimeStaticText;
    TextView goalTimeText;
    Button quitButton;
    LinearLayout currentPace;
    Button startButton;
    double distance_for_pace;
    private float[][] originalData;
    private float[][][] modelInput;
    private int historyNum;
    private float[][][] modelOutput;
    private int updatedExp;
    private List<LatLng> pathPoints = new ArrayList<>();
    private List<Float> speedList = new ArrayList<>(); // 매 km 마다 속력 (km/h)
    private HistoryApi historyApi;
    private FragmentSingleModeBinding binding;
    private GoogleMap mMap;
    private Location lastLocation = null;
    private float minSpeed;
    private float maxSpeed;
    private float goalDistance;
    private float nowGoalDistance;
    private float goalTime;
    private float nowGoalTime;
    private boolean runningNow;
    private Queue<Double> pace_distance_queue;
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
                        mMap.addPolyline(
                                new PolylineOptions().addAll(pathPoints).color(Color.parseColor("#4AA570")).width(10));
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
                            Log.d("test:distance:5sec",
                                    "Last 5 second Distance :" + location.distanceTo(lastLocation) / (double) 1000);
                            if (last_distance_5s_kilometer > 0) {
                                int paceMinute = (int) (1 / (distance_for_pace / (5 * pace_distance_queue.size())))
                                        / 60;
                                int paceSecond = (int) (1 / (distance_for_pace / (5 * pace_distance_queue.size())))
                                        % 60;
                                Log.d("test:distance:5sec",
                                        "distance : " + distance_for_pace + " and queue size is "
                                                + pace_distance_queue.size() + " pace :" + paceMinute + "' "
                                                + paceSecond + "''");
                                if (paceMinute < 60) {
                                    String paceString = String.format("%02d'%02d\"", paceMinute, paceSecond);
                                    currentPaceText.setText(paceString);
                                } else {
                                    String paceString = "59'59\"";
                                    currentPaceText.setText(paceString);
                                }
                            } else {
                                String paceString = "--'--\"";
                                currentPaceText.setText(paceString);
                            }

                            // log distance into file
                            FileLogger.logToFileAndLogcat(mainActivity, "test:distance:5sec",
                                    "" + location.distanceTo(lastLocation) / (double) 1000);
                            Log.d("test:distance:total", "Distance:" + distance);
                        }
                    } else {
                        String paceString = "--'--\"";
                        currentPaceText.setText(paceString);
                    }
                    currentDistanceText.setText(
                            String.format(Locale.getDefault(), "%.1f " + "km", Math.floor(distance * 10) / 10));

                    lastLocation = location;
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
    private int updatedBadgeCollection;
    private int isMissionSucceeded = 0;
    private int mode = 0;
    private Interpreter tflite;
    private MappedByteBuffer tfliteModel;
    private boolean startlocation = false;

    public SingleModeFragment() {
    }

    private double sumQueue() {
        double sum = 0;
        Iterator<Double> iterator = pace_distance_queue.iterator();
        while (iterator.hasNext()) {
            sum += iterator.next();
        }
        return sum;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SingleModeViewModel singleModeViewModel = new ViewModelProvider(this).get(SingleModeViewModel.class);

        binding = FragmentSingleModeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        distance_for_pace = 0;
        pace_distance_queue = new LinkedList<>();
        mainActivity = (MainActivity2) getActivity();
        historyApi = RetrofitClient.getClient().create(HistoryApi.class);
        currentPace = binding.currentPace;
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
        startButton.setVisibility(View.VISIBLE);
        quitButton.setVisibility(View.GONE);

        hideBottomNavigation(false);

        try {
            tfliteModel = FileUtil.loadMappedFile(mainActivity, "model_231206.tflite");
            tflite = new Interpreter(tfliteModel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        getMission();

        dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // for debugging purpose, hidden button on right bottom corner shows toast about
        // lastly detected activity transition and isRunning value
        Button hiddenButton = binding.hiddenButton;
        hiddenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean isRunning = RunningState.getIsRunning();
                String lastActivityType = RunningState.getLastActivityType();
                String lastTransitionType = RunningState.getLastTransitionType();
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showModeChoice();

            }
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View finishDialog = getLayoutInflater().inflate(R.layout.dialog_single_play_finish, null);
                Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                dialog.setContentView(finishDialog);

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
            // set initial point to on saved already in mainActivity, if null, set to
            // default location (남산타워)
            LatLng initialPoint;
            if (mainActivity.initialLocation != null) {
                initialPoint = new LatLng(mainActivity.initialLocation.getLatitude(),
                        mainActivity.initialLocation.getLongitude());
            } else {
                initialPoint = new LatLng(37.55225, 126.9873);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPoint, 16));
        });
        // Location initialLocation =
        // mainActivity.getFusedLocationClient().getLastLocation().getResult();
        // LatLng initialPoint = new LatLng(initialLocation.getLatitude(),
        // initialLocation.getLongitude());

        // Viewmodel contains status, and when status changes (observe), the text will
        // change
        final TextView textView = binding.textSingleMode;
        singleModeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    private int showModeChoice() {
        View dialogView = getLayoutInflater().inflate(R.layout.diaglog_mode_choice, null);
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);

        TextView modeTitle = dialogView.findViewById(R.id.textViewTitle);
        setTextBold(modeTitle, new String[]{"AI",});

        TextView textTimeAttack = dialogView.findViewById(R.id.textViewTimeAttack);
        setTextBold(textTimeAttack, new String[]{"주어진", "AI가 추천한 거리"});

        TextView textMarathon = dialogView.findViewById(R.id.textViewMarathon);
        setTextBold(textMarathon, new String[]{"긴 거리", "한계"});

        TextView textCustom = dialogView.findViewById(R.id.textViewCustom);
        setTextBold(textCustom, new String[]{"직접"});

        dialog.show();

        ImageButton buttonClose = dialogView.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = -1;
                dialog.dismiss();
            }
        });

        Button buttonTimeAttack = dialogView.findViewById(R.id.buttonTimeAttack);
        buttonTimeAttack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = 1;
                dialog.dismiss();
                showTimeAttackDialog();
            }
        });

        Button buttonMarathon = dialogView.findViewById(R.id.buttonMarathon);
        buttonMarathon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = 2;
                dialog.dismiss();
                showMarathonDialog();
            }
        });

        Button buttonCustom = dialogView.findViewById(R.id.buttonCustom);
        buttonCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mode = 0;
                dialog.dismiss();
                showCustomDialog();
            }
        });

        return mode;

    }

    private void showCustomDialog() {
        final boolean[] validationDistance = {true};
        final boolean[] validationMinute = {true};
        final boolean[] validationHour = {true};

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_mission_custom, null);
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT; // 원하는 너비로 조절
        dialog.getWindow().setAttributes(params);

        boolean enoughHistory = historyNum >= 5;
        if (!enoughHistory || goalDistance < 0.01) {
            setStandard();
        }

        int goalHour = (int) goalTime / 60;
        int goalMinute = (int) goalTime % 60;
        nowGoalDistance = goalDistance;
        nowGoalTime = goalTime;

        EditText textDistance = dialogView.findViewById(R.id.editTextGoalDistance);
        EditText textHour = dialogView.findViewById(R.id.editTextGoalHour);
        EditText textMinute = dialogView.findViewById(R.id.editTextGoalMinute);
        TextView MissionDistanceValidationText = dialog.findViewById(R.id.MissionDistanceValidationText);
        TextView MissionTimeValidationText = dialog.findViewById(R.id.MissionTimeValidationText);

        textDistance.setText(floatTo1stDecimal(nowGoalDistance));
        textHour.setText(floatTo1stDecimal(goalHour));
        textMinute.setText(floatTo1stDecimal(goalMinute));

        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
        textDistance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s != null && !s.toString().equals("")) {
                    try {
                        validationDistance[0] = true;
                        String distanceText = s.toString();
                        nowGoalDistance = (float) Double.parseDouble(distanceText);
                        if (((int) (nowGoalDistance * 10)) / 10f != nowGoalDistance) {
                            Log.e("TEXT", nowGoalDistance + " / " + Math.round(nowGoalDistance * 10) / 10f);
                            nowGoalDistance = ((int) (nowGoalDistance * 10)) / 10f;
                            textDistance.setText(String.valueOf(nowGoalDistance));
                        }
                        textDistance.setSelection(textDistance.getText().length());
                    } catch (NumberFormatException e) {
                        String distanceText = s.toString();
                        Log.e("editText check", distanceText);
                        e.printStackTrace();
                    }
                } else {
                    validationDistance[0] = false;
                    int new_distance = 0;
                    nowGoalDistance = new_distance;
                }

            }
        });

        textHour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s != null && !s.toString().equals("")) {
                    try {
                        validationHour[0] = true;
                        String hourText = s.toString();
                        int newHour = Integer.parseInt(hourText);
                        nowGoalTime = newHour * 60 + Integer.parseInt(textMinute.getText().toString());
                    } catch (NumberFormatException e) {
                        String distanceText = s.toString();
                        Log.e("editText check", distanceText);
                        e.printStackTrace();
                    }
                } else {
                    validationHour[0] = false;
                    int new_time = 0;
                    nowGoalTime = new_time;
                }

            }
        });

        textMinute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s != null && !s.toString().equals("")) {
                    try {
                        validationMinute[0] = true;
                        String minuteText = s.toString();
                        int newMinute = Integer.parseInt(minuteText);
                        if (newMinute > 59) {
                            newMinute = 59;
                            textMinute.setText("59");
                            textMinute.setSelection(2);
                        }
                        nowGoalTime = Integer.parseInt(textHour.getText().toString()) * 60 + newMinute;
                    } catch (NumberFormatException e) {
                        String distanceText = s.toString();
                        Log.e("editText check", distanceText);
                        e.printStackTrace();
                    }
                } else {
                    validationMinute[0] = false;
                    int new_time = 0;
                    nowGoalTime = new_time;
                }

            }
        });

        ImageButton buttonClose = dialogView.findViewById(R.id.buttonClose);

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showModeChoice();
            }
        });

        buttonConfirm.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (validationMinute[0] && validationHour[0]) {
                    MissionTimeValidationText.setVisibility(View.GONE);
                }
                if (validationDistance[0]) {
                    MissionDistanceValidationText.setVisibility(View.GONE);
                }
                if (!(validationMinute[0] && validationHour[0] && validationDistance[0])) {
                    if (!validationMinute[0] || !validationHour[0]) {
                        MissionTimeValidationText.setVisibility(View.VISIBLE);
                    }
                    if (!validationDistance[0]) {
                        MissionDistanceValidationText.setVisibility(View.VISIBLE);
                    }
                } else {
                    goalDistance = nowGoalDistance;
                    Log.d("goalDistance", goalDistance + "");
                    goalTime = nowGoalTime;
                    dialog.dismiss();
                    setRunningStart();
                }

            }
        });
        dialogView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Hide keyboard when tapped outside EditText
                hideKeyboard(dialogView);
                return false;
            }
        });
        dialog.show();
    }

    private void showMarathonDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_mission_marathon, null);
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT; // 원하는 너비로 조절
        dialog.getWindow().setAttributes(params);

        boolean enoughHistory = historyNum >= 5;
        if (!enoughHistory || goalDistance < 0.01) {
            setStandard();
            nowGoalDistance = goalDistance * 1.3f;
        } else {
            float max_distance = 0;
            for (int i = 0; i < modelInput.length; i++) {
                float tmp_distance = originalData[i][0];
                if (tmp_distance > max_distance) {
                    max_distance = tmp_distance;
                }
            }
            nowGoalDistance = goalDistance * 1.3f;
            if (nowGoalDistance > 2 * max_distance) {
                nowGoalDistance = goalDistance * 0.6f + max_distance * 0.4f;
            } else if (nowGoalDistance < max_distance) {
                nowGoalDistance = max_distance * 1.2f;
            }
        }

        SeekBar distanceSeekBar = dialogView.findViewById(R.id.distanceSeekBar);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
        ImageButton buttonClose = dialogView.findViewById(R.id.buttonClose);
        TextView distanceTextView = dialogView.findViewById(R.id.textViewGoalDistance);
        distanceTextView.setText("목표 거리   " + floatTo1stDecimal(nowGoalDistance) + "km");

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showModeChoice();
            }
        });

        buttonConfirm.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                goalDistance = nowGoalDistance;
                goalTime = -1;
                Log.d("goalDistance", goalDistance + "");
                isMissionSucceeded = 2;
                setRunningStart();
            }
        });

        final float revise_distance = Math.round(nowGoalDistance / 2f) / 10f;
        float fixed_distance = nowGoalDistance;

        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                nowGoalDistance = fixed_distance + (progress - 4) * revise_distance;
                distanceTextView.setText("목표 거리   " + String.format(floatTo1stDecimal(nowGoalDistance)) + "km");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        dialog.show();

    }

    private void showTimeAttackDialog() {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_mission_timeattack, null);
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT; // 원하는 너비로 조절
        dialog.getWindow().setAttributes(params);
        TextView missionInfo = dialogView.findViewById(R.id.textViewMissionInfo);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        String nickname = sharedPreferences.getString("nickname", "");

        Log.e("goalDistance&time Timeattack", "goalDistance&time Timeattack : " + goalDistance + " " + goalTime);

        boolean enoughHistory = historyNum >= 5;
        if (!enoughHistory || goalDistance < 0.01) {
            setStandard();
            nowGoalDistance = goalDistance;
            missionInfo.setText("5회 러닝 전이나 기존 기록이 지나치게 짧을 때는 \n" + nickname + "님과 비슷한 그룹의 평균을 추천해요!");
        } else {
            nowGoalDistance = goalDistance;
            float lastDistance = originalData[historyNum - 1][0];
            float lastTime = originalData[historyNum - 1][1];

            if (nowGoalDistance / (goalTime / 60) > lastDistance * 1.1 / lastTime) {
                Log.e("difficult running", nowGoalDistance + " " + goalTime + " " + lastDistance + " " + lastTime);
                missionInfo.setText(nickname + "님, 오늘은 더 바람을 느끼며 달려 보세요! \n 지난 기록보다 높은 목표를 추천해 드렸어요.");
            } else if (nowGoalDistance / (goalTime / 60) < lastDistance * 0.8 / lastTime) {
                Log.e("easy running", nowGoalDistance + " " + goalTime + " " + lastDistance + " " + lastTime);
                missionInfo.setText(nickname + "님, 오늘은 쉬어가는 러닝을 가져 보세요! \n 지난 기록보다 편한 목표를 추천해 드렸어요.");
            } else {
                missionInfo.setText("러닝은 꾸준함이 생명! \n 지난 러닝의 감각을 계속해서 익혀 보세요.");
            }
        }
        nowGoalTime = goalTime;

        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);

        SeekBar distanceSeekBar = dialogView.findViewById(R.id.distanceSeekBar);
        TextView distanceTextView = dialogView.findViewById(R.id.textViewGoalDistance);

        distanceTextView.setText("목표 거리   " + floatTo1stDecimal(nowGoalDistance) + "km");

        SeekBar timeSeekBar = dialogView.findViewById(R.id.timeSeekBar);
        TextView timeTextView = dialogView.findViewById(R.id.textViewGoalTime);

        timeTextView.setText("목표 시간   " + (int) nowGoalTime + "분");

        ImageButton buttonClose = dialogView.findViewById(R.id.buttonClose);

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showModeChoice();
            }
        });

        final float revise_distance = Math.round(nowGoalDistance / 2f) / 10f;
        final float fixed_distance = nowGoalDistance;
        final float revise_time = (int) (Math.round(nowGoalTime) / 20.0f)
                + ((int) (Math.round(nowGoalTime) / 20.0f) == 0 ? 1 : 0);

        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                nowGoalDistance = fixed_distance + (progress - 4) * revise_distance;
                Log.e("Revise", "original distance : " + fixed_distance + ", revise : " + revise_distance);
                distanceTextView.setText("목표 거리   " + String.format(floatTo1stDecimal(nowGoalDistance)) + "km");

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
                nowGoalTime = goalTime + (progress - 4) * revise_time;
                timeTextView.setText("목표 시간   " + (int) (nowGoalTime) + "분");
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
                goalDistance = nowGoalDistance;
                Log.d("goalDistance", goalDistance + "");
                goalTime = nowGoalTime;
                isMissionSucceeded = 1;
                setRunningStart();
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void setRunningStart() {

        iterationStartTime = LocalDateTime.now();
        pathPoints = new ArrayList<>();
        speedList = new ArrayList<>();
        goalDistance = Math.round(goalDistance * 1000) / 1000f;
        Log.d("goalDistance", "setRunningStart" + " " + goalDistance);
        goalDistanceStaticText.setText("목표 거리");
        goalTimeStaticText.setText("목표 시간");
        if (mode == 2) {
            goalTimeText.setText("--");
        } else {
            goalTimeText.setText(goalTime + " 분");
        }
        goalDistanceText.setText(floatTo1stDecimal(goalDistance) + " km");
        if (ActivityCompat.checkSelfPermission(mainActivity,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // ActivityCompat.requestPermissions(mainActivity,
            // background_location_permission, 200);
            showBackgroundLocationPermissionDialog();
        }

        gameStartTime = LocalDateTime.now();
        lastLocation = null;
        distance = 0;
        currentPace.setVisibility(View.VISIBLE);
        currentDistanceText.setText("0.0 km");
        currentTimeText.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer chronometer) {
                currentTime = SystemClock.elapsedRealtime() - chronometer.getBase();
                chronometer.setText(dateFormat.format(currentTime));
            }
        });
        hideBottomNavigation(true);
        currentTimeText.setBase(SystemClock.elapsedRealtime());
        currentTimeText.start();
        startButton.setVisibility(View.GONE);
        quitButton.setVisibility(View.VISIBLE);
        runningNow = true;
    }

    private void showBackgroundLocationPermissionDialog() {
        @SuppressLint("InflateParams")
        View exitGameDialog = getLayoutInflater().inflate(R.layout.dialog_multimode_play_finish, null);
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(exitGameDialog);
        TextView textView = exitGameDialog.findViewById(R.id.textViewExitGame);
        textView.setText("원활한 앱 이용을 위해 위치 정보 접근 권한을 \n항상 허용으로 설정해주세요");
        Button buttonConfirmPlayExit = exitGameDialog.findViewById(R.id.buttonConfirmPlayExit);
        buttonConfirmPlayExit.setOnClickListener(v -> {
            dialog.dismiss();
            ActivityCompat.requestPermissions(mainActivity, background_location_permission, 200);
        });
        dialog.show();
    }

    private void setTextBold(TextView wantView, String[] words) {
        String originalString = wantView.getText().toString();
        SpannableStringBuilder spannableString = new SpannableStringBuilder(originalString);
        for (int i = 0; i < words.length; i++) {
            String wantWord = words[i];
            int start = originalString.indexOf(wantWord);
            int end = start + wantWord.length();
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        wantView.setText(spannableString);

    }

    private void setStandard() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        int gender = sharedPreferences.getInt("gender", 0) - 1;

        if (gender < 0)
            gender = 0;
        if (gender > 1)
            gender = 1;
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
        goalTime = 12;
    }

    @Override
    public void onDestroyView() {
        Log.d("test:singlemode:lifecycle", "onDestroyView");
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
                    Dialog dialog = new Dialog(getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    dialog.setContentView(finishDialog);

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
        Log.d("test:singlemode:lifecycle", "onResume, startlocation : " + startlocation);
        if (!startlocation) {
            // if (ActivityCompat.checkSelfPermission(mainActivity,
            // Manifest.permission.ACCESS_COARSE_LOCATION) !=
            // PackageManager.PERMISSION_GRANTED) {
            // ActivityCompat.requestPermissions(mainActivity, new
            // String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
            // 1001);
            // }
            // if (ActivityCompat.checkSelfPermission(mainActivity,
            // Manifest.permission.ACCESS_FINE_LOCATION) !=
            // PackageManager.PERMISSION_GRANTED) {
            // ActivityCompat.requestPermissions(mainActivity, new
            // String[]{Manifest.permission.ACCESS_FINE_LOCATION},
            // 1002);
            // }
            Log.d("test:singlemode:lifecycle", "initiate location service");
            Intent intent = new Intent(getContext(), BackGroundLocationService.class);
            intent.setAction(START_LOCATION_SERVICE);
            getActivity().startForegroundService(intent);
            startlocation = true;

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationReceiver,
                    new IntentFilter("location_update"));

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("test:singlemode:lifecycle", "onPause, startlocation : " + startlocation);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("test:singlemode:lifecycle", "onStop");
    }

    void saveHistoryDataOnSingleMode() throws JSONException {
        if ((int) minSpeed == 999)
            minSpeed = 0;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        Long userId = sharedPreferences.getLong("userid", -1); // TODO: -1이면 안되긴하는데, catch해야 함.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String startTimeString = gameStartTime.format(formatter);
        String finishTimeString = LocalDateTime.now().format(formatter);
        Duration duration = Duration.between(gameStartTime, LocalDateTime.now());
        long durationInSeconds = duration.getSeconds();
        // NOTE: group_history_id에 null을 넣을 수 없어 싱글모드인 경우 -1로 관리
        int exp = ExpSystem.getExp("single", distance, duration, isMissionSucceeded);
        Toast.makeText(getActivity(), "경험치 " + exp + "를 획득하셨습니다.", Toast.LENGTH_SHORT).show();
        Log.d("UAT:exp", "single " + distance + " " + duration + " " + isMissionSucceeded + " " + exp);
        HistoryData requestData = new HistoryData(userId, (float) distance, durationInSeconds,
                true, startTimeString, finishTimeString, calories, false, maxSpeed, minSpeed,
                calculateMedian(speedList), speedList, -1, isMissionSucceeded, exp);

        historyApi.postHistoryData(requestData).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("response", "Send History Success");
                    try {
                        String responseBodyString = response.body().string();
                        Log.d("responseData", responseBodyString);

                        JSONObject jsonObject = new JSONObject(responseBodyString);

                        // "exp" 키의 값을 가져오기
                        JSONObject expObject = jsonObject.getJSONObject("exp");
                        updatedExp = expObject.getInt("exp");
                        JSONObject updatedBadgeCollectionObject = jsonObject.getJSONObject("badge_collection");
                        updatedBadgeCollection = updatedBadgeCollectionObject.getInt("badge_collection");

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
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
            if (row[0] == 0) {
                break;
            }
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

                        historyNum = jsonArray.length();
                        if (historyNum == 0) {
                            setStandard();
                            return;
                        }

                        originalData = new float[777][2];
                        modelInput = new float[1][777][2];
                        modelOutput = new float[1][777][2];

                        int cleansedNum = 0;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject historyObject = jsonArray.getJSONObject(i);

                            float recentDistance = (float) historyObject.getDouble("distance");
                            float recentDuration = convertTimetoHour(historyObject.getString("duration"));

                            if (recentDistance>=0.01 && recentDuration>=0.008){
                                originalData[cleansedNum][0] = recentDistance;
                                originalData[cleansedNum][1] = recentDuration;
                                wholeDistance += recentDistance;
                                wholeTime += recentDuration;
                                cleansedNum += 1;

                            }
                        }
                        historyNum = cleansedNum;
                        float alpha = 2f / (1f + 20);

                        for (int i=0; i<cleansedNum; i++){
                            int idx = cleansedNum - i - 1;
                            float tmpDistance = (originalData[idx][0] - 0.0083549205f) / (83.8955084972f - 0.0083549205f);
                            float tmpTime = (originalData[idx][1] - 0.1391666667f) / (4.9963888889f - 0.1391666667f);
                            if (i>0){
                                modelInput[0][i][0] = modelInput[0][i - 1][0] * alpha + tmpDistance * (1 - alpha);
                                modelInput[0][i][1] = modelInput[0][i - 1][1] * alpha + tmpTime * (1 - alpha);
                            }
                            else{
                                modelInput[0][i][0] = tmpDistance;
                                modelInput[0][i][1] = tmpTime;
                            }
                        }
                        wholeTime /= historyNum;
                        wholeDistance /= historyNum;

                        tflite.run(modelInput, modelOutput);
                        goalDistance = modelOutput[0][historyNum - 1][0] * (83.8955084972f - 0.0083549205f)
                                + 0.0083549205f;
                        goalTime = (modelOutput[0][historyNum - 1][1] * (4.9963888889f - 0.1391666667f)
                                + 0.1391666667f);

                        // adjustment
                        goalDistance *= 1.02f;
                        if (goalDistance / goalTime >= 1.2 * wholeDistance / wholeTime) {
                            goalTime *= 1.2f;
                        }
                        if (goalDistance >= 1.2f * wholeDistance) {
                            goalDistance /= 1.2f;
                            goalTime /= 1.2f;
                        }
                        else if (goalDistance <= 0.8f * wholeDistance) {
                            goalDistance /= 0.8f;
                            goalTime /= 0.8f;
                        }
                        goalTime *= 60;
                        goalTime = (int) goalTime;
                        Log.d("goalDistance&time", "goalDistance&time : " + goalDistance + " " + goalTime);

                        if (goalTime < 2 || goalDistance <= 0.01) {
                            setStandard();
                        }
                    } catch (Exception e) {
                        Log.e("modeloutput error", e.toString());
                        setStandard();
                        e.printStackTrace();
                    }
                } else {
                    Log.e("MainActivity", "Error: " + response.message());
                    setStandard();
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
        return localTime.getHour() + (float) localTime.getMinute() / 60 + (float) localTime.getSecond() / 3600;
    }

    public void hideBottomNavigation(Boolean hide) {
        BottomNavigationView nav_view = getActivity().findViewById(R.id.nav_view);
        if (nav_view != null) {
            if (hide)
                nav_view.setVisibility(View.GONE);
            else
                nav_view.setVisibility(View.VISIBLE);
        }
    }

    private void finishPlaySingleMode() {
        runningNow = false;
        Log.d("goalDistance", "finishPlaySingleMode1 " + goalDistance);
        float finalGoalDistance = goalDistance;
        float finalGoalTime = goalTime;
        mainActivity.getLastLocation();
        currentTimeText.stop();
        View dialogView;
        Button confirmButton;
        boolean missionCompleted = false;
        Log.d("goalDistance", "finishPlaySingleMode2 " + goalDistance);

        float wholeDistance = Float.valueOf(
                (String) currentDistanceText.getText().subSequence(0, currentDistanceText.getText().length() - 2));
        float wholeTime = (float) Duration.between(gameStartTime, LocalDateTime.now()).getSeconds();

        if (mode == 2) {
            if (wholeDistance >= goalDistance) {
                missionCompleted = true;
            }
        } else {
            if (wholeDistance >= goalDistance && wholeTime <= goalTime * 60) {
                Log.e("Mission completed", wholeDistance + " " + goalDistance + " " + wholeTime + " " + goalTime * 60);
                missionCompleted = true;
            }
        }

        Log.d("goalDistance", "finishPlaySingleMode3 " + goalDistance);

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        if (missionCompleted) {
            dialogView = inflater.inflate(R.layout.dialog_mission_success, null);
            confirmButton = dialogView.findViewById(R.id.buttonConfirm);
            TextView elapsedTimeTextView = dialogView.findViewById(R.id.textViewElapsedTimeonSuccess);
            TextView distanceTextView = dialogView.findViewById(R.id.textViewDistanceonSuccess);
            elapsedTimeTextView.setText("달린 시간: " + currentTimeText.getText());
            distanceTextView.setText("달린 거리: " + currentDistanceText.getText());
        } else {
            isMissionSucceeded *= -1;
            dialogView = inflater.inflate(R.layout.dialog_mission_failure, null);
            confirmButton = dialogView.findViewById(R.id.buttonConfirmFailure);
            TextView elapsedTimeTextView = dialogView.findViewById(R.id.textViewElapsedTimeonFailure);
            TextView distanceTextView = dialogView.findViewById(R.id.textViewDistanceonFailure);
            elapsedTimeTextView.setText("달린 시간: " + currentTimeText.getText());
            distanceTextView.setText("달린 거리: " + currentDistanceText.getText());
        }
        Log.d("goalDistance", "finishPlaySingleMode4 " + goalDistance);

        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(dialogView);
        Log.d("goalDistance", "finishPlaySingleMode5 " + goalDistance);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (speedList != null) {
                    LocalDateTime currentTime = LocalDateTime.now();
                    Duration iterationDuration = Duration.between(iterationStartTime, currentTime);
                    long secondsDuration = iterationDuration.getSeconds();
                    float newPace = (float) ((distance - (int) distance) / (secondsDuration / 3600.0));
                    speedList.add(newPace);
                }
                Bundle bundle = new Bundle();
                Log.d("goalDistance", "finishPlaySingleMode6 " + finalGoalDistance);
                Log.e("time check", "currenttiime, wholeTime : " + currentTime + " " + wholeTime);

                bundle.putSerializable("updatedExp", updatedExp);
                Log.d("UAT:exp", "confirm onClick exp " + updatedExp);
                bundle.putSerializable("updatedBadgeCollection", updatedBadgeCollection);
                bundle.putSerializable("goalDistance", finalGoalDistance);
                bundle.putSerializable("goalTime", finalGoalTime);
                bundle.putSerializable("currentDistance", distance);
                bundle.putSerializable("currentTime", currentTime);
                bundle.putSerializable("calories", calories);
                bundle.putSerializable("userSpeedList", (Serializable) speedList);
                bundle.putSerializable("pathPointList", (Serializable) pathPoints);
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.navigation_single_mode_result, bundle);
                quitButton.setVisibility(View.GONE);
                currentPace.setVisibility(View.GONE);
                goalDistanceStaticText.setText("");
                goalDistanceText.setText("");
                goalTimeStaticText.setText("");
                goalTimeText.setText("");
                lastLocation = null;
                distance = 0;
                currentDistanceText.setText("0.0 km");
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

    }

    public float getCalories(float weight, float pace, float minute) {
        float METs = 0.1f;
        float speed = (1f / pace) * 1000; // (m/m)
        if (speed < 100) {
            METs = speed / 30f + 2.0f / 3.0f;
        } else if (speed < 107) {
            METs = (speed - 72f) / 7f;
        } else if (speed < 134) {
            METs = (speed - 62f) / 9f;
        } else {
            METs = (2 * speed - 52f) / 27;
        }
        if (METs < 1) {
            METs = 1f;
        }
        float calories = METs * 3.5f * weight * minute * 5f / 1000f;

        return calories;
    }

    private String floatTo1stDecimal(float num) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return decimalFormat.format(num);
    }

}
