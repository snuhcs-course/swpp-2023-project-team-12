package com.example.runusandroid.ui.multi_mode;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runusandroid.R;

import java.util.Arrays;
import java.util.List;

public class MultiModeFragment extends Fragment {

    private Button createRoomButton;

    private void showModal(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 없애기
        dialog.setContentView(R.layout.create_room_modal_layout); // 모달에 사용할 레이아웃 지정

        // 모달 외부 클릭 시 모달 닫기
        dialog.setCanceledOnTouchOutside(true);

        // 모달에 있는 버튼 등의 클릭 리스너도 여기에 설정 가능

        dialog.show();
    }

    public MultiModeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_multi_mode, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        List<String> items = Arrays.asList("Item 1", "Item 2", "Item 3", "item 4", "item 5");
        MultiModeAdapter adapter = new MultiModeAdapter(items);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        createRoomButton = view.findViewById(R.id.createRoomButton);
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModal(v.getContext());
            }
        });

        return view;


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
