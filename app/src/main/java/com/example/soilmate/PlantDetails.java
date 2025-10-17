package com.example.soilmate;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.chip.Chip;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
public class PlantDetails extends AppCompatActivity {

    private ImageView plantImage;
    private TextView plantName, soilMoistureText, humidityText, phLevelText, waterLevelText, nitrogenText, phosphorusText, potassiumText, minSoilMoistureLevel;
    private CircularProgressIndicator progSoil, progHum, progPh, progWater;
    private FlexboxLayout scheduleContainer;

    private ConstraintLayout scheduleSection;
    private AppCompatButton viewAllButton;
    private FirebaseFirestore databaseReference;
    private Uri selectedImageUri;
    private ProgressBar progressBar;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ImageButton editButton, backButton, changeImgButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_details);



        changeImgButton = findViewById(R.id.change_img);
        changeImgButton.setOnClickListener(v -> openImagePicker());

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);



        // Initialize UI components
        plantImage = findViewById(R.id.plant_img);
        plantName = findViewById(R.id.plant_name);
        soilMoistureText = findViewById(R.id.soil_moisture);
        humidityText = findViewById(R.id.humid);
        phLevelText = findViewById(R.id.ph_level);
        waterLevelText = findViewById(R.id.water_level);
        nitrogenText = findViewById(R.id.nitrogen);
        phosphorusText = findViewById(R.id.phosphorus);
        potassiumText = findViewById(R.id.potassium);
        minSoilMoistureLevel = findViewById(R.id.minMoistureLevel);

        progSoil = findViewById(R.id.progsoil);
        progHum = findViewById(R.id.proghum);
        progPh = findViewById(R.id.progph);
        progWater = findViewById(R.id.progwat);

        scheduleContainer = findViewById(R.id.scheduleContainer);
        scheduleSection = findViewById(R.id.scheduleSection);
        editButton = findViewById(R.id.editplant);
        backButton = findViewById(R.id.backbtn);
        viewAllButton = findViewById(R.id.viewall);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        // Back button
        backButton.setOnClickListener(v -> finish());

        // View all button
        // In PlantDetails.java
        viewAllButton.setOnClickListener(v -> {
            Intent historyIntent = new Intent(PlantDetails.this, Watering_History.class);
            startActivity(historyIntent);
        });

        // Get plant data from intent
        Intent intent = getIntent();
        if (intent != null) {
            plantName.setText(intent.getStringExtra("plantName"));
            plantImage.setImageResource(intent.getIntExtra("imageResId", 0));

            // Display the minSoilMoistureLevel
            minSoilMoistureLevel.setText(intent.getStringExtra("minSoilMoisture"));

            // Set other values
            int soilMoisture = intent.getIntExtra("soilMoisture", 0);
            int humidity = intent.getIntExtra("humidity", 0);
            float phLevel = intent.getFloatExtra("phLevel", 0.0f);
            int waterLevel = intent.getIntExtra("waterLevel", 0);
            int nitrogen = intent.getIntExtra("nitrogen", 0);
            int phosphorus = intent.getIntExtra("phosphorus", 0);
            int potassium = intent.getIntExtra("potassium", 0);

            // Update text values
            soilMoistureText.setText(soilMoisture + "%");
            humidityText.setText(humidity + "°C");
            phLevelText.setText(String.valueOf(phLevel));
            waterLevelText.setText(waterLevel + " µS/cm");
            nitrogenText.setText(nitrogen + " mg/kg");
            phosphorusText.setText(phosphorus + " mg/kg");
            potassiumText.setText(potassium + " mg/kg");

            // Set progress bars max values and update progress
            progSoil.setMax(100);
            progHum.setMax(100);
            progPh.setMax(14);
            progWater.setMax(5000);

            progSoil.setProgress(soilMoisture);
            progHum.setProgress(humidity);
            progPh.setProgress((int) phLevel);
            progWater.setProgress(waterLevel);


            // Get watering schedules from intent
            ArrayList<String> schedules = intent.getStringArrayListExtra("wateringSchedules");
            if (schedules != null && !schedules.isEmpty()) {
                Log.d("PlantDetails", "Watering schedules: " + schedules);
                scheduleSection.setVisibility(View.VISIBLE); // Make the schedule container visible
                populateSchedules(schedules);
            } else {
                scheduleSection.setVisibility(View.GONE); // Hide the schedule container if no schedules
            }



        }

        // Edit Plant Button
        editButton.setOnClickListener(v -> {
            String plantId = getIntent().getStringExtra("plantId"); // Get plantId from intent

            if (plantId != null) {
                EditPlantPopUp editDialog = new EditPlantPopUp();

                // Pass plant details
                Bundle args = new Bundle();
                args.putString("plantId", plantId);
                args.putString("plantName", plantName.getText().toString());
                args.putString("plantImage", "android.resource://" + getPackageName() + "/" + intent.getIntExtra("imageResId", R.drawable.img));
                args.putString("minSoilMoisture", intent.getStringExtra("minSoilMoisture"));

                editDialog.setArguments(args);
                editDialog.show(getSupportFragmentManager(), "EditPlantPopUp");
            } else {
                Toast.makeText(PlantDetails.this, "Error: Plant ID not found", Toast.LENGTH_SHORT).show();
            }
        });


        String plantId = getIntent().getStringExtra("plantId");
        if (plantId != null) {
            fetchActualImageFromFirestore(plantId);
        }

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        plantImage.setImageURI(selectedImageUri); // Show image in ImageView
                        Log.d("FirebaseUpload", "✅ Image selected: " + uri.toString());

                        // Automatically upload after selection
                        uploadImageToFirebase();
                    } else {
                        Log.e("FirebaseUpload", "❌ Image selection failed or canceled");
                    }
                });




        changeImgButton.setOnClickListener(v -> {
            openImagePicker();
        });


    }




    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void uploadImageToFirebase() {
        if (selectedImageUri == null) {
            Log.e("FirebaseUpload", "❌ No image selected for upload");
            return;
        }

        progressBar.setVisibility(View.VISIBLE); // Show progress bar when upload starts

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String plantId = getIntent().getStringExtra("plantId");

        if (plantId == null || plantId.isEmpty()) {
            Log.e("FirebaseUpload", "❌ Plant ID is null or empty. Cannot upload image.");
            progressBar.setVisibility(View.GONE);
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("plant_images/" + userId + "/" + plantId);

        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("FirebaseUpload", "✅ Image uploaded successfully: " + uri.toString());
                    updateFirestoreWithImage(uri.toString(), plantId);

                    // Load image into ShapeableImageView with Glide
                    Glide.with(this)
                            .load(uri.toString())
                            .centerCrop()
                            .into(plantImage);

                    progressBar.setVisibility(View.GONE);
                }))
                .addOnFailureListener(e -> {
                    Log.e("FirebaseUpload", "❌ Image upload failed: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
    }






    private void updateFirestoreWithImage(String imageUrl, String plantId) {
        FirebaseFirestore.getInstance().collection("plants").document(plantId)
                .update("actualImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "✅ Image updated successfully");

                    // Fetch and display immediately
                    fetchActualImageFromFirestore(plantId);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "❌ Failed to update image: " + e.getMessage()));
    }




    private void fetchActualImageFromFirestore(String plantId) {
        FirebaseFirestore.getInstance().collection("plants").document(plantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("actualImageUrl")) {
                        String imageUrl = documentSnapshot.getString("actualImageUrl");
                        Log.d("Firestore", "📸 Retrieved actualImage: " + imageUrl);
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .centerCrop()
                                    .into(plantImage);
                        }
                    } else {
                        Log.e("Firestore", "⚠️ No actual image found for this plant");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "❌ Failed to fetch actual image: " + e.getMessage()));
    }






    // Populate schedule chips
    private void populateSchedules(ArrayList<String> schedules) {
        scheduleContainer.removeAllViews();  // Remove any existing views

        // Iterate over the schedules and create a chip for each one
        for (String schedule : schedules) {
            Log.d("PlantDetails", "Adding schedule: " + schedule);  // Debug log for each schedule

            // Create a Chip for each schedule
            Chip chip = new Chip(this);
            chip.setText(schedule);
            chip.setChipBackgroundColorResource(R.color.lg); // Use the correct color for the chip background
            chip.setTextColor(getResources().getColor(R.color.dg)); // Set the text color
            chip.setTextSize(14);
            chip.setCloseIconVisible(false); // Set close icon as invisible (if you don't want it visible)

            // Add margins programmatically
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 3, 10, 0); // Set margins for spacing
            chip.setLayoutParams(params);

            // Add the chip to the FlexboxLayout container
            scheduleContainer.addView(chip);
        }
    }




}
