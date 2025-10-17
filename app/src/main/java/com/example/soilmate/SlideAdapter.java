package com.example.soilmate;


import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class SlideAdapter extends RecyclerView.Adapter<SlideAdapter.SlideViewHolder> {
    private final List<Integer> slideLayouts;
    private final ViewPager2 viewPager; // Pass ViewPager2

    public SlideAdapter(List<Integer> slideLayouts, ViewPager2 viewPager) {
        this.slideLayouts = slideLayouts;
        this.viewPager = viewPager;
    }

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(slideLayouts.get(viewType), parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        // Handle button clicks for each slide
        if (position == 0) { // First slide
            View btnNext = holder.itemView.findViewById(R.id.next);
            if (btnNext != null) {
                btnNext.setOnClickListener(v -> viewPager.setCurrentItem(1, true)); // Move to next slide
            }
        } else if (position == 1) { // Second slide
            View btnLogin = holder.itemView.findViewById(R.id.start);
            if (btnLogin != null) {
                btnLogin.setOnClickListener(v -> {
                    v.getContext().startActivity(new Intent(v.getContext(), Login.class));
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return slideLayouts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        public SlideViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
