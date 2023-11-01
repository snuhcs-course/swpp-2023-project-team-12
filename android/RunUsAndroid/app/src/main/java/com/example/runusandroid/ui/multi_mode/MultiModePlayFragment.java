package com.example.runusandroid.ui.multi_mode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.runusandroid.MainActivity2;
import com.example.runusandroid.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import MultiMode.MultiModeRoom;
import MultiMode.MultiModeUser;
import MultiMode.Packet;
import MultiMode.Protocol;
import MultiMode.UserDistance;

public class MultiModePlayFragment extends Fragment {

    private final List<LatLng> pathPoints = new ArrayList<>();
    LocalDateTime iterationStartTime;
    private final List<Double> pace = new ArrayList<>(); // 매 km 마다 속력 (km/h)
    MultiModeUser user = MultiModeFragment.user;
    //MultiModeUser user = new MultiModeUser(1, "choco");
    //MultiModeUser user = new MultiModeUser(2, "berry"); // 유저 정보 임시로 더미데이터 활용
    //MultiModeUser user = new MultiModeUser(3, "apple");

    SocketManager socketManager = SocketManager.getInstance();
    ObjectOutputStream oos;
    MultiModeRoom selectedRoom;
    double distance = 0;
    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    TextView paceGoalContentTextView;
    MainActivity2 mainActivity;
    TextView timeGoalContentTextView;
    TextView goldDistanceTextView;
    TextView goldNickNameTextView;
    TextView silverDistanceTextView;
    TextView silverNickNameTextView;
    TextView bronzeDistanceTextView;
    TextView bronzeNickNameTextView;
    ProgressBar progressBar;

    LocalDateTime gameStartTime;
    TextView distancePresentContentTextView; //API 사용해서 구한 나의 현재 이동 거리
    TextView pacePresentContentTextView; //API 사용해서 구한 나의 현재 페이스

    Button playLeaveButton;
    SocketListenerThread socketListenerThread = null;
    Handler timeHandler;
    Runnable timeRunnable;
    Handler sendDataHandler;
    Runnable sendDataRunnable;
    private TextView timePresentContentTextView;
    private int isFinished = 0;
    private ObjectInputStream ois;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        selectedRoom = (MultiModeRoom) getArguments().getSerializable("room");
        gameStartTime = LocalDateTime.now();
        iterationStartTime = gameStartTime;

        mainActivity = (MainActivity2) getActivity();
        locationRequest = LocationRequest.create();
        // TODO: let's find out which interval has best tradeoff
        locationRequest.setInterval(1000); // Update interval in milliseconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        View view = inflater.inflate(R.layout.fragment_multi_room_play, container, false); //각종 view 선언
        if (selectedRoom != null) {
            timeGoalContentTextView = view.findViewById(R.id.time_goal_content);
            timePresentContentTextView = view.findViewById(R.id.time_present_content);
            goldNickNameTextView = view.findViewById(R.id.gold_nickname);
            goldDistanceTextView = view.findViewById(R.id.gold_distance);
            silverNickNameTextView = view.findViewById(R.id.silver_nickname);
            silverDistanceTextView = view.findViewById(R.id.silver_distance);
            bronzeNickNameTextView = view.findViewById(R.id.bronze_nickname);
            bronzeDistanceTextView = view.findViewById(R.id.bronze_distance);
            distancePresentContentTextView = view.findViewById(R.id.distance_present_content);
            pacePresentContentTextView = view.findViewById(R.id.pace_present_content);
            progressBar = view.findViewById(R.id.linear_progress_bar);
            playLeaveButton = view.findViewById(R.id.play_leaveButton);
            //목표 시간 계산하기 위한 코드
            long secondsRemaining = selectedRoom.getDuration().getSeconds();

            // 시간, 분으로 변환
            long hours = secondsRemaining / 3600;
            long minutes = (secondsRemaining % 3600) / 60;
            long seconds = secondsRemaining % 60;
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                    hours, minutes, seconds);

