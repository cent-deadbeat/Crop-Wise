package com.example.soilmate;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TrashedPlants extends AppCompatActivity {
    private RecyclerView trashedPlantsRecyclerView;
    private ImageView emptyTrashIcon;
    private TextView emptyTrashText;
    private TrashedPlantAdapter adapter;
    private List<PlantModel> trashedPlantList = new ArrayList<>();
    private List<PlantModel> originalTrashedPlantList = new ArrayList<>(); // Keep original list



    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trashed_plants);

        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        trashedPlantsRecyclerView = findViewById(R.id.trashedplantsRecyclerView);
        emptyTrashIcon = findViewById(R.id.emptyIcon);
        emptyTrashText = findViewById(R.id.emptyText);

        // Setup RecyclerView
        trashedPlantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TrashedPlantAdapter(this, trashedPlantList);
        trashedPlantsRecyclerView.setAdapter(adapter);

        // Fetch trashed plants from Firestore
        fetchTrashedPlants();

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

        // Initialize back button
        ImageButton backButton = findViewById(R.id.back2plants);
        backButton.setOnClickListener(v -> finish());

        // Initialize delete permanently button
        ImageButton deletePermanentlyButton = findViewById(R.id.deletePermanently);
        deletePermanentlyButton.setOnClickListener(v -> showPermanentDeleteConfirmation());


        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchTrashedPlants(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchTrashedPlants(newText);  // Dynamically update results
                return true;
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        fetchTrashedPlants();
    }

    @Override
    public void finish() {
        super.finish();
    }

    private void fetchTrashedPlants() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("FirestoreDebug", "Fetching trashed plants for user: " + userId);

        firestore.collection("trash")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(value -> {
                    originalTrashedPlantList.clear();
                    trashedPlantList.clear();

                    if (!value.isEmpty()) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            PlantModel plant = document.toObject(PlantModel.class);
                            if (plant != null) {
                                plant.setId(document.getId());
                                originalTrashedPlantList.add(plant);
                            }
                        }
                    } else {
                        Log.w("FirestoreDebug", "No trashed plants found for user: " + userId);
                    }

                    trashedPlantList.addAll(originalTrashedPlantList); // ✅ Restore full list
                    adapter.updateList(new ArrayList<>(trashedPlantList)); // ✅ Ensure adapter updates
                    checkIfTrashIsEmpty();
                })
                .addOnFailureListener(error -> Log.e("FirestoreDebug", "Error fetching trashed plants: " + error.getMessage()));
    }






    private void searchTrashedPlants(String query) {
        if (query.isEmpty()) {
            adapter.updateList(new ArrayList<>(originalTrashedPlantList)); // ✅ Restore full list
        } else {
            List<PlantModel> filteredList = new ArrayList<>();
            for (PlantModel plant : originalTrashedPlantList) {
                if (plant.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(plant);
                }
            }
            adapter.updateList(filteredList);
        }

        checkIfTrashIsEmpty(); // ✅ Show empty message if no results
    }





    private void showPermanentDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Permanent Deletion")
                .setMessage("Are you sure you want to empty trash? All items will be permanently deleted.")
                .setPositiveButton("Delete Permanently", (dialog, which) -> deletePlantsPermanently())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePlantsPermanently() {
        for (PlantModel plant : trashedPlantList) {
            firestore.collection("trash").document(plant.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirestoreDebug", "Plant deleted permanently: " + plant.getId());
                        trashedPlantList.remove(plant);
                        adapter.notifyDataSetChanged();
                        checkIfTrashIsEmpty();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreDebug", "Failed to delete plant permanently: " + e.getMessage());
                    });
        }
    }

    private void checkIfTrashIsEmpty() {
        if (trashedPlantList.isEmpty()) {
            emptyTrashIcon.setVisibility(View.VISIBLE);
            emptyTrashText.setVisibility(View.VISIBLE);
            trashedPlantsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyTrashIcon.setVisibility(View.GONE);
            emptyTrashText.setVisibility(View.GONE);
            trashedPlantsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}
