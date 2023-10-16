package com.example.runusandroid;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.runusandroid.databinding.ActivityMain2Binding;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MainActivity2 extends AppCompatActivity {

    private ActivityMain2Binding binding;

    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new NetworkTask().execute();

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


    }



    private class NetworkTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            openSocket();
            // 여기에서 소켓 연결을 열고 다른 네트워크 작업 수행

            return null;
        }
    }
    public void openSocket() {
        try {
            socket = new Socket("10.0.2.2", 5001);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket(){
        return socket;
    }
    public ObjectOutputStream getOos(){
        return oos;
    }

    public ObjectInputStream getOis(){
        return ois;
    }
    public void closeSocket() {
        try {
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObjectOutputStream getObjectOutputStream() {
        return oos;
    }

    public ObjectInputStream getObjectInputStream() {
        return ois;
    }



}