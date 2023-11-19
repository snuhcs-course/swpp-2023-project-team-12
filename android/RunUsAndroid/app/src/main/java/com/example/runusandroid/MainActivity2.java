package com.example.runusandroid;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.runusandroid.ActivityRecognition.UserActivityBroadcastReceiver;
import com.example.runusandroid.ActivityRecognition.UserActivityTransitionManager;
import com.example.runusandroid.databinding.ActivityMain2Binding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity2 extends AppCompatActivity {

    public UserActivityBroadcastReceiver activityReceiver;
    UserActivityTransitionManager activityManager;
    PendingIntent pendingIntent;
    private ActivityMain2Binding binding;
    private FusedLocationProviderClient fusedLocationClient;
    public NavController navController;
    private PermissionSupport permission;
    IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        Long lastLoginTime = sharedPreferences.getLong("lastLoginTime", 0);
        Long currentTime = System.currentTimeMillis();
        double elapsedTime = (currentTime - lastLoginTime)/1000.0;

        if (elapsedTime >= 432000 || token == null) {
            // 토큰이 저장되어 있지 않으면 로그인되어 있지 않다고 판단하고 LoginActivity로 전환
            Intent intent = new Intent(MainActivity2.this, LoginActivity.class);
            startActivity(intent);
            finish();  // MainActivity2를 종료
        }
        AccountAPIFactory accountFactory = AccountAPIFactory.getInstance();
        accountFactory.refreshToken(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main2);
        NavigationUI.setupWithNavController(binding.navView, navController);
        permissionCheck();
        // REMOVAL : permission check moved to PermissionSupport.java
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1001);
//            }
//        }
//        // Request location permission
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1002);
//        }
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1003);
//        }
//        if (Build.VERSION.SDK_INT >= 33) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1004);
//            }
//        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        activityManager = new UserActivityTransitionManager(this);
        pendingIntent = PendingIntent.getBroadcast(
                this,
                UserActivityTransitionManager.CUSTOM_REQUEST_CODE_USER_ACTION,
                new Intent(UserActivityTransitionManager.CUSTOM_INTENT_USER_ACTION),
                PendingIntent.FLAG_MUTABLE
        );
        activityReceiver = new UserActivityBroadcastReceiver();
        filter = new IntentFilter(UserActivityTransitionManager.CUSTOM_INTENT_USER_ACTION);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("test:lifecycle:main", "onStart");
        this.registerReceiver(activityReceiver, filter, RECEIVER_EXPORTED);
        activityManager.registerActivityTransitions(pendingIntent);

    }


    @Override
    protected void onStop() {
        activityManager.removeActivityTransitions(pendingIntent);
        this.unregisterReceiver(activityReceiver);
        super.onStop();
    }

    public FusedLocationProviderClient getFusedLocationClient() {
        return fusedLocationClient;
    }

    // Get the last location. Currently printing log only. TODO: return the location
    public void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1003);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1004);
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            // Handle location update here
            if (location != null) {
                Log.d("test:location:main", "Location:" + location.getLatitude() + ", " + location.getLongitude());
            } else {
                Log.d("test:location:main", "Location failed");
            }
        });
    }

    public void createNotificationChannel() {
        CharSequence name = getString(R.string.channel_name);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("MultiModeWait", name, importance);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    // 권한 체크
    private void permissionCheck() {

        // PermissionSupport.java 클래스 객체 생성
        permission = new PermissionSupport(this, this);

        // 권한 체크 후 리턴이 false로 들어오면
        if (!permission.checkPermission()){
            //권한 요청
            permission.requestPermission();
        }
    }

    // Request Permission에 대한 결과 값 받아와
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //여기서도 리턴이 false로 들어온다면 (사용자가 권한 허용 거부)
        if (!permission.permissionResult(requestCode, permissions, grantResults)) {
            // 다시 permission 요청
            permission.requestPermission();
        } else{
            createNotificationChannel();
            activityManager.removeActivityTransitions(pendingIntent);
            this.unregisterReceiver(activityReceiver);
            this.registerReceiver(activityReceiver, filter, RECEIVER_EXPORTED);
            activityManager.registerActivityTransitions(pendingIntent);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}