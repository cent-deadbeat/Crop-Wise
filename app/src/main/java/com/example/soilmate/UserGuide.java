package com.example.soilmate;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserGuide extends AppCompatActivity {

    private DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);

        ImageButton plantTypesButton = findViewById(R.id.plantTypesButton);
        plantTypesButton.setOnClickListener(v -> {
            startActivity(new Intent(UserGuide.this, PlantTypesGuideActivity.class));
        });


        // Sidebarmenu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        ImageButton menuButton = findViewById(R.id.menubtn);
        menuButton.setOnClickListener(view -> drawerLayout.openDrawer(navigationView));

        Menu menu = navigationView.getMenu();


        // Apply padding programmatically to create space above logout
        menu.add(Menu.NONE, Menu.NONE, 100, "").setEnabled(false);




        //menu itemnav
        // Handling NavigationView menu item clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.nav_plants:
                        startActivity(new Intent(UserGuide.this,MyPlants.class));
                        break;
                    case R.id.nav_my_acc:
                        startActivity(new Intent(UserGuide.this, MyAccount.class));
                        break;
                    case R.id.nav_guide:
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_help:
                        startActivity(new Intent(UserGuide.this, Help.class));
                        break;
                    case R.id.nav_logout:
                        startActivity(new Intent(UserGuide.this, Login.class));
                        finish();
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        updateSidebarInfo();
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