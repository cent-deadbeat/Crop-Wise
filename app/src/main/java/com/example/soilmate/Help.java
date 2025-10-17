package com.example.soilmate;

import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Help extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private RecyclerView helpRecyclerView;
    private FAQAdapter faqAdapter;
    private List<FAQModel> faqList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        setupSidebarMenu();
        setupSearchBar();

        helpRecyclerView = findViewById(R.id.helpRecyclerView);
        helpRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        faqList = new ArrayList<>();

        // 1. GETTING STARTED
        faqList.add(new FAQModel("Getting Started"));
        faqList.add(new FAQModel("What is SoilMate?",
                "### Product Overview\n\n" +
                        "SoilMate is an intelligent plant care system that combines precision hardware with smart software:\n\n" +
                        "**Core Features**:\n" +
                        "- Real-time soil monitoring (moisture, pH, NPK levels)\n" +
                        "- Weather-adaptive watering schedules\n" +
                        "- Plant health analytics dashboard\n" +
                        "- Species-specific care plans \n\n" +
                        "**Technical Specifications**:\n" +
                        "- Measurement accuracy: ±2% for moisture\n" +
                        "- Wireless range: 30m (open air)\n" +
                        "- Battery life: 6 months (sensor)\n" +
                        "- App compatibility: Android 8+/iOS 12+\n\n",
                "Getting Started"));

        faqList.add(new FAQModel("Initial Setup Guide",
                "### Step-by-Step Installation\n\n" +
                        "**Hardware Installation**:\n" +
                        "1. Unpack components:\n" +
                        "   - Soil sensor (IP67 waterproof)\n" +
                        "   - Smart water pump (5V DC)\n" +
                        "   - 4mm irrigation tubing (3m included)\n" +
                        "2. Sensor placement:\n" +
                        "   - Insert vertically 2-3 inches (5-7cm) near root zone\n" +
                        "   - Avoid direct sunlight on sensor body\n" +
                        "3. Pump connection:\n" +
                        "   - Submerge intake in water reservoir\n" +
                        "   - Secure tubing connections\n\n" +
                        "**Software Setup**:\n" +
                        "1. Download SoilMate app\n" +
                        "2. Create account (email verification required)\n" +
                        "3. Add device via WiFi pairing\n" +
                        "4. Complete guided calibration\n\n" +
                        "**Troubleshooting Tip**: If setup fails, restart both app and sensor",
                "Getting Started"));

// 2. TROUBLESHOOTING
        faqList.add(new FAQModel("Troubleshooting"));
        faqList.add(new FAQModel("Watering System Issues",
                "### Diagnostic Procedures\n\n" +
                        "**Symptom**: No water delivery\n\n" +
                        "1. **Power Verification**:\n" +
                        "   - Check pump LED status\n" +
                        "   - Test outlet with another device\n" +
                        "   - Verify 5V power adapter connection\n\n" +
                        "2. **Hydraulic Check**:\n" +
                        "   - Minimum water level: 1/4 reservoir\n" +
                        "   - Inspect tubing for:\n" +
                        "     - Kinks (minimum bend radius 5cm)\n" +
                        "     - Blockages (flush with vinegar solution)\n" +
                        "     - Leaks (check all connectors)\n\n" +
                        "3. **Sensor Validation**:\n" +
                        "   - Confirm moisture readings in app\n" +
                        "   - Recalibrate if readings seem inaccurate\n\n",
                "Troubleshooting"));

        faqList.add(new FAQModel("Sensor Accuracy Problems",
                "### Calibration and Maintenance\n\n" +
                        "**Common Causes of Inaccurate Readings**:\n" +
                        "- Sensor not fully inserted in soil\n" +
                        "- Mineral buildup on probes\n" +
                        "- Extreme temperatures (<0°C or >50°C)\n" +
                        "- Soil type differences (clay vs sandy)\n\n" +
                        "**Calibration Procedure**:\n" +
                        "1. Prepare two reference samples:\n" +
                        "   - Completely dry soil\n" +
                        "   - Saturated soil (water dripping)\n" +
                        "2. Run calibration wizard in app\n" +
                        "3. Allow 24-hour stabilization period\n\n" +
                        "**Maintenance Schedule**:\n" +
                        "- Weekly: Visual inspection\n" +
                        "- Monthly: Deep cleaning\n" +
                        "- Quarterly: Full recalibration",
                "Troubleshooting"));

// 3. PLANT CARE
        faqList.add(new FAQModel("Plant Care"));
        faqList.add(new FAQModel("Plant Health Diagnosis",
                "### Visual Symptom Guide\n\n" +
                        "**Yellow Leaves**:\n" +
                        "1. Overwatering:\n" +
                        "   - Soil constantly wet\n" +
                        "   - Solution: Reduce frequency by 25%\n" +
                        "2. Nutrient Deficiency:\n" +
                        "   - Yellowing between veins\n" +
                        "   - Solution: Apply balanced fertilizer\n\n" +
                        "**Wilting/Drooping**:\n" +
                        "1. Underwatering:\n" +
                        "   - Dry soil 2 inches below surface\n" +
                        "   - Solution: Deep watering session\n" +
                        "2. Root Rot:\n" +
                        "   - Foul odor from soil\n" +
                        "   - Solution: Repot with fresh mix\n\n",
                "Plant Care"));

