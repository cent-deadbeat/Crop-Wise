package com.example.soilmate;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EditPlantPopUp extends DialogFragment {

    private LinearLayout waterScheduling, soilMoisture;
    private Button schedulingBtn, addPlantBtn;
    private FlexboxLayout schedulePreviewLayout;  // Updated to FlexboxLayout
    private TextView schedulePreview, textView27, scheduleError; // Added scheduleError
    private List<String> wateringSchedules = new ArrayList<>();
    private ImageButton buttonCancel;
    private TextInputEditText inputPlantName, moistureInput;
    private TextInputLayout plantNameLayout, moistureInputLayout;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private ImageView imagePreview;

    private String plantId;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Inflate the custom layout
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_edit_plant_pop_up, null);

        view.setBackgroundResource(R.drawable.adding_plant_bg);


        // Retrieve Plant Data
        Bundle args = getArguments();
        if (args != null) {
            plantId = args.getString("plantId"); // 🔹 Get Plant ID
            fetchPlantDetails(plantId); // 🔹 Fetch from Firestore
        }

        // Initialize views

        buttonCancel = view.findViewById(R.id.close);
        waterScheduling = view.findViewById(R.id.water_scheduling);
        soilMoisture = view.findViewById(R.id.soil_moisture);
        schedulingBtn = view.findViewById(R.id.scheduling_btn);
        schedulePreviewLayout = view.findViewById(R.id.schedule_preview_layout);
        schedulePreview = view.findViewById(R.id.schedule_preview);
        textView27 = view.findViewById(R.id.textView27);
        scheduleError = view.findViewById(R.id.schedule_error);
        imagePreview = view.findViewById(R.id.image_preview);

        imagePreview.setOnClickListener(v -> openImagePicker());

        // Initially hide scheduling button and schedule preview
        schedulingBtn.setVisibility(View.GONE);
        schedulePreviewLayout.setVisibility(View.GONE);
        textView27.setVisibility(View.GONE);
        scheduleError.setVisibility(View.GONE);

        // Click listener for Water Scheduling
        waterScheduling.setOnClickListener(v -> {
            toggleMode(waterScheduling, soilMoisture);
            schedulingBtn.setVisibility(View.VISIBLE);
            schedulePreviewLayout.setVisibility(View.VISIBLE);
            textView27.setVisibility(View.VISIBLE);

            // Hide the mode error immediately
            TextView modeError = getDialog().findViewById(R.id.mode_error);
            modeError.setVisibility(View.GONE);
        });

        // Click listener for Soil Moisture
        soilMoisture.setOnClickListener(v -> {
            toggleMode(soilMoisture, waterScheduling);
            schedulingBtn.setVisibility(View.GONE);
            schedulePreviewLayout.setVisibility(View.GONE);
            textView27.setVisibility(View.GONE);

            // Hide the mode error immediately
            TextView modeError = getDialog().findViewById(R.id.mode_error);
            modeError.setVisibility(View.GONE);
        });

        // Open TimePickerDialog when scheduling button is clicked
        schedulingBtn.setOnClickListener(v -> showTimePickerDialog());

        // Handle Cancel Button
        buttonCancel.setOnClickListener(v -> dismiss());



        // Create the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);


        Dialog dialog = builder.create();

        // Set Transparent Background to Enable Rounded Corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        moistureInputLayout = view.findViewById(R.id.moisture_input_layout);
        moistureInput = view.findViewById(R.id.moisture_input);

        waterScheduling.setOnClickListener(v -> {
            toggleMode(waterScheduling, soilMoisture);

            // Show scheduling UI and hide soil moisture input
            schedulingBtn.setVisibility(View.VISIBLE);
            schedulePreviewLayout.setVisibility(View.VISIBLE);
            textView27.setVisibility(View.VISIBLE);
            moistureInputLayout.setVisibility(View.GONE);

            // Hide the mode error immediately
            TextView modeError = getDialog().findViewById(R.id.mode_error);
            modeError.setVisibility(View.GONE);
        });

        soilMoisture.setOnClickListener(v -> {
            toggleMode(soilMoisture, waterScheduling);

            // Hide scheduling UI and show soil moisture input
            schedulingBtn.setVisibility(View.GONE);
            schedulePreviewLayout.setVisibility(View.GONE);
            textView27.setVisibility(View.GONE);
            moistureInputLayout.setVisibility(View.VISIBLE);

            // Hide the mode error immediately
            TextView modeError = getDialog().findViewById(R.id.mode_error);
            modeError.setVisibility(View.GONE);

            scheduleError.setVisibility(View.GONE);
        });

        inputPlantName = view.findViewById(R.id.inputplantName);
        plantNameLayout = view.findViewById(R.id.plant_name_layout);
        addPlantBtn = view.findViewById(R.id.addplant_btn);


        // Set input filters
        inputPlantName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(35)});
        moistureInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        // Add validation on text change
        inputPlantName.addTextChangedListener(new ValidationWatcher(inputPlantName));
        moistureInput.addTextChangedListener(new ValidationWatcher(moistureInput));

        // Click listener for "Add Plant"
        addPlantBtn.setOnClickListener(v -> validateInputs());

