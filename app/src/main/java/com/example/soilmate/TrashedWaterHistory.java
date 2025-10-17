package com.example.soilmate;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TrashedWaterHistory extends AppCompatActivity {
    private RecyclerView trashedWaterRecyclerView;
    private ImageView emptyTrashIcon;
    private TextView emptyTrashText;
    private WateringhistoryAdapter adapter;
    private List<WaterhistoryModel> trashedWaterHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trashed_water_history);

        emptyTrashIcon = findViewById(R.id.emptyIcon);
        emptyTrashText = findViewById(R.id.emptyText);

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







        // Retrieve trashed items from intent
        trashedWaterHistoryList = (List<WaterhistoryModel>) getIntent().getSerializableExtra("trashed_items");

        // If the list is null, create an empty list to prevent crashes
        if (trashedWaterHistoryList == null) {
            trashedWaterHistoryList = new ArrayList<>();
        }

        // Initialize RecyclerView
        trashedWaterRecyclerView = findViewById(R.id.trashedwaterRecyclerView);
        trashedWaterRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set adapter with trashed items
        adapter = new WateringhistoryAdapter(this, trashedWaterHistoryList, null);
        trashedWaterRecyclerView.setAdapter(adapter);

        checkIfTrashIsEmpty();

        ImageButton backButton = findViewById(R.id.back2water);
        backButton.setOnClickListener(v -> finish());
    }

    private void checkIfTrashIsEmpty() {
        if (trashedWaterHistoryList.isEmpty()) {
            emptyTrashIcon.setVisibility(View.VISIBLE);
            emptyTrashText.setVisibility(View.VISIBLE);
            trashedWaterRecyclerView.setVisibility(View.GONE);
        } else {
            emptyTrashIcon.setVisibility(View.GONE);
            emptyTrashText.setVisibility(View.GONE);
            trashedWaterRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}

