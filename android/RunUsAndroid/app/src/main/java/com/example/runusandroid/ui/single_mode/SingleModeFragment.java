package com.example.runusandroid.ui.single_mode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class SingleModeFragment extends Fragment {

    private FragmentSingleModeBinding binding;
    private GoogleMap mMap;
    private List<LatLng> pathPoints = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SingleModeViewModel singleModeViewModel =
                new ViewModelProvider(this).get(SingleModeViewModel.class);

        binding = FragmentSingleModeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MainActivity2 mainActivity = (MainActivity2) getActivity();

        Button quitButton = (Button) binding.quitButton;

        // clicking the quit button will print location in log. for testing
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.getLastLocation();
            }
        });


        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Update interval in milliseconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //TODO: only draw lines if running is started
        //TODO: doesn't update location when app is in background -> straight lines are drawn from the last location when app is opened again
        //TODO: lines are ugly and noisy -> need to filter out some points or smoothed
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    Log.d("test:location", "Location:" + location.getLatitude() + ", " + location.getLongitude());

                    LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
                    pathPoints.add(newPoint);

                    // Update UI (draw line, zoom in)
                    if (mMap != null) {
                        mMap.clear(); // Remove previous polylines
                        mMap.addPolyline(new PolylineOptions().addAll(pathPoints).color(Color.BLUE).width(10));
                        if (newPoint != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPoint, 16));
                        }
                    }
                }
            }
        };

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity);

        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
        }
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

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
    }
}