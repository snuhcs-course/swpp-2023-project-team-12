package com.example.runusandroid.ui.multi_mode;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runusandroid.R;

public class RecordDialog extends Dialog implements View.OnClickListener {

    public Context context;
    public ImageButton closeButton;
    RecyclerView recyclerView;

    MultiModeRecordAdapter adapter;

    public RecordDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.running_record_modal_layout);

        closeButton = findViewById(R.id.buttonClose);
        closeButton.setOnClickListener(this);

        recyclerView = findViewById(R.id.recordRecyclerView);
        adapter = new MultiModeRecordAdapter();
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonClose) {
            dismiss();
        }
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }
}
