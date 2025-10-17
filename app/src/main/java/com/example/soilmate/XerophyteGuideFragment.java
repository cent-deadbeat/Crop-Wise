package com.example.soilmate;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;

public class XerophyteGuideFragment extends Fragment {

    private boolean isExpanded = false;
    private LinearLayout plantsContainer;
    private ImageView dropdownIcon;

    private String[] mesophytePlants = {
            "Acacia", "Agave", "Aloe Vera", "Cactus", "Orleander", "Pineapple", "Pines", "Succulents", "Euphorbia"
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_xerophyte_guide, container, false);

        ImageView plantImage = view.findViewById(R.id.plant_image);

        LinearLayout dropdownHeader = view.findViewById(R.id.dropdown_header);
        plantsContainer = view.findViewById(R.id.plants_container);
        dropdownIcon = view.findViewById(R.id.dropdown_icon);

        // Setup dropdown toggle
        dropdownHeader.setOnClickListener(v -> toggleDropdown());

        // Add plant names to container
        addPlantNames();

        // Safely load image
        try {
            plantImage.setImageResource(R.drawable.xerophyte_img);
            // OR using Glide (recommended)
            Glide.with(this)
                    .load(R.drawable.xerophyte_img)
                    .centerCrop()
                    .into(plantImage);
        } catch (Exception e) {
            Log.e("XerophyteFragment", "Image loading failed", e);
            plantImage.setVisibility(View.GONE);
        }
        return view;


    }

    private void toggleDropdown() {
        isExpanded = !isExpanded;
        plantsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        dropdownIcon.setImageResource(isExpanded ?
                R.drawable.ic_up_svgrepo_com : R.drawable.ic_dropdown_svgrepo_com);
    }

    private void addPlantNames() {
        plantsContainer.removeAllViews();
        LinearLayout rowLayout = null;

        for (int i = 0; i < mesophytePlants.length; i++) {
            if (i % 2 == 0) {
                rowLayout = new LinearLayout(getContext());
                rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                plantsContainer.addView(rowLayout);
            }

            TextView plantText = new TextView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            params.setMargins(0, 8, 16, 8);

            plantText.setLayoutParams(params);
            plantText.setText(mesophytePlants[i]);
            plantText.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            plantText.setTextSize(14);
            plantText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.poppinsregular));

            if (rowLayout != null) {
                rowLayout.addView(plantText);
            }
        }
    }
}