// 4. MAINTENANCE
        faqList.add(new FAQModel("Maintenance"));
        faqList.add(new FAQModel("System Maintenance",
                "### Preventive Care Schedule\n\n" +
                        "**Weekly Tasks**:\n" +
                        "- Check tubing connections\n" +
                        "- Verify sensor data consistency\n" +
                        "- Inspect for pest activity\n\n" +
                        "**Monthly Tasks**:\n" +
                        "1. Sensor Maintenance:\n" +
                        "   - Clean probes with soft brush\n" +
                        "   - Check for corrosion\n" +
                        "2. Water System:\n" +
                        "   - Flush tubing (1:3 vinegar solution)\n" +
                        "   - Clean pump filter\n\n" +
                        "**Quarterly Tasks**:\n" +
                        "- Full system diagnostic\n" +
                        "- Battery replacement check\n" +
                        "- Firmware update verification\n\n" +
                        "**Maintenance Log**:\n" +
                        "Record all service activities in app",
                "Maintenance"));
        faqAdapter = new FAQAdapter(faqList);
        helpRecyclerView.setAdapter(faqAdapter);
        updateSidebarInfo();
    }

    private void setupSidebarMenu() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("");

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        ImageButton menuButton = findViewById(R.id.menubtn);
        menuButton.setOnClickListener(view -> drawerLayout.openDrawer(navigationView));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_plants) startActivity(new Intent(this, MyPlants.class));
            else if (id == R.id.nav_my_acc) startActivity(new Intent(this, MyAccount.class));
            else if (id == R.id.nav_guide) startActivity(new Intent(this, UserGuide.class));
            else if (id == R.id.nav_logout) {
                startActivity(new Intent(this, Login.class));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });
    }

    private void setupSearchBar() {
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchbar);
        TextView searchHint = findViewById(R.id.searchHint);

        // Make the entire search area clickable
        View searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_plate);
        searchPlate.setOnClickListener(v -> {
            searchView.setIconified(false);
            searchView.requestFocus();
        });

        searchView.setOnSearchClickListener(v -> searchHint.setVisibility(View.GONE));
        searchView.setOnCloseListener(() -> {
            searchHint.setVisibility(View.VISIBLE);
            return false;
        });

        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchEditText.setHint("Search questions...");
        searchView.setOnClickListener(v -> searchView.setIconified(false));

        searchEditText.setHintTextColor(getResources().getColor(R.color.gray));
        searchEditText.setTextColor(getResources().getColor(R.color.black));
        searchEditText.setTypeface(ResourcesCompat.getFont(this, R.font.poppinsregular));

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                faqAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String query = newText.toLowerCase();

                // Expanded quick tips
                if (query.contains("water") || query.contains("dry")) {
                    showQuickTips(Arrays.asList(
                            "➊ Check water reservoir level",
                            "➋ Inspect tubing for kinks",
                            "➌ Verify pump power connection",
                            "➍ Check for clogged filters"
                    ));
                }
                else if (query.contains("sensor") || query.contains("reading")) {
                    showQuickTips(Arrays.asList(
                            "➊ Reinsert sensor firmly",
                            "➋ Clean sensor contacts",
                            "➌ Re-pair Bluetooth",
                            "➍ Check for moisture damage"
                    ));
                }
                else if (query.contains("yellow") || query.contains("leaf")) {
                    showQuickTips(Arrays.asList(
                            "➊ Check watering frequency",
                            "➋ Test soil nutrients",
                            "➌ Inspect for pests",
                            "➍ Adjust sunlight exposure"
                    ));
                }
                else if (query.contains("calibrat")) {
                    showQuickTips(Arrays.asList(
                            "➊ Use distilled water for calibration",
                            "➋ Ensure sensor is clean",
                            "➌ Follow app instructions precisely"
                    ));
                }

                faqAdapter.getFilter().filter(newText);
                return false;
            }
        });

        searchView.clearFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
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

        headerEmail.setText(user.getEmail());

        user.reload().addOnSuccessListener(aVoid -> {
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(headerProfileImage);
            } else {
                headerProfileImage.setImageResource(R.drawable.profile);
            }
        });

        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        headerUsername.setText(username != null ? username : "No username");

                        FirebaseFirestore.getInstance().collection("plants")
                                .whereEqualTo("userId", user.getUid())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    int plantCount = queryDocumentSnapshots.size();
                                    headerPlantNum.setText(String.valueOf(plantCount));
                                })
                                .addOnFailureListener(e -> headerPlantNum.setText("0"));
                    }
                })
                .addOnFailureListener(e -> {
                    headerUsername.setText("Error loading username");
                    headerPlantNum.setText("0");
                });
    }

    private void showQuickTips(List<String> tips) {
        // Inflate custom layout
        View quickTipsView = LayoutInflater.from(this).inflate(R.layout.quick_tips, null);

        RecyclerView tipsRecycler = quickTipsView.findViewById(R.id.tipsRecyclerView);
        tipsRecycler.setLayoutManager(new LinearLayoutManager(this));
        tipsRecycler.setAdapter(new QuickTipsAdapter(tips));

        // Show as bottom sheet
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(quickTipsView);
        dialog.show();
    }
}