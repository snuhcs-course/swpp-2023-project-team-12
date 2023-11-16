package com.example.runusandroid.ui.multi_mode;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runusandroid.R;

public class RecordDialog extends Dialog implements View.OnClickListener {

    public Context context;
    public ImageButton closeButton;
    public LinearLayout caloriesLayout;

    public TextView caloriesText;
    public MultiModeRecordAdapter adapter;
    RecyclerView recyclerView;

    public RecordDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.running_record_modal_layout);
        caloriesLayout = findViewById(R.id.calories);
        caloriesText = findViewById(R.id.caloriesText);
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
