package com.example.soilmate;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
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
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddPlantPopUp extends DialogFragment {

    private ActivityResultLauncher<String> imagePickerLauncher;


    private ImageView imagePreview;


    private Uri selectedImageUri;

    private LinearLayout waterScheduling, soilMoisture;
    private Button schedulingBtn, addPlantBtn;
    private FlexboxLayout schedulePreviewLayout;
    private TextView schedulePreview, textView27, scheduleError;
    private List<String> wateringSchedules = new ArrayList<>();
    private ImageButton buttonCancel;
    private TextInputEditText inputPlantName, moistureInput;
    private TextInputLayout plantNameLayout, moistureInputLayout;

    private FirebaseAuth auth;
    private FirebaseFirestore databaseReference;

    private StorageReference storageReference;

    private ProgressBar progressBar;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_add_plant_pop_up, null);
        // Inside onCreateDialog(), update the background to the rounded drawable
        view.setBackgroundResource(R.drawable.rounded_dialog_bg);  // Set the rounded background



        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("plant_images");


        imagePreview = view.findViewById(R.id.image_preview);
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
        progressBar = view.findViewById(R.id.progressBar);


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



        buttonCancel.setOnClickListener(v -> dismiss());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        Dialog dialog = builder.create();
        // Set Transparent Background to Enable Rounded Corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // Make the background transparent
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


        addPlantBtn.setOnClickListener(v -> validateAndUpload());

        // Initialize ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imagePreview.setImageURI(selectedImageUri);
                    }
                });

        imagePreview.setOnClickListener(v -> openImagePicker());



        return dialog;

    }

    // Toggle Active/Inactive Mode
    private void toggleMode(LinearLayout active, LinearLayout inactive) {
        Drawable activeBg = getResources().getDrawable(R.drawable.active_mode_bg);
        Drawable inactiveBg = getResources().getDrawable(R.drawable.inactive_mode_bg);
        active.setBackground(activeBg);
        inactive.setBackground(inactiveBg);
    }

    // Initialize the image picker launcher to allow image selection




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



    private void updateSchedulePreview() {
        schedulePreviewLayout.removeAllViews();

        for (String time : wateringSchedules) {

            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(5, 5, 5, 5);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);


            TextView timeText = new TextView(getContext());
            timeText.setText(formatTime(time));
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


            deleteButton.setOnClickListener(v -> {
                wateringSchedules.remove(time);
                updateSchedulePreview();
            });


            itemLayout.addView(timeText);
            itemLayout.addView(deleteButton);


            schedulePreviewLayout.addView(itemLayout);
        }

        moistureInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                moistureInput.clearFocus();
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


    // rea; time validation
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


    // Open the image picker to allow user to pick a PNG image
    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }


    private boolean isImageTransparent(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);

            for (int x = 0; x < bitmap.getWidth(); x++) {
                for (int y = 0; y < bitmap.getHeight(); y++) {
                    int pixel = bitmap.getPixel(x, y);
                    int alpha = (pixel >> 24) & 0xFF;
                    if (alpha < 255) {
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;  // No transparency detected
    }


    // Validate and upload the image
    private void validateAndUpload() {
        boolean isValid = true;

        // Validate plant name
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

        // Validate moisture level
        String moisture = moistureInput.getText().toString().trim();
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

        // Image validation
        if (selectedImageUri == null) {
            Toast.makeText(requireContext(), "⚠️ Please select an image!", Toast.LENGTH_SHORT).show();
            isValid = false;
        } else if (!isImageTransparent(selectedImageUri)) {
            Toast.makeText(requireContext(), "❌ Image must be transparent!", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        // If everything is valid, proceed with the upload
        if (!isValid) return;

        // Hide the "Add Plant" button and show progress bar while uploading
        addPlantBtn.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        // Upload the image to Firebase
        uploadImageToFirebase(plantName, moisture);
    }




    // Upload image to Firebase
    private void uploadImageToFirebase(String plantName, String moisture) {
        String userId = auth.getCurrentUser ().getUid();
        String imageName = UUID.randomUUID().toString(); // Generate a unique image name
        StorageReference imageRef = storageReference.child(userId).child(imageName);

        imageRef.putFile(selectedImageUri)  // Upload the image file to Firebase
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    savePlantToDatabase(plantName, moisture, uri.toString());  // Save plant data with the image URL
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    addPlantBtn.setVisibility(View.VISIBLE);  // Show the button again if upload fails
                });
    }



    private void savePlantToDatabase(String plantName, String moisture, String imageUrl) {
        FirebaseUser  user = auth.getCurrentUser ();
        if (user == null) {
            Log.e("FirestoreDebug", "❌ User not authenticated!");
            Toast.makeText(requireContext(), "You must be logged in to add a plant!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        String plantId = FirebaseFirestore.getInstance().collection("plants").document().getId();

        Map<String, Object> plantData = new HashMap<>();
        plantData.put("name", plantName);
        plantData.put("imageUrl", imageUrl); // This is the URL of the uploaded image
        plantData.put("actualImageUrl", imageUrl); // Set the actual image URL to the uploaded image URL
        plantData.put("minSoilMoisture", moisture + "%");
        plantData.put("userId", userId);
        plantData.put("wateringSchedules", new ArrayList<>(wateringSchedules));

        databaseReference.collection("plants").document(plantId)
                .set(plantData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreDebug", "🔥 Plant added successfully");
                    Toast.makeText(requireContext(), "✅ Plant added successfully!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreDebug", "❌ Failed to save plant: " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to save plant", Toast.LENGTH_SHORT).show();
                });
    }






}
