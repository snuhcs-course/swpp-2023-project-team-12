package com.example.runusandroid.ui.single_mode;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.runusandroid.ExpSystem;
import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.example.runusandroid.databinding.FragmentSingleModeResultBinding;
import com.example.runusandroid.ui.multi_mode.RecordDialog;
import com.example.runusandroid.ui.multi_mode.RecordItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SingleModeResultFragment extends Fragment {
    private final Location lastLocation = null;
    GoogleMap mMap;
    MainActivity2 mainActivity;
    SimpleDateFormat dateFormat;
    SingleModeFragment singleModeFragment;
    RecordDialog dialog;
    boolean isDialogOpenedBefore = false;
    NavController navController;
    OnBackPressedCallback backPressedCallBack;
    private float calories = 0;
    private float goalDistance;
    private float goalTime;
    private double currentDistance;
    private long currentTime;
    private List<Float> speedList = new ArrayList<>();
    private List<LatLng> pathPoints = new ArrayList<>();
    private LatLng lastPoint;
    private FragmentSingleModeResultBinding binding;
    private TextView goalDistanceStaticText;
    private TextView goalDistanceText;
    private TextView goalTimeStaticText;
    private TextView goalTimeText;
    private Button quitButton;
    private LinearLayout currentPace;
    private LinearLayout caloriesLayout;
    private TextView caloriesText;
    private Chronometer currentTimeText;
    private TextView currentDistanceText;
    private Button paceDetailButton;
    private boolean isMapReady = true;
    private long backButtonLastClickTime = 0;

    private int updatedExp;
    private int updatedBadgeCollection;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity2) getActivity();
        dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        goalDistance = (float) getArguments().getSerializable("goalDistance");
        goalTime = (float) getArguments().getSerializable("goalTime");
        currentDistance = (double) getArguments().getSerializable("currentDistance");
        currentTime = (long) getArguments().getSerializable("currentTime");
        calories = (float) getArguments().getSerializable("calories");
        speedList = (List<Float>) getArguments().getSerializable("userSpeedList");
        pathPoints = (List<LatLng>) getArguments().getSerializable("pathPointList");
        updatedExp = (int) getArguments().getSerializable("updatedExp");
        Log.d("UAT:exp", "updatedExp OnCreate is " + updatedExp);
        updatedBadgeCollection = (int) getArguments().getSerializable("updatedBadgeCollection");
        if (pathPoints != null && pathPoints.size() != 0) {
            Log.d("mMapCheck", "lastpoint is not null");
            lastPoint = pathPoints.get(pathPoints.size() - 1);
        }


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("mMapCheck", "onCreateView");
        binding = FragmentSingleModeResultBinding.inflate(inflater, container, false);

        View root = binding.getRoot();
        currentPace = binding.currentPace;
        quitButton = binding.quitButton;
        currentDistanceText = binding.currentDistanceText;
        currentTimeText = binding.currentTimeText;
        goalDistanceStaticText = binding.goalDistanceStaticText;
        goalDistanceText = binding.goalDistanceText;
        goalTimeStaticText = binding.goalTimeStaticText;
        goalTimeText = binding.goalTimeText;
        paceDetailButton = binding.paceDetailButton;
        dialog = new RecordDialog(requireContext());

        int updatedLevel = ExpSystem.getLevel(updatedExp);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("exp", updatedExp);
        editor.putInt("badge_collection", updatedBadgeCollection);
        int pastLevel = sharedPreferences.getInt("level", -1);
        if (pastLevel < updatedLevel) {
            editor.putInt("level", updatedLevel);
            showLevelUpDialog(pastLevel, updatedLevel);
            Log.d("response", "past level is " + pastLevel + ", and updated level is " + updatedLevel);
        }
        editor.apply();
        Log.d("UAT:exp", "update_exp is + " + sharedPreferences.getInt("exp", -1));


        // Finding the visual component displaying the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_result);
        // Initialize the map
        mapFragment.getMapAsync(googleMap -> {
            mMap = googleMap;
            Log.d("mMapCheck", "mMap is " + mMap);

            // Check the permission and enable the location marker
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
            isMapReady = true;

            updateMap();
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showExitResultDialog();
            }
        });

        paceDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                if (!isDialogOpenedBefore) {
                    Log.d("speedList", speedList.size() + "");
                    double section = 1.0;

                    while (true) {
                        if (currentDistance - section >= 0) {
                            float speed = speedList.get((int) section - 1);
                            dialog.adapter.addItem(new RecordItem(section, speed));
                            Log.d("speedList", "first if : " + section + " " + speed);

                            section++;
                        } else {
                            if (speedList.size() > 0) {
                                float speed = speedList.get((int) section - 1);
                                dialog.adapter.addItem(new RecordItem(currentDistance - (section - 1), speed));
                                Log.d("speedList", "second if : " + (section - 1) + " " + speed);
                            }
                            section = 1.0;
                            break;
                        }
                    }
                    dialog.adapter.notifyDataSetChanged();
                    dialog.caloriesText.setText(calories + " kcal");
                    isDialogOpenedBefore = true;
                }
            }
        });


        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("mMapCheck", "onResume");
        backPressedCallBack = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (SystemClock.elapsedRealtime() - backButtonLastClickTime < 1000) {
                    return;
                }
                backButtonLastClickTime = SystemClock.elapsedRealtime();
                showExitResultDialog();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, backPressedCallBack);

        goalDistanceText.setText(floatToFirstDeciStr(goalDistance) + "km");
        currentDistanceText.setText(String.format(Locale.getDefault(), "%.1f " + "km", Math.floor(currentDistance * 10) / 10));
        if (goalTime != -1) {
            goalTimeText.setText(goalTime + " 분");
        } else {
            goalTimeText.setText("--");
        }
        currentTimeText.setText(dateFormat.format(currentTime));
        if (isMapReady) {
            updateMap();
        }


    }

    @Override
    public void onStart() {
        Log.d("mMapCheck", "onStart");

        super.onStart();

    }

    private String floatToFirstDeciStr(float num) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return decimalFormat.format(num);
    }

    private void updateMap() {
        if (mMap != null) {
            Log.d("mMapCheck", "mMap is not null");

            mMap.clear(); // Remove previous polylines
            mMap.addPolyline(new PolylineOptions().addAll(pathPoints).color(Color.parseColor("#4AA570")).width(10));
            if (lastPoint != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastPoint, 16));
            }
        } else {
            Log.d("mMapCheck", "mMap is null");
        }
    }

    private void showExitResultDialog() {
        @SuppressLint("InflateParams")
        View exitResultDialog = getLayoutInflater().inflate(R.layout.dialog_multimode_play_finish, null);
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(exitResultDialog);
        Button buttonConfirmPlayExit = exitResultDialog.findViewById(R.id.buttonConfirmPlayExit);
        buttonConfirmPlayExit.setOnClickListener(v -> {
            dialog.dismiss();
            navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.navigation_single_mode);
        });
        TextView textViewExitResult = exitResultDialog.findViewById(R.id.textViewExitGame);
        textViewExitResult.setText(R.string.MultiModeResultExitMessage);
        dialog.show();
    }

    public void showLevelUpDialog(int pastLevel, int updatedLevel) {
        View levelUpDialogView = getLayoutInflater().inflate(R.layout.dialog_level_up, null);
        Dialog levelUpDialog = new Dialog(requireContext());
        levelUpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(levelUpDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        levelUpDialog.setContentView(levelUpDialogView);
        Button buttonConfirm = levelUpDialog.findViewById(R.id.buttonConfirmLevelUp);
        buttonConfirm.setOnClickListener(v -> {
            levelUpDialog.dismiss();
        });
        TextView textViewExitResult = levelUpDialogView.findViewById(R.id.textViewLevelUp);
        textViewExitResult.setText("Level " + pastLevel + "  ->  Level " + updatedLevel);
        levelUpDialog.show();
    }

}
