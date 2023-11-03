package com.example.runusandroid.ui.single_mode;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
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
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.example.runusandroid.databinding.FragmentSingleModeBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SingleModeFragment extends Fragment {

    private final List<LatLng> pathPoints = new ArrayList<>();
    Chronometer currentTimeText;
    SimpleDateFormat dateFormat;
    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    MainActivity2 mainActivity;
    double distance = 0;
    private FragmentSingleModeBinding binding;
    private GoogleMap mMap;
    private Location lastLocation = null;
    private float totalDistance = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SingleModeViewModel singleModeViewModel =
                new ViewModelProvider(this).get(SingleModeViewModel.class);

        binding = FragmentSingleModeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mainActivity = (MainActivity2) getActivity();
        Button showMissionButton = binding.showMissionButton;
        Button quitButton = binding.quitButton;
        Button startButton = binding.startButton;
        Button MissionButton = binding.showMissionButton;
        TextView currentDistanceText = binding.currentDistanceText;
        currentTimeText = binding.currentTimeText;
        currentTimeText.setBase(SystemClock.elapsedRealtime());
        TextView goalDistanceStaticText = binding.goalDistanceStaticText;
        TextView goalDistanceText = binding.goalDistanceText;
        TextView goalTimeStaticText = binding.goalTimeStaticText;
        TextView goalTimeText = binding.goalTimeText;

        MissionButton.setOnClickListener(new View.OnClickListener() {
            //TODO: 미션 생성 함수에서 받은 값으로 업데이트 해주어야 함
            @Override
            public void onClick(View v) {
                goalDistanceStaticText.setText("목표 거리");
                goalDistanceText.setText("5km");
                goalTimeStaticText.setText("목표 시간");
                goalTimeText.setText("01:00:00");
            }
        });

        dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(View.GONE);
                quitButton.setVisibility(View.VISIBLE);
                lastLocation = null;
                totalDistance = 0;
                currentDistanceText.setText("0.00 km");
                currentTimeText.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                    public void onChronometerTick(Chronometer chronometer) {
                        long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                        chronometer.setText(dateFormat.format(time));
                    }
                });
                currentTimeText.setBase(SystemClock.elapsedRealtime());
                currentTimeText.start();
            }
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitButton.setVisibility(View.GONE);
                startButton.setVisibility(View.VISIBLE);
                mainActivity.getLastLocation(); // ?
                currentTimeText.stop();
                View dialogView;
                Button confirmButton;
                boolean missionCompleted = true; // TODO: 변수명은 받아와야 함

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
                    TextView elapsedTimeTextView = dialogView.findViewById(R.id.textViewElapsedTimeonSuccess);
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
                    }
                });
                dialog.show();
                // TODO: 1. 미션 성공 여부 확인 2. DB에 러닝 데이터 저장 3. 미션 성공 or 실패 모달 꾸미기
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
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    Log.d("test:location", "Location:" + location.getLatitude() + ", " + location.getLongitude());

                    LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
                    pathPoints.add(newPoint);

                    // get distance
                    if (newPoint != null) {
                        // first few points might be noisy
                        if (pathPoints.size() > 5) {
                            Location lastLocation = new Location("");
                            lastLocation.setLatitude(pathPoints.get(pathPoints.size() - 2).latitude);
                            lastLocation.setLongitude(pathPoints.get(pathPoints.size() - 2).longitude);
                            // unit : meter -> kilometer
                            distance += location.distanceTo(lastLocation) / (double) 1000;
                            Log.d("test:distance", "Distance:" + distance);
                        }
                    }
                    // update distance text
                    currentDistanceText.setText(String.format(Locale.getDefault(), "%.1f" + "km", distance));

                    if (lastLocation != null) {
                        totalDistance += lastLocation.distanceTo(location);
                        // Update DistanceTextView
                        currentDistanceText.setText(String.format("%.2f km", totalDistance / 1000));
                    }
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
}