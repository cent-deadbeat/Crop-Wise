package com.example.soilmate;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyPlants extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private RecyclerView plantRecyclerView;
    private ImageView emptyIcon;
    private TextView emptyText;
    private PlantAdapter adapter;
    private List<PlantModel> plantList = new ArrayList<>();
    private List<PlantModel> trashedPlantList = new ArrayList<>();
    private List<PlantModel> originalPlantList = new ArrayList<>();
    private ImageButton trashPlants, removeButton, cancelDeleteButton, addPlantButton;
    private static final int TRASHED_PLANTS_REQUEST_CODE = 1;
    private boolean isSelectionMode = false;
    private FirebaseFirestore databaseReference;
    private FirebaseAuth auth;

    @Override

    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Do you really want to exit the app?")
                .setPositiveButton("Yes", (dialog, which) -> finishAffinity()) // Exits the app
                .setNegativeButton("No", null) // Dismisses the dialog
                .show();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_plants);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Check if the user is logged in and their email is verified
        if (user == null || !user.isEmailVerified()) {
            Log.e("MyPlantsDebug", "User not authenticated or email not verified! Redirecting to login.");
            startActivity(new Intent(MyPlants.this, Login.class));
            finish();
            return;
        } else {
            Log.d("MyPlantsDebug", "User authenticated and email verified: " + user.getUid());
        }


        databaseReference = FirebaseFirestore.getInstance();
        FirebaseApp.initializeApp(this);

        fetchPlantsFromFirestore();


        emptyIcon = findViewById(R.id.emptyIcon);
        emptyText = findViewById(R.id.emptyText);


        // Sidebar menu setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        ImageButton menuButton = findViewById(R.id.menubtn);
        menuButton.setOnClickListener(view -> drawerLayout.openDrawer(navigationView));

        // Navigation menu setup
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_my_acc:
                    startActivity(new Intent(MyPlants.this, MyAccount.class));
                    break;
                case R.id.nav_guide:
                    startActivity(new Intent(MyPlants.this, UserGuide.class));
                    break;
                case R.id.nav_help:
                    startActivity(new Intent(MyPlants.this, Help.class));
                    break;
                case R.id.nav_logout:
                    showLogoutConfirmation();
                    break;
            }
            drawerLayout.closeDrawers();
            return true;
        });

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


        // Buttons setup
        trashPlants = findViewById(R.id.trash_plant);
        removeButton = findViewById(R.id.remove_plant);
        cancelDeleteButton = findViewById(R.id.cancel_delete_plant);
        addPlantButton = findViewById(R.id.addplant);

        removeButton.setVisibility(View.GONE);
        cancelDeleteButton.setVisibility(View.GONE);

        // RecyclerView setup
        plantRecyclerView = findViewById(R.id.plantsRecyclerView);
        plantRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PlantAdapter(this, plantList, this);
        plantRecyclerView.setAdapter(adapter);

        // Fetch plant data from Firebase


        // Button click listeners
        trashPlants.setOnClickListener(v -> openTrashPlants());
        removeButton.setOnClickListener(v -> showDeleteConfirmation());
        cancelDeleteButton.setOnClickListener(v -> exitSelectionMode());

        addPlantButton.setOnClickListener(v -> {
            AddPlantPopUp addPlantPopUp = new AddPlantPopUp();
            addPlantPopUp.show(getSupportFragmentManager(), "AddPlantPopUp");
        });



        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPlants(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchPlants(newText);  // Dynamically update results
                return true;
            }
        });





        checkIfEmpty();
        updateSidebarInfo();

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPlantsFromFirestore();
    }

    private void fetchPlantsFromFirestore() {
        String userId = auth.getCurrentUser().getUid();
        Log.d("FirestoreDebug", "Fetching plants for user: " + userId);

        databaseReference.collection("plants")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("FirestoreDebug", "Firestore error: " + error.getMessage());
                        return;
                    }

                    originalPlantList.clear();  // Ensure no duplicates
                    plantList.clear();

                    if (value != null) {
                        for (DocumentSnapshot document : value.getDocuments()) {
                            PlantModel plant = document.toObject(PlantModel.class);
                            if (plant != null) {
                                plant.setId(document.getId());
                                originalPlantList.add(plant);
                            }
                        }
                    } else {
                        Log.w("FirestoreDebug", "No plants found for user: " + userId);
                    }

                    // ✅ Ensure plants are displayed when opening the app
                    adapter.updateList(new ArrayList<>(originalPlantList));

                    checkIfEmpty();
                });
    }






    private void searchPlants(String query) {
        if (query.isEmpty()) {
            // ✅ Restore full list when search is cleared
            adapter.updateList(new ArrayList<>(originalPlantList));
        } else {
            List<PlantModel> filteredList = new ArrayList<>();
            for (PlantModel plant : originalPlantList) {
                if (plant.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(plant);
                }
            }
            adapter.updateList(filteredList);
        }

        // ✅ Update UI if no results found
        checkIfEmpty();
    }









    private void openTrashPlants() {
        Intent intent = new Intent(this, TrashedPlants.class);
        startActivityForResult(intent, TRASHED_PLANTS_REQUEST_CODE);
    }

    private void showDeleteConfirmation() {
        int count = adapter.getSelectedItems().size();
        if (count == 0) return;

        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Would you like to move the selected " + count + " plant(s) to trash?")
                .setPositiveButton("Move to Trash", (dialog, which) -> moveToTrash())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void moveToTrash() {
        Set<Integer> selectedItems = adapter.getSelectedItems();
        if (selectedItems.isEmpty()) return;

        // Create a list to store the indices of the plants to be removed
        List<Integer> indicesToRemove = new ArrayList<>(selectedItems);

        // Iterate over the selected items in reverse order to avoid index issues
        for (int i = indicesToRemove.size() - 1; i >= 0; i--) {
            int index = indicesToRemove.get(i);
            if (index < 0 || index >= plantList.size()) {
                Log.e("MoveToTrashError", "Invalid index: " + index);
                continue; // Skip invalid indices
            }

            PlantModel plant = plantList.get(index);

            // Add the userId field to the plant object
            plant.setUserId(auth.getCurrentUser().getUid());

            // Move plant to trash collection
            databaseReference.collection("trash").document(plant.getId())
                    .set(plant)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirestoreDebug", "Plant moved to trash: " + plant.getId());

                        // Remove plant from the main collection
                        databaseReference.collection("plants").document(plant.getId())
                                .delete()
                                .addOnSuccessListener(aVoid1 -> {
                                    Log.d("FirestoreDebug", "Plant deleted from main collection: " + plant.getId());

                                    // Remove the plant from the list and notify the adapter
                                    adapter.notifyItemRemoved(index);

                                    // Show a toast message
                                    Toast.makeText(MyPlants.this, "Plant moved to trash", Toast.LENGTH_SHORT).show();

                                    // Check if the list is empty and update the UI
                                    checkIfEmpty();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirestoreDebug", "Failed to delete plant from main collection: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirestoreDebug", "Failed to move plant to trash: " + e.getMessage());
                    });
        }

        // Exit selection mode after moving plants to trash
        adapter.setSelectionMode(false);
        toggleSelectionMode(false);
    }

    public void toggleSelectionMode(boolean enabled) {
        isSelectionMode = enabled;
        trashPlants.setVisibility(enabled ? View.GONE : View.VISIBLE);
        removeButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
        cancelDeleteButton.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private void exitSelectionMode() {
        adapter.setSelectionMode(false);
        toggleSelectionMode(false);
    }

    private void checkIfEmpty() {
        if (plantList.isEmpty()) {
            emptyIcon.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.VISIBLE);
            plantRecyclerView.setVisibility(View.GONE);
        } else {
            emptyIcon.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
            plantRecyclerView.setVisibility(View.VISIBLE);
        }
    }





    //logout
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> logoutUser())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();  // Logs out from Firebase Authentication
        GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut();

        // Clear SharedPreferences (if you're using login persistence)
        SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        //  screen
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  // Clears activity stack
        startActivity(intent);
        finish();
    }


    private void updateSidebarInfo() {
        NavigationView navigationView = findViewById(R.id.navigationView);
        View headerView = navigationView.getHeaderView(0);

        TextView headerUsername = headerView.findViewById(R.id.header_username);
        TextView headerEmail = headerView.findViewById(R.id.header_email);
        TextView headerPlantNum = headerView.findViewById(R.id.header_plant_num);
        ImageView headerProfileImage = headerView.findViewById(R.id.profileImage);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // ✅ Set email from Firebase Auth
        headerEmail.setText(user.getEmail());

        // ✅ Reload user to get updated profile info
        user.reload().addOnSuccessListener(aVoid -> {
            // Load profile image from Firebase Authentication
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.profile) // Placeholder while loading
                        .error(R.drawable.profile) // Default if loading fails
                        .into(headerProfileImage);
            } else {
                headerProfileImage.setImageResource(R.drawable.profile);
            }
        });

        // ✅ Fetch username and plant count from Firestore
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get username
                        String username = documentSnapshot.getString("username");
                        headerUsername.setText(username != null ? username : "No username");

                        // Fetch the number of plants created by the user
                        FirebaseFirestore.getInstance().collection("plants")
                                .whereEqualTo("userId", user.getUid()) // Filter plants by logged-in user
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    int plantCount = queryDocumentSnapshots.size();
                                    headerPlantNum.setText(String.valueOf(plantCount)); // Update plant count
                                })
                                .addOnFailureListener(e -> headerPlantNum.setText("0"));
                    }
                })
                .addOnFailureListener(e -> {
                    headerUsername.setText("Error loading username");
                    headerPlantNum.setText("0");
                });
    }

}
