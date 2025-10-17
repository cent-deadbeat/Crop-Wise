package com.example.soilmate;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Watering_History extends AppCompatActivity {
    private ImageView emptyIcon;
    private TextView emptyText;

    private RecyclerView waterRecyclerView;
    private WateringhistoryAdapter adapter;
    private List<WaterhistoryModel> waterHistoryList;
    private static final int INITIAL_DISPLAY_COUNT = 10; // Number of items initially shown
    private boolean isShowingAll = false; // Tracks if all items are displayed
    private List<WaterhistoryModel> displayedHistoryList = new ArrayList<>();

    private Button seeMoreButton;


    private ImageButton removeButton, addPlantButton, filterButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watering_history);

        emptyIcon = findViewById(R.id.emptyIcon);
        emptyText = findViewById(R.id.emptyText);
        seeMoreButton = findViewById(R.id.see_more_button);
        seeMoreButton.setVisibility(View.GONE);



        ImageButton filterButton = findViewById(R.id.filter_button);
        filterButton.setOnClickListener(v -> showFilterDialog());


        //just change font and icon color for search bar
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchbar);

        TextView searchHint = findViewById(R.id.searchHint);

        searchView.setOnSearchClickListener(v -> searchHint.setVisibility(View.GONE));

        searchView.setOnCloseListener(() -> {
            searchHint.setVisibility(View.VISIBLE);
            return false;
        });

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);


        searchEditText.setHint("Search plants...");
        searchEditText.setHintTextColor(getResources().getColor(R.color.gray));
        searchEditText.setTextSize(16);


        searchView.setOnClickListener(v -> searchView.setIconified(false));


        searchEditText.setHintTextColor(getResources().getColor(R.color.gray));
        searchEditText.setTextColor(getResources().getColor(R.color.black));


        Typeface typeface = ResourcesCompat.getFont(this, R.font.poppinsregular);
        searchEditText.setTypeface(typeface);
        searchEditText.setTextColor(getResources().getColor(R.color.black));
        searchEditText.setHintTextColor(getResources().getColor(R.color.gray));








        removeButton = findViewById(R.id.remove_water);

        // Ensure trash icon is visible initially
        removeButton.setVisibility(View.GONE); // Initially hidden

        // Initialize RecyclerView
        waterRecyclerView = findViewById(R.id.waterRecyclerView);
        waterRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // HARD-CODED SAMPLE DATA
        waterHistoryList = new ArrayList<>();
        waterHistoryList.add(new WaterhistoryModel(       "Watered", "4:55 PM", "Friday, 14 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "2:30 PM", "Thursday, 08 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "6:15 AM", "Monday, 05 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "10:45 PM", "Saturday, 03 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "4:55 PM", "Friday, 14 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "2:30 PM", "Thursday, 08 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "6:15 AM", "Monday, 05 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "10:45 PM", "Saturday, 03 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "4:55 PM", "Friday, 14 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "2:30 PM", "Thursday, 08 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "6:15 AM", "Monday, 05 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "10:45 PM", "Saturday, 03 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "4:55 PM", "Friday, 14 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "2:30 PM", "Thursday, 08 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "6:15 AM", "Monday, 05 Feb, 2025"));
        waterHistoryList.add(new WaterhistoryModel("Watered", "10:45 PM", "Saturday, 03 Feb, 2025"));



        // Set Adapter
        adapter = new WateringhistoryAdapter(this, displayedHistoryList, this);
        waterRecyclerView.setAdapter(adapter);

        checkIfHistoryIsEmpty();


        // Show only the first 10 items
        updateDisplayedHistory();

        // Show "See More" button if more than 10 items exist
        if (waterHistoryList.size() > INITIAL_DISPLAY_COUNT) {
            seeMoreButton.setVisibility(View.VISIBLE);
        }




        // Click Listener for "See More"
        seeMoreButton.setOnClickListener(v -> {
            if (!isShowingAll) {
                displayedHistoryList.clear();
                displayedHistoryList.addAll(waterHistoryList);
                seeMoreButton.setText("See Less");
                isShowingAll = true;
            } else {
                updateDisplayedHistory(); // Show only 10 again
                seeMoreButton.setText("See More");
                isShowingAll = false;
            }
            adapter.notifyDataSetChanged();
        });






        ImageButton backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(v -> finish());

        addPlantButton = findViewById(R.id.addplant2);

        addPlantButton.setOnClickListener(v -> {
            AddPlantPopUp addPlantPopUp = new AddPlantPopUp();
            addPlantPopUp.show(getSupportFragmentManager(), "AddPlantPopUp");
        });
    }



    // Show confirmation popup before deleting selected items


    // Move selected items to trash and update UI


    // Toggle selection mode (Show/Hide trash and remove buttons)


    // In Watering_History.java
    private void checkIfHistoryIsEmpty() {
        if (displayedHistoryList.isEmpty()) {
            emptyIcon.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.VISIBLE);
            waterRecyclerView.setVisibility(View.GONE);
        } else {
            emptyIcon.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
            waterRecyclerView.setVisibility(View.VISIBLE);
        }
    }


    private void updateDisplayedHistory() {
        displayedHistoryList.clear();

        int itemCount = Math.min(INITIAL_DISPLAY_COUNT, waterHistoryList.size());
        for (int i = 0; i < itemCount; i++) {
            displayedHistoryList.add(waterHistoryList.get(i));
        }

        adapter.notifyDataSetChanged();

        // Show "See More" button only if there are more than 10 items
        if (waterHistoryList.size() > INITIAL_DISPLAY_COUNT) {
            seeMoreButton.setVisibility(View.VISIBLE);
        } else {
            seeMoreButton.setVisibility(View.GONE); // Hide when ≤ 10 items
        }

        // Check if the list is empty
        checkIfHistoryIsEmpty();
    }


    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Watering History");

        String[] filterOptions = {"Today", "This Week", "This Month", "Custom Date"};
        builder.setItems(filterOptions, (dialog, which) -> {
            String selectedFilter = filterOptions[which];

            if (selectedFilter.equals("Custom Date")) {
                showDatePicker();
            } else {
                applyFilter(selectedFilter);
            }
        });

        builder.show();
    }


    private void applyFilter(String filterType) {
        List<WaterhistoryModel> filteredList = new ArrayList<>();

        for (WaterhistoryModel history : waterHistoryList) {
            if (matchesFilter(history, filterType)) {
                filteredList.add(history);
            }
        }

        displayedHistoryList.clear();
        displayedHistoryList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        // Hide "See More" button if the filtered results are ≤ 10
        if (filteredList.size() > INITIAL_DISPLAY_COUNT) {
            seeMoreButton.setVisibility(View.VISIBLE);
        } else {
            seeMoreButton.setVisibility(View.GONE);
        }

        // Check if history is empty
        checkIfHistoryIsEmpty();
    }




    private boolean matchesFilter(WaterhistoryModel history, String filterType) {
        // Dummy logic (replace with actual date filtering)
        if (filterType.equals("Today") && history.getDate().contains("14 Feb")) {
            return true;
        } else if (filterType.equals("This Week") && history.getDate().contains("Feb")) {
            return true;
        } else if (filterType.equals("This Month") && history.getDate().contains("2025")) {
            return true;
        }
        return false;
    }



    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + " " + getMonthName(month) + ", " + year;
                    applyFilter(selectedDate);
                }, 2025, 1, 14);
        datePickerDialog.show();
    }

    private String getMonthName(int month) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return months[month];
    }










}
