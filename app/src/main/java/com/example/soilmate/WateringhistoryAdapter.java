package com.example.soilmate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WateringhistoryAdapter extends RecyclerView.Adapter<WateringhistoryAdapter.ViewHolder> {
    private List<WaterhistoryModel> historyList;
    private Context context;
    private boolean isSelectionMode = false;
    private Watering_History activity;
    private Set<Integer> selectedItems = new HashSet<>();

    public WateringhistoryAdapter(Context context, List<WaterhistoryModel> historyList, @Nullable Watering_History activity) {
        this.context = context;
        this.historyList = historyList;
        this.activity = activity; // Accept null
    }

    public void setSelectionMode(boolean enabled) {
        isSelectionMode = enabled;
        if (!enabled) selectedItems.clear();
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedItems() {
        return selectedItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemwater_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaterhistoryModel item = historyList.get(position);
        holder.plantName.setText(item.getStatus());
        holder.time.setText(item.getTime());
        holder.date.setText(item.getDate());





    }


    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView plantName, time, date;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.plantName);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.date);
            checkBox = itemView.findViewById(R.id.historyCheckBox);
        }
    }
}
