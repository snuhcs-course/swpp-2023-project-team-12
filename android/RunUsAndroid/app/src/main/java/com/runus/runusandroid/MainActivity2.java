package com.runus.runusandroid;


import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.runus.runusandroid.ActivityRecognition.UserActivityBroadcastReceiver;
import com.runus.runusandroid.ActivityRecognition.UserActivityTransitionManager;
import com.runus.runusandroid.databinding.ActivityMain2Binding;
import com.runus.runusandroid.ui.multi_mode.BackGroundSocketService;
import com.runus.runusandroid.ui.multi_mode.SocketManager;

import java.io.IOException;


public class MainActivity2 extends AppCompatActivity {

    static final String START_SOCKET_SERVICE = "start";
    static final String STOP_SOCKET_SERVICE = "stop";
    private final SocketManager socketManager = SocketManager.getInstance();
    public UserActivityBroadcastReceiver activityReceiver;
    public NavController navController;
    public Location initialLocation;
    UserActivityTransitionManager activityManager;
    PendingIntent pendingIntent;
    IntentFilter filter;
    private boolean isLogin = true;
    private ActivityMain2Binding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private PermissionSupport permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", null);
        Long lastLoginTime = sharedPreferences.getLong("lastLoginTime", 0);
        Long currentTime = System.currentTimeMillis();
        double elapsedTime = (currentTime - lastLoginTime) / 1000.0;

        if (elapsedTime >= 432000 || token == null) {
            // 토큰이 저장되어 있지 않으면 로그인되어 있지 않다고 판단하고 LoginActivity로 전환
            isLogin = false;
            Intent intent = new Intent(MainActivity2.this, LoginActivity.class);
            startActivity(intent);
            finish();  // MainActivity2를 종료
        }
        AccountAPIFactory accountFactory = AccountAPIFactory.getInstance();
        accountFactory.refreshToken(this);

        Log.d("socket service", "start");
        Intent intent = new Intent(this, BackGroundSocketService.class);
        intent.setAction(START_SOCKET_SERVICE);
        this.startForegroundService(intent);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main2);
        NavigationUI.setupWithNavController(binding.navView, navController);
        permissionCheck();

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
        this.registerReceiver(activityReceiver, filter, RECEIVER_EXPORTED);
        activityManager.registerActivityTransitions(pendingIntent);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (socketManager.getOIS() == null) {
            new Thread(() -> {
                try {
                    socketManager.openSocket();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }).start();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        }
    }

    @Override
    protected void onStop() {
        activityManager.removeActivityTransitions(pendingIntent);
        this.unregisterReceiver(activityReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isLogin) {
            Intent intent = new Intent(this, BackGroundSocketService.class);
            intent.setAction(STOP_SOCKET_SERVICE);
            this.startForegroundService(intent);
        }
        //heartbeatHandler.removeCallbacks(heartbeatRunnable);
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
            initialLocation = location;
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
        if (!permission.checkPermission()) {
            //권한 요청
            permission.requestPermission();
        }
    }

    // Request Permission에 대한 결과 값 받아와
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 권한이 전부 있다면 정상적으로 진행
        if (permission.checkPermission()) {
            Log.d("test:permission", "allPermissionGranted");
            createNotificationChannel();
            activityManager.removeActivityTransitions(pendingIntent);
            this.unregisterReceiver(activityReceiver);
            this.registerReceiver(activityReceiver, filter, RECEIVER_EXPORTED);
            activityManager.registerActivityTransitions(pendingIntent);
            getLastLocation();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACTIVITY_RECOGNITION)) {
            Log.d("test:permission", "showRationaleActivityRecognition");
            showRationaleActivityRecognition();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            Log.d("test:permission", "showRationaleAccessLocation");
            showRationaleAccessLocation();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.POST_NOTIFICATIONS)) {
            Log.d("test:permission", "showRationalePostNotifications");
            showRationalePostNotifications();
        } else {
            Log.d("test:permission", "unableToUseApp");
            showPermissionDialog();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // 최후통첩, 설정으로 가서 권한설정하고 오든가 앱을 떠나든가 해라
    private void showPermissionDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.AppTheme_AlertDialogTheme);
        builder.setTitle("권한 요청");
        builder.setMessage("앱을 사용하기 위해서는 권한이 필요합니다.");
        builder.setPositiveButton("설정", (dialog, which) -> {
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("종료", (dialog, which) -> finish());

        builder.setCancelable(false);
        builder.show();
    }

    private void showRationaleActivityRecognition() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.AppTheme_AlertDialogTheme);
        builder.setTitle("활동 권한");
        builder.setMessage("달리기 상태를 인식하기 위해 활동 권한이 필요합니다. RunUs는 어떠한 형태로도 수집된 활동 정보를 저장하지 않습니다.");
        builder.setPositiveButton("재설정", (dialog, which) -> {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1001);
        });
        builder.setNegativeButton("종료", (dialog, which) -> finish());

        builder.setCancelable(false);
        builder.show();
    }

    private void showRationaleAccessLocation() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.AppTheme_AlertDialogTheme);
        builder.setTitle("위치 권한");
        builder.setMessage("달리기 거리를 확인하기 위해 위치 권한이 필요합니다. RunUs는 유저 개인을 위한 목표 추천 목적 외 어떠한 형태로도 위치 정보를 저장 및 활용하지 않습니다.");
        builder.setPositiveButton("재설정", (dialog, which) -> {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
        });
        builder.setNegativeButton("종료", (dialog, which) -> finish());

        builder.setCancelable(false);
        builder.show();
    }

    private void showRationalePostNotifications() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.AppTheme_AlertDialogTheme);
        builder.setTitle("알림 권한");
        builder.setMessage("예정된 시작 시간을 알리기 위해 알림 권한이 필요합니다.");
        builder.setPositiveButton("재설정", (dialog, which) -> {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1003);
        });
        builder.setNegativeButton("종료", (dialog, which) -> finish());

        builder.setCancelable(false);
        builder.show();
    }

}