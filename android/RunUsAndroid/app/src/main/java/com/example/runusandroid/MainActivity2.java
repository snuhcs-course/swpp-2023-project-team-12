package com.example.runusandroid;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.example.runusandroid.ActivityRecognition.UserActivityBroadcastReceiver;
import com.example.runusandroid.ActivityRecognition.UserActivityTransitionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.runusandroid.databinding.ActivityMain2Binding;

public class MainActivity2 extends AppCompatActivity {

    private ActivityMain2Binding binding;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main2);
        NavigationUI.setupWithNavController(binding.navView, navController);
        /*
        // 10.12: 없어도 되는것 같다.
        navView.setOnNavigationItemSelectedListener(item -> {
            int selected = item.getItemId();
            if (selected == R.id.navigation_single_mode) {
                navController.navigate(R.id.navigation_single_mode);
            } else if (selected == R.id.navigation_multi_mode) {
                navController.navigate(R.id.navigation_multi_mode);
            } else if (selected == R.id.navigation_home) {
                navController.navigate(R.id.navigation_home);
            } else if (selected == R.id.navigation_history) {
                navController.navigate(R.id.navigation_history);
            } else if (selected == R.id.navigation_user_setting) {
                navController.navigate(R.id.navigation_user_setting);
            }
            return true;
        }
        );
        */

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1000);
        }

        UserActivityTransitionManager activityManager = new UserActivityTransitionManager(this);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                UserActivityTransitionManager.CUSTOM_REQUEST_CODE_USER_ACTION,
                new Intent(UserActivityTransitionManager.CUSTOM_INTENT_USER_ACTION),
                PendingIntent.FLAG_MUTABLE
        );
        activityManager.registerActivityTransitions(pendingIntent);
        UserActivityBroadcastReceiver activityReceiver = new UserActivityBroadcastReceiver();
        IntentFilter filter = new IntentFilter(UserActivityTransitionManager.CUSTOM_INTENT_USER_ACTION);
        this.registerReceiver(activityReceiver, filter, RECEIVER_EXPORTED);
    }
}