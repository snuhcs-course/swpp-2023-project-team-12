package com.runus.runusandroid.ui.multi_mode;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.runus.runusandroid.R;

import java.util.ArrayList;

public class MultiModeRecordAdapter extends RecyclerView.Adapter<MultiModeRecordAdapter.RecordViewHolder> {
    private final ArrayList<RecordItem> items = new ArrayList<>();

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.record_item, parent, false);

        return new RecordViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        holder.setItem(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(RecordItem item) {
        items.add(item);
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView runSectionTextView;
        TextView averagePaceTextView;

        RecordViewHolder(View itemView) {
            super(itemView);

            runSectionTextView = itemView.findViewById(R.id.runSection);
            averagePaceTextView = itemView.findViewById(R.id.average_pace);
        }

        void setItem(RecordItem item) {
            String sectionString;
            if (Math.abs(item.getSection() - (int) item.getSection()) < 0.0005) {
                sectionString = String.valueOf((int) item.getSection());
            } else {
                Log.d("speedlist", item.getSection() + "");
                sectionString = String.format("%.3f", item.getSection());
            }

            double averageSpeed = item.getAverageSpeed();
            String paceString;
            if (averageSpeed < 1) {
                paceString = "59'59''";
            } else {
                double pace = 1.0 / (averageSpeed / 60.0);
                int paceMinutes = (int) pace;
                int paceSeconds = (int) ((pace - paceMinutes) * 60.0);
                paceString = String.format("%02d'%02d''", paceMinutes, paceSeconds);
            }


            runSectionTextView.setText(sectionString);
            averagePaceTextView.setText(paceString);


        }


    }

}