//        // ✅ Retrieve Passed Data from `PlantDetails`
//        Bundle args = getArguments();
//        if (args != null) {
//            // ✅ Set Plant Name in Input Field (Allow Editing)
//            String plantName = args.getString("plantName", "Unknown Plant");
//            inputPlantName.setText(plantName);
//
//            // ✅ Set Moisture Level in Input Field (Allow Editing)
//            String moistureLevel = args.getString("moistureLevel", "");
//            moistureInput.setText(moistureLevel);
//
//            // ✅ Handle Image (Supports Both URI & Drawable Resource)
//            String imageUri = args.getString("plantImage");
//            if (imageUri != null && !imageUri.isEmpty()) {
//                selectedImageUri = Uri.parse(imageUri);
//                imagePreview.setImageURI(selectedImageUri);
//            } else {
//                imagePreview.setImageResource(R.drawable.img); // Default image if no image is set
//            }
//
//            //Retrieve Mode ("schedule" or "moisture") and Set UI
//            String mode = args.getString("mode", "schedule");
//            if (mode.equals("schedule")) {
//                waterScheduling.setBackgroundResource(R.drawable.active_mode_bg);
//                soilMoisture.setBackgroundResource(R.drawable.inactive_mode_bg);
//                schedulingBtn.setVisibility(View.VISIBLE);
//                schedulePreviewLayout.setVisibility(View.VISIBLE);
//                textView27.setVisibility(View.VISIBLE);
//                moistureInputLayout.setVisibility(View.GONE);
//
//                //Retrieve and Show Watering Schedules
//                wateringSchedules = args.getStringArrayList("wateringSchedules");
//                if (wateringSchedules == null || wateringSchedules.isEmpty()) {
//                    wateringSchedules = new ArrayList<>();
//                }
//                updateSchedulePreview(); // Display schedules
//            } else {
//                soilMoisture.setBackgroundResource(R.drawable.active_mode_bg);
//                waterScheduling.setBackgroundResource(R.drawable.inactive_mode_bg);
//                schedulingBtn.setVisibility(View.GONE);
//                schedulePreviewLayout.setVisibility(View.GONE);
//                textView27.setVisibility(View.GONE);
//                moistureInputLayout.setVisibility(View.VISIBLE);
//            }
//        }


        return dialog;
    }

    // Toggle Active/Inactive Mode
    private void toggleMode(LinearLayout active, LinearLayout inactive) {
        Drawable activeBg = getResources().getDrawable(R.drawable.active_mode_bg);
        Drawable inactiveBg = getResources().getDrawable(R.drawable.inactive_mode_bg);
        active.setBackground(activeBg);
        inactive.setBackground(inactiveBg);
    }


    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {

                    String amPm;
                    int hour;

                    if (hourOfDay >= 12) {
                        amPm = "PM";
                        hour = (hourOfDay == 12) ? 12 : hourOfDay - 12;
                    } else {
                        amPm = "AM";
                        hour = (hourOfDay == 0) ? 12 : hourOfDay;
                    }

                    // Format time
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm);

                    // Check for duplicates
                    if (wateringSchedules.contains(formattedTime)) {
                        scheduleError.setVisibility(View.VISIBLE);
                        scheduleError.setText("That schedule already exists!");
                    } else {
                        scheduleError.setVisibility(View.GONE);
                        wateringSchedules.add(formattedTime);
                        updateSchedulePreview();
                    }
                },
                18, 0, false
        );
        timePickerDialog.show();
    }


    // Update the preview of selected schedules
    private void updateSchedulePreview() {
        schedulePreviewLayout.removeAllViews(); // Clear previous views

        for (String time : wateringSchedules) {
            // Create a horizontal LinearLayout to hold time and delete button
            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(5, 5, 5, 5);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL); // Ensure alignment

            // Time TextView
            TextView timeText = new TextView(getContext());
            timeText.setText(formatTime(time)); // Convert to 12-hour format
            timeText.setTextSize(14);
            timeText.setTextColor(getResources().getColor(R.color.black));

            // Delete Button (ex_close)
            ImageButton deleteButton = new ImageButton(getContext());
            deleteButton.setImageResource(R.drawable.remove_sched);
            deleteButton.setBackground(null); // Remove default button background
            deleteButton.setPadding(5, 5, 5, 5);
            deleteButton.setAdjustViewBounds(true);
            deleteButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // Remove the schedule when clicking the delete button
            deleteButton.setOnClickListener(v -> {
                wateringSchedules.remove(time);
                updateSchedulePreview(); // Refresh UI
            });

            // Add time text and delete button side by side
            itemLayout.addView(timeText);
            itemLayout.addView(deleteButton);

            // Add this horizontal layout to the FlexboxLayout
            schedulePreviewLayout.addView(itemLayout);
        }

        moistureInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                moistureInput.clearFocus();
                validateInputs();
                return true;
            }
            return false;
        });

    }



    private String formatTime(String time) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return time; // If there's an error, return the original time
        }
    }



    private void validateInputs() {
        boolean isValid = true;

        // Plant Name Validation
        String plantName = inputPlantName.getText().toString().trim();
        if (plantName.isEmpty()) {
            plantNameLayout.setError("Plant name cannot be empty");
            isValid = false;
        } else if (!plantName.matches("^[a-zA-Z\\s]+$")) {
            plantNameLayout.setError("Only letters allowed");
            isValid = false;
        } else {
            plantNameLayout.setError(null);
        }

        // Soil Moisture Validation
        String moisture = moistureInput.getText().toString().trim();
        if (moistureInputLayout.getVisibility() == View.VISIBLE) {
            if (moisture.isEmpty()) {
                moistureInputLayout.setError("Soil moisture cannot be empty");
                isValid = false;
            } else {
                try {
                    int moistureValue = Integer.parseInt(moisture);
                    if (moistureValue < 0 || moistureValue > 100) {
                        moistureInputLayout.setError("Enter a valid percentage (0-100)");
                        isValid = false;
                    } else {
                        moistureInputLayout.setError(null);
                    }
                } catch (NumberFormatException e) {
                    moistureInputLayout.setError("Invalid number");
                    isValid = false;
                }
            }
        }

        // Watering Schedule Validation
        if (schedulingBtn.getVisibility() == View.VISIBLE && wateringSchedules.isEmpty()) {
            scheduleError.setVisibility(View.VISIBLE);
            scheduleError.setText("You must add at least one watering schedule");
            isValid = false;
        } else {
            scheduleError.setVisibility(View.GONE);
        }

        if (!isValid) {
            return;
        }

        // 🔹 Update Firestore with new details
        Map<String, Object> plantUpdates = new HashMap<>();
        plantUpdates.put("name", plantName);
        plantUpdates.put("minSoilMoisture", moisture + "%");
        plantUpdates.put("wateringSchedules", new ArrayList<>(wateringSchedules));

        FirebaseFirestore.getInstance().collection("plants").document(plantId)
                .update(plantUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "✅ Plant updated successfully!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "❌ Failed to update plant", Toast.LENGTH_SHORT).show());

        // 🔹 Upload new image if changed
        uploadNewImageIfChanged();
    }






    // real time validation
    private class ValidationWatcher implements TextWatcher {
        private final View view;

        public ValidationWatcher(View view) {
            this.view = view;
        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (view.getId()) {
                case R.id.inputplantName:
                    // Clear error when the user enters a valid plant name
                    if (!s.toString().trim().isEmpty() && s.toString().trim().matches("^[a-zA-Z\\s]+$")) {
                        plantNameLayout.setError(null);
                    }
                    break;

                case R.id.moisture_input:
                    // Clear error when the user enters a valid moisture percentage (0-100)
                    String moisture = s.toString().trim();
                    if (!moisture.isEmpty() && moisture.matches("^(100|[0-9]{1,2})$")) {
                        moistureInputLayout.setError(null);
                    }
                    break;
            }
        }


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }


    // Define the image picker result

    private void checkImageTransparency(Uri imageUri) {
        try {
            // Convert the URI to a Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);

            // Check if the image has transparency
            boolean hasTransparency = false;
            for (int x = 0; x < bitmap.getWidth(); x++) {
                for (int y = 0; y < bitmap.getHeight(); y++) {
                    int pixel = bitmap.getPixel(x, y);
                    int alpha = (pixel >> 24) & 0xFF; // Extract alpha value
                    if (alpha < 255) { // Found a transparent pixel
                        hasTransparency = true;
                        break;
                    }
                }
                if (hasTransparency) break;
            }

            // Show message based on transparency
            if (hasTransparency) {
                Toast.makeText(requireContext(), "✅ Image is transparent", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "❌ Image is NOT transparent", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error checking transparency", Toast.LENGTH_SHORT).show();
        }
    }


    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            imagePreview.setImageURI(selectedImageUri);
                            checkImageTransparency(selectedImageUri);
                        }
                    });

    private void openImagePicker() {
        imagePickerLauncher.launch("image/png");
    }


    private boolean isImageTransparent(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);

            for (int x = 0; x < bitmap.getWidth(); x++) {
                for (int y = 0; y < bitmap.getHeight(); y++) {
                    int pixel = bitmap.getPixel(x, y);
                    int alpha = (pixel >> 24) & 0xFF;
                    if (alpha < 255) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // No transparency found
    }




    private void fetchPlantDetails(String plantId) {
        FirebaseFirestore.getInstance().collection("plants").document(plantId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        inputPlantName.setText(documentSnapshot.getString("name"));
                        moistureInput.setText(documentSnapshot.getString("minSoilMoisture").replace("%", ""));

                        // Load image
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(requireContext()).load(imageUrl).into(imagePreview);
                            selectedImageUri = Uri.parse(imageUrl);
                        }

                        // Load watering schedules
                        List<String> schedules = (List<String>) documentSnapshot.get("wateringSchedules");
                        if (schedules != null) {
                            wateringSchedules.clear();
                            wateringSchedules.addAll(schedules);
                            updateSchedulePreview();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "❌ Failed to load plant details", e));
    }


    private void uploadNewImageIfChanged() {
        if (selectedImageUri == null) return; // No new image selected, skip upload

        StorageReference fileRef = FirebaseStorage.getInstance()
                .getReference("plant_images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + plantId);

        fileRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    FirebaseFirestore.getInstance().collection("plants").document(plantId)
                            .update("imageUrl", uri.toString())
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "✅ Image updated successfully!"))
                            .addOnFailureListener(e -> Log.e("Firestore", "❌ Failed to update image in Firestore"));
                }))
                .addOnFailureListener(e -> Log.e("FirebaseStorage", "❌ Image upload failed: " + e.getMessage()));
    }







}


