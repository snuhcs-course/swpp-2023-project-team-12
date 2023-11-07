package com.example.runusandroid.ui.single_mode;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runusandroid.HistoryApi;
import com.example.runusandroid.HistoryData;
import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.example.runusandroid.RetrofitClient;
import com.example.runusandroid.databinding.FragmentSingleModeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SingleModeFragment extends Fragment {

    private final List<LatLng> pathPoints = new ArrayList<>();
    private final List<Float> speedList = new ArrayList<>(); // 매 km 마다 속력 (km/h)
    Chronometer currentTimeText;
    SimpleDateFormat dateFormat;
    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    MainActivity2 mainActivity;
    LocalDateTime gameStartTime;
    LocalDateTime iterationStartTime;
    float calories = 0; //TODO: 칼로리 계산
    double distance = 0;
    private HistoryApi historyApi;
    private FragmentSingleModeBinding binding;
    private GoogleMap mMap;
    private Location lastLocation = null;
    private float minSpeed;
    private float maxSpeed;

    private float goalDistance;
    private float goalTime;

    private boolean runningNow;

    private Interpreter tflite;
    private MappedByteBuffer tfliteModel;
    private float[][] modelInput = new float[5][3];
    private float[][] modelOutput = new float[1][2];

    private boolean timeLimitLess = true;

    private float[][] standard = {{2.41f, 2.38f, 2.32f, 2.21f}, {2.04f, 1.96f, 1.88f, 1.79f}};


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SingleModeViewModel singleModeViewModel =
                new ViewModelProvider(this).get(SingleModeViewModel.class);

        binding = FragmentSingleModeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainActivity = (MainActivity2) getActivity();
        historyApi = RetrofitClient.getClient().create(HistoryApi.class);

        Button quitButton = binding.quitButton;
        Button startButton = binding.startButton;
        TextView currentDistanceText = binding.currentDistanceText;
        currentTimeText = binding.currentTimeText;
        currentTimeText.setBase(SystemClock.elapsedRealtime());
        TextView goalDistanceStaticText = binding.goalDistanceStaticText;
        TextView goalDistanceText = binding.goalDistanceText;
        TextView goalTimeStaticText = binding.goalTimeStaticText;
        TextView goalTimeText = binding.goalTimeText;
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

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(View.GONE);

                boolean enoughHistory = modelInput[4][2] == 0 ? false : true;
                if (!enoughHistory) {
                    setStandard();
                }


                View dialogView = getLayoutInflater().inflate(R.layout.dialog_mission_start, null);
                AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
                dialog.setView(dialogView);
                dialog.show();

                Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
                Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

                SeekBar distanceSeekBar =  dialogView.findViewById(R.id.distanceSeekBar);
                TextView distanceTextView = dialogView.findViewById(R.id.textViewGoalDistance);

                distanceSeekBar.setProgress((int) (goalDistance*10));

                String formattedDistance = String.format("%.1f", goalDistance);
                distanceTextView.setText(String.valueOf(formattedDistance) + " km");

                SeekBar timeSeekBar =  dialogView.findViewById(R.id.timeSeekBar);
                TextView timeTextView = dialogView.findViewById(R.id.textViewGoalTime);

                timeSeekBar.setProgress((int) goalTime);
                timeTextView.setText(String.valueOf((int) goalTime)+" 분");

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
                        distanceTextView.setText(String.valueOf(progress / 10.0f)+"km");

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
                        timeTextView.setText(String.valueOf(progress) +"분");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                buttonConfirm.setOnClickListener(new Button.OnClickListener(){
                    public void onClick(View v){
                        goalDistanceStaticText.setText("목표 거리");
                        goalDistance = distanceSeekBar.getProgress()/100.0f;
                        goalTime = timeSeekBar.getProgress();
                        String formattedDistance = String.format("%.1f", goalDistance);
                        goalDistanceText.setText(String.valueOf(formattedDistance) + " km");
                        goalTimeStaticText.setText("목표 시간");
                        int roundedGoalTime = Math.round(goalTime);
                        goalTimeText.setText(String.valueOf(roundedGoalTime) + " 분");

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

                        quitButton.setVisibility(View.VISIBLE);

                        dialog.dismiss();
                    }
                });

                buttonCancel.setOnClickListener(new Button.OnClickListener(){
                    public void onClick(View v){

                        startButton.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });



            }
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runningNow = false;
                quitButton.setVisibility(View.GONE);
                startButton.setVisibility(View.VISIBLE);
                mainActivity.getLastLocation();
                currentTimeText.stop();
                View dialogView;
                Button confirmButton;
                boolean missionCompleted = false;
                float wholeDistance = Float.valueOf((String) currentDistanceText.getText().subSequence(0,currentDistanceText.getText().length()-2));
                float wholeTime = (float) Duration.between(gameStartTime, LocalDateTime.now()).getSeconds();
                if (timeLimitLess){
                    if(wholeDistance>=goalDistance && wholeTime < goalTime*60) {
                        missionCompleted = true;
                    }
                }
                else{
                    if(wholeDistance>=goalDistance && wholeTime >= goalTime*60){
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
            }
        });

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Update interval in milliseconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //TODO: only draw lines if running is started
        //TODO: doesn't update location when app is in background -> straight lines are drawn from the last location when app is opened again
        //TODO: lines are ugly and noisy -> need to filter out some points or smoothed
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && runningNow) {
                    Location location = locationResult.getLastLocation();
                    Log.d("test:location", "Location:" + location.getLatitude() + ", " + location.getLongitude());

                    LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
                    pathPoints.add(newPoint);

                    int lastDistanceInt = (int) distance;

                    // get distance
                    if (newPoint != null) {
                        // first few points might be noisy
                        if (pathPoints.size() > 5) {
                            Location lastLocation = new Location("");
                            lastLocation.setLatitude(pathPoints.get(pathPoints.size() - 2).latitude);
                            lastLocation.setLongitude(pathPoints.get(pathPoints.size() - 2).longitude);
                            // unit : meter -> kilometer
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
                            Log.d("test:distance", "Distance:" + distance);
                        }
                    }
                    // update distance text
                    currentDistanceText.setText(String.format(Locale.getDefault(), "%.2f " + "km", distance));

                    lastLocation = location;
                }
            }
        };

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity);


        // Finding the visual component displaying the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        // Initialize the map
        mapFragment.getMapAsync(googleMap -> {
            mMap = googleMap;
            // Check the permission and enable the location marker
            if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        });


        // Viewmodel contains status, and when status changes (observe), the text will change
        final TextView textView = binding.textSingleMode;
        singleModeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        return root;
    }

    private void setStandard() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        int gender = sharedPreferences.getInt("gender", 0) - 1;

        if (gender<0) gender =0;
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
    }

    @Override
    public void onResume() {
        super.onResume();
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
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    void saveHistoryDataOnSingleMode() throws JSONException {
        if ((int) minSpeed == 999) minSpeed = 0;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", MODE_PRIVATE);
        Long userId = sharedPreferences.getLong("userid", -1); //TODO: -1이면 안되긴하는데, catch해야 함.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String startTimeString = gameStartTime.format(formatter);
        String finishTimeString = LocalDateTime.now().format(formatter);
        // long durationInSeconds = Duration.between(gameStartTime, LocalDateTime.now()).getSeconds();
        long durationInSeconds = 3600L;
        distance = 5.2;
                //NOTE: group_history_id에 null을 넣을 수 없어 싱글모드인 경우 -1로 관리
        HistoryData requestData = new HistoryData(userId, (float) distance, durationInSeconds,
                true, startTimeString, finishTimeString, calories, false, maxSpeed, minSpeed, calculateMedian(speedList), speedList, -1);

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
        if (size == 0) return 0;

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

        for (float [] row : array) {
            for (float value : row){
                result.append(value).append(" ");
            }
            result.append("\n");
        }

        return result.toString();
    }

    private void getMission(){
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
                            float recentDuration = convertTimetoHour(historyObject.getString("duration")) ;

                            modelInput[i][0] = (gender - 0.9111115f) / 0.3117750f;
                            modelInput[i][1] = (recentDistance - 1.207809e+01f) / 7.019781e+00f;
                            modelInput[i][2] = (recentDuration - 1.156572e+00f) / 6.457635e-01f;
                            wholeDistance +=recentDistance;
                            wholeTime += recentDuration;

                        }
                        wholeTime /= 5;
                        wholeDistance /= 5;

                        String inputString = convertArrayToString(modelInput);
                        tflite.run(modelInput, modelOutput);
                        goalDistance = modelOutput[0][0] * 7.019781e+00f + 1.207809e+01f;
                        goalTime = (modelOutput[0][1] * 6.457635e-01f +  1.156572e+00f);


                        Log.e("mission", convertArrayToString(modelInput));
                        Log.e("mission", "original : "+String.valueOf(goalDistance)+ " "+String.valueOf(goalTime));

                        if (goalDistance/goalTime >= 1.3*wholeDistance/wholeTime){
                            goalDistance = goalTime* 1.3f*wholeDistance/wholeTime;
                        }
                        if (goalDistance >= 1.3f * wholeDistance){
                            goalDistance /= 1.3f;
                            goalTime /= 1.3f;
                        }
                        goalTime *= 60;
                        Log.e("mission", "Revised : "+String.valueOf(goalDistance)+ " "+String.valueOf(goalTime));
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

}