            timeGoalContentTextView.setText(formattedTime);

        }

        playLeaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ExitGameTask().execute();
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.navigation_multi_mode);
            }
        });

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

                    int lastDistanceInt = (int) distance;

                    // TODO: check calculate distance
                    if (newPoint != null) {
                        // first few points might be noisy && while activity is running (or walking)
                        if (pathPoints.size() > 5 && mainActivity.activityReceiver.getIsRunning()) {
                            Location lastLocation = new Location("");
                            lastLocation.setLatitude(pathPoints.get(pathPoints.size() - 2).latitude);
                            lastLocation.setLongitude(pathPoints.get(pathPoints.size() - 2).longitude);
                            // unit : meter -> kilometer
                            distance += location.distanceTo(lastLocation) / (double) 1000;
                            Log.d("test:distance", "Distance:" + distance);
                            // update pace if new iteration started (every 1km)
                            if ((int)distance != lastDistanceInt) {
                                LocalDateTime currentTime = LocalDateTime.now();
                                Duration iterationDuration = Duration.between(iterationStartTime, currentTime);
                                long secondsDuration = iterationDuration.getSeconds();
                                double newPace = 1.0 / (secondsDuration / 3600.0);

                                pace.add(newPace);
                                iterationStartTime = currentTime;

                            }
                        }
                    }
                    // update distance text
                    distancePresentContentTextView.setText(String.format(Locale.getDefault(), "%.1f" + "km", distance));
                }
            }
        };

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainActivity);


        //경과 시간 업데이트
        timeHandler = new Handler(Looper.getMainLooper());

        // Runnable을 사용하여 매 초마다 시간 업데이트
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                if (selectedRoom != null) {
                    LocalDateTime currentTime = LocalDateTime.now();
                    //Duration present = Duration.between(selectedRoom.getStartTime(), currentTime);
                    Duration present = Duration.between(gameStartTime, currentTime);
                    long secondsElapsed = present.getSeconds();

                    // 시간, 분, 초로 변환
                    long hours = secondsElapsed / 3600;
                    long minutes = (secondsElapsed % 3600) / 60;
                    long seconds = secondsElapsed % 60;

                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                            hours, minutes, seconds);
                    Log.d("response", formattedTime);
                    timePresentContentTextView.setText(formattedTime);

                    // present가 목표 시간(selectedRoom.getDuration())과 같아지면 업데이트 중지
                    Log.d("response", "present : " + present.getSeconds());
                    Log.d("response", "getDuration : " + selectedRoom.getDuration().getSeconds());
                    if (present.getSeconds() >= selectedRoom.getDuration().getSeconds()) {
                        timeHandler.removeCallbacks(timeRunnable);
                        isFinished = 1;
                    }
                }
                if (isFinished == 0) {
                    // 1초마다 Runnable 실행
                    timeHandler.postDelayed(this, 1000);
                } else {
                    new SendFinishedTask().execute();
                }
            }
        };
        timeHandler.post(timeRunnable);

        //5초마다 현재 이동 거리 전송
        sendDataHandler = new Handler(Looper.getMainLooper());
        sendDataRunnable = new Runnable() {
            @Override
            public void run() {
                Packet requestPacket = new Packet(Protocol.UPDATE_USER_DISTANCE, user, distance);
                //distance += 1;
                new SendDistanceTask().execute(requestPacket);
                if (isFinished == 0) {
                    // 1초마다 Runnable 실행
                    sendDataHandler.postDelayed(this, 5000); // 5초마다 전송. 처음에 socketlistnerthread 설정 때문에 약간의 딜레이가 필요할 듯 함
                }
            }
        };

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start sendDataHandler after 5 seconds
                sendDataHandler.post(sendDataRunnable);
            }
        }, 5000); // 최초에 경기 시작하고 5초 뒤에 전송.

        return view;

    }


    public void updateTop3UserDistance(UserDistance[] userDistances) { // 화면에 표시되는 top3 유저 정보 업데이트. socketListenerThread에서 사용
        UserDistance[] top3UserDistance = userDistances;
        for (int i = 0; i < userDistances.length; i++) {

            Log.d("response", "In updateTop3UserDistance, top3user " + i + " : " + top3UserDistance[0].getUser().getNickName() + " , distance : " + userDistances[0].getDistance());
            Log.d("response", "In updateTop3UserDistance, user " + i + " : " + userDistances[0].getUser().getNickName() + " , distance : " + userDistances[0].getDistance());

        }
        double goldDistance = 0;

        if (top3UserDistance.length == 1) {
            goldNickNameTextView.setText(top3UserDistance[0].getUser().getNickName());
            goldDistance = top3UserDistance[0].getDistance();
            String goldDistanceString = String.format("%.3fkm", goldDistance);
            goldDistanceTextView.setText(goldDistanceString);

            silverNickNameTextView.setText("-");
            silverDistanceTextView.setText("-");

            bronzeNickNameTextView.setText("-");
            bronzeDistanceTextView.setText("-");
        } else if (top3UserDistance.length == 2) {
            goldNickNameTextView.setText(top3UserDistance[0].getUser().getNickName());
            goldDistance = top3UserDistance[0].getDistance();
            String goldDistanceString = String.format("%.3fkm", goldDistance);
            goldDistanceTextView.setText(goldDistanceString);

            silverNickNameTextView.setText(top3UserDistance[1].getUser().getNickName());
            double silverDistance = top3UserDistance[1].getDistance();
            String silverDistanceString = String.format("%.3fkm", silverDistance);
            silverDistanceTextView.setText(silverDistanceString);

            bronzeNickNameTextView.setText("-");
            bronzeDistanceTextView.setText("-");
        } else {
            goldNickNameTextView.setText(top3UserDistance[0].getUser().getNickName());
            goldDistance = top3UserDistance[0].getDistance();
            String goldDistanceString = String.format("%.3fkm", goldDistance);
            goldDistanceTextView.setText(goldDistanceString);

            silverNickNameTextView.setText(top3UserDistance[1].getUser().getNickName());
            double silverDistance = top3UserDistance[1].getDistance();
            String silverDistanceString = String.format("%.3fkm", silverDistance);
            silverDistanceTextView.setText(silverDistanceString);

            bronzeNickNameTextView.setText(top3UserDistance[2].getUser().getNickName());
            double bronzeDistance = top3UserDistance[2].getDistance();
            String bronzeDistanceString = String.format("%.3fkm", bronzeDistance);
            bronzeDistanceTextView.setText(bronzeDistanceString);
        }

        int progress = 0;
        if (goldDistance != 0) {
            progress = (int) ((int) distance / goldDistance) * 100;
        }
        progressBar.setProgress(progress);

    }

    @Override
    public void onResume() {
        super.onResume();
        socketListenerThread = (SocketListenerThread) getArguments().getSerializable("socketListenerThread"); //waitFragment의 socketListenrThread객체 가져와서 이어서 사용
        socketListenerThread.addPlayFragment(this);
        socketListenerThread.resumeListening();
        Log.d("response", "start play screen");
        //top3UpdateHandler.postDelayed(sendDataRunnable, 5000);

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
        if (socketListenerThread != null) {
            socketListenerThread.interrupt();
        }
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 타이머가 더 이상 필요하지 않을 때 핸들러를 제거합니다.
        timeHandler.removeCallbacks(timeRunnable);
    }

    private class SendDistanceTask extends AsyncTask<Packet, Void, Boolean> { // 서버에 업데이트할 거리 정보 전송
        @Override
        protected Boolean doInBackground(Packet... packets) {
            boolean success = true;
            try {
                if (packets.length > 0) {
                    // Get the first Packet to send from the parameters
                    Packet packetToSend = packets[0];

                    // Get the ObjectOutputStream from the socket manager
                    ObjectOutputStream oos = socketManager.getOOS();

                    // Write the Packet object to the output stream
                    oos.writeObject(packetToSend);
                    oos.flush();
                } else {
                    // Handle the case when no packets are provided
                    success = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
            return success;
        }

        // Handle the result of sending the packet
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Log.d("SendPacket", "Packet sent successfully! distance : " + distance);
            } else {
                Log.d("SendPacket", "Failed to send packet! distance : " + distance);
            }
        }
    }


    //경기 시간이 종료되었을 경우 해당 유저의 레이스가 종료되었다는 패킷을 보냄
    private class SendFinishedTask extends AsyncTask<Void, Void, Boolean> {
        Packet packet;

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = true;
            try {
                ObjectOutputStream oos = socketManager.getOOS();
                Packet requestPacket = new Packet(Protocol.FINISH_GAME, user, selectedRoom);
                oos.writeObject(requestPacket);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            } finally {
                timeHandler.removeCallbacksAndMessages(null);
                sendDataHandler.removeCallbacksAndMessages(null);
            }
            return success;
        }

        //ExitGameTask 실행 결과에 따라 수행
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Log.d("SendPacket", "Packet sent successfully!");

            } else {
                Log.d("ExitSendPacket", "Failed to send packet!");
            }
        }

    }

    private class ExitGameTask extends AsyncTask<Void, Void, Boolean> {
        Packet packet;

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = true;
            try {
                ObjectOutputStream oos = socketManager.getOOS();
                Packet requestPacket = new Packet(Protocol.EXIT_GAME, user, selectedRoom);
                oos.writeObject(requestPacket);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            } finally {
                try {

                    timeHandler.removeCallbacksAndMessages(null);
                    sendDataHandler.removeCallbacksAndMessages(null);
                    socketManager.closeSocket();

                    Log.d("response", "socket closed");


                } catch (IOException e) {
                    Log.d("response", "socket close error");
                    success = false;
                }

            }
            return success;
        }

        //ExitGameTask 실행 결과에 따라 수행
        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Log.d("SendPacket", "Packet sent successfully!");

            } else {
                Log.d("ExitSendPacket", "Failed to send packet!");
            }
        }

    }


}
