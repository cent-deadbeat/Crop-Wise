package com.example.soilmate;

import android.app.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.content.ActivityNotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import androidx.appcompat.widget.SwitchCompat;

public class MyAccount extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private static final int PICK_IMAGE = 1;
    private ImageView profileImage;
    private Uri imageUri;

    private FirebaseAuth auth;
    private FirebaseUser user;
    private EditText usernameInput, passwordUsername, emailInput, emailPassword, newPasswordInput, currentPasswordInput, phoneInput, phonePassword;
    private TextView errorUsername, errorPassUsername, errorEmail, errorPassEmail, errorNewPassword, errorCurrentPassword, errorPhone, errorPhonePassword, googleSignInMessage;
    private AppCompatButton changeUsernameBtn, changeEmailBtn, changePassBtn, changePhoneBtn, verifyPhoneButton;

    private String verificationId;
    private String passwordChangeVerificationId;
    private String phoneChangeVerificationId;
    private Spinner spinnerCountryCode;

    // Add these as class variables
    private TextWatcher phoneTextWatcher;
    private TextWatcher phonePasswordWatcher;


    private View currentEditingSection = null;

    private SwitchCompat switch2FA; // Use SwitchCompat instead of Switch
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        googleSignInMessage = findViewById(R.id.googleSignInMessage);
        verifyPhoneButton = findViewById(R.id.verifyPhoneButton);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        switch2FA = findViewById(R.id.switch_2fa); // Ensure this ID exists in your layout
        db = FirebaseFirestore.getInstance();

        // Load the current 2FA status from Firestore
        load2FAStatus();

        // Set up the 2FA toggle listener
        switch2FA.setOnCheckedChangeListener((buttonView, isChecked) -> {
            update2FAStatus(isChecked);
        });

        phoneInput = findViewById(R.id.changephone);
        phonePassword = findViewById(R.id.change_phone_password);
        errorPhone = findViewById(R.id.errorchange_phone);
        errorPhonePassword = findViewById(R.id.error_change_phone_password);
        changePhoneBtn = findViewById(R.id.changephone_btn);
        spinnerCountryCode = findViewById(R.id.spinnerCountryCode);



        phoneInput.setVisibility(View.GONE);
        phonePassword.setVisibility(View.GONE);
        changePhoneBtn.setVisibility(View.GONE);


        setupCountryCodeSpinner();


        findViewById(R.id.phone_edit).setOnClickListener(v -> openPhoneSection());
        changePhoneBtn.setOnClickListener(v -> validatePhoneChange());


        // Sidebar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("");

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        ImageButton menuButton = findViewById(R.id.menubtn);
        menuButton.setOnClickListener(view -> {
            updateSidebarInfo();
            drawerLayout.openDrawer(navigationView);
        });

        //WORKING

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.nav_plants:
                        startActivity(new Intent(MyAccount.this, MyPlants.class));

                        break;
                    case R.id.nav_my_acc:
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.nav_guide:
                        startActivity(new Intent(MyAccount.this, UserGuide.class));
                        break;
                    case R.id.nav_help:
                        startActivity(new Intent(MyAccount.this, Help.class));
                        break;
                    case R.id.nav_logout:
                        showLogoutConfirmation();
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });




        usernameInput = findViewById(R.id.username);
        passwordUsername = findViewById(R.id.password_username);
        emailInput = findViewById(R.id.email);
        emailPassword = findViewById(R.id.email_password);
        newPasswordInput = findViewById(R.id.changepassword);
        currentPasswordInput = findViewById(R.id.change_pass_password);

        errorUsername = findViewById(R.id.errorUsername);
        errorPassUsername = findViewById(R.id.pass_error_username);
        errorEmail = findViewById(R.id.erroremail);
        errorPassEmail = findViewById(R.id.email_error_pass);
        errorNewPassword = findViewById(R.id.errorchange_pass);
        errorCurrentPassword = findViewById(R.id.error_change_pass_password);

        changeUsernameBtn = findViewById(R.id.changeusername_btn);
        changeEmailBtn = findViewById(R.id.changeemail_btn);
        changePassBtn = findViewById(R.id.changepass_btn);

        // Edit button click listeners (only one section can be edited at a time)
        findViewById(R.id.username_edit).setOnClickListener(v -> openSection(usernameInput, passwordUsername, changeUsernameBtn));
        findViewById(R.id.email_edit).setOnClickListener(v -> openSection(emailInput, emailPassword, changeEmailBtn));
        findViewById(R.id.password_edit).setOnClickListener(v -> openSection(newPasswordInput, currentPasswordInput, changePassBtn));




        // Set up live validation for phone section
        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phone = s.toString().trim();

                // Validate phone number length
                if (phone.length() < 10) {
                    errorPhone.setText("Phone number must be 10 digits");
                    errorPhone.setVisibility(View.VISIBLE);
                } else {
                    errorPhone.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        phonePassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString().trim();
                if (TextUtils.isEmpty(password)) {
                    errorPhonePassword.setText("Enter your password");
                    errorPhonePassword.setVisibility(View.VISIBLE);
                } else {
                    errorPhonePassword.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });



        // Automatically add +63 and limit input length
        phoneInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phone = s.toString().trim();



                // Limit input to 13 characters
                if (phone.length() > 10) {
                    phoneInput.setText(phone.substring(0, 10));
                    phoneInput.setSelection(10); // Move cursor to the end
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        if (isGoogleSignedIn()) {
            setupForGoogleUser();
            setupGoogleAccountManagement();
            return;
        }





        changeUsernameBtn.setOnClickListener(v -> {
            if (validateUsername() && validatePasswordField(passwordUsername, errorPassUsername)) {
                String newUsername = usernameInput.getText().toString().trim();
                String password = passwordUsername.getText().toString().trim();

                // Check if username already exists
                checkUsernameExists(newUsername, () -> {
                    // ✅ Call reAuthenticate with passwordUsername as the active field
                    reAuthenticate(password, passwordUsername, () -> {
                        user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(newUsername).build())
                                .addOnSuccessListener(aVoid -> {
                                    FirebaseFirestore.getInstance().collection("users")
                                            .document(user.getUid())
                                            .update("username", newUsername)
                                            .addOnSuccessListener(aVoid1 -> {
                                                updateAccountTextViews();
                                                updateSidebarInfo();
                                                Toast.makeText(MyAccount.this, "Username updated!", Toast.LENGTH_SHORT).show();
                                                resetFields(usernameInput, passwordUsername);
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(MyAccount.this, "Error updating username", Toast.LENGTH_SHORT).show());
                                })
                                .addOnFailureListener(e -> Toast.makeText(MyAccount.this, "Failed to update Firebase Authentication", Toast.LENGTH_SHORT).show());
                    });
                });
            }
        });




        changeEmailBtn.setOnClickListener(v -> {
            if (validateEmail() && validatePasswordField(emailPassword, errorPassEmail)) {
                String newEmail = emailInput.getText().toString().trim();
                String password = emailPassword.getText().toString().trim();

                // Check if email already exists
                checkEmailExists(newEmail, () -> {
                    reAuthenticate(password, emailPassword, () -> {
                        ProgressDialog progressDialog = new ProgressDialog(MyAccount.this);
                        progressDialog.setMessage("Sending verification email...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();

                        // Store pending email in Firestore first
                        FirebaseFirestore.getInstance().collection("users")
                                .document(user.getUid())
                                .update("pendingEmail", newEmail)
                                .addOnSuccessListener(aVoid -> {
                                    // Remove ActionCodeSettings to use default Firebase behavior
                                    user.verifyBeforeUpdateEmail(newEmail)
                                            .addOnCompleteListener(task -> {
                                                progressDialog.dismiss();
                                                if (task.isSuccessful()) {
                                                    Intent intent = new Intent(MyAccount.this, ChangeEmailVerifSent.class);
                                                    intent.putExtra("newEmail", newEmail);
                                                    startActivity(intent);
                                                    finish(); // Close current activity
                                                } else {
                                                    // Clean up pending email if failed
                                                    FirebaseFirestore.getInstance().collection("users")
                                                            .document(user.getUid())
                                                            .update("pendingEmail", null);

                                                    Toast.makeText(MyAccount.this,
                                                            "Failed to send verification: " +
                                                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(MyAccount.this,
                                            "Failed to initiate email change: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    });
                });
            }
        });



        changePassBtn.setOnClickListener(v -> {
            // Check if phone is verified
            FirebaseFirestore.getInstance().collection("users")
                    .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            boolean isPhoneVerified = Boolean.TRUE.equals(documentSnapshot.getBoolean("isPhoneVerified"));
                            if (!isPhoneVerified) {
                                Toast.makeText(this, "Please verify your phone number first", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Proceed with password change if phone is verified
                            if (validatePassword()) {
                                String oldPassword = currentPasswordInput.getText().toString().trim();
                                String newPassword = newPasswordInput.getText().toString().trim();

                                // Re-authenticate the user with their current password
                                reAuthenticate(oldPassword, currentPasswordInput, () -> {
                                    // Fetch the registered phone number from Firestore
                                    FirebaseFirestore.getInstance().collection("users")
                                            .document(user.getUid())
                                            .get()
                                            .addOnSuccessListener(documentSnapshot1 -> {
                                                if (documentSnapshot1.exists()) {
                                                    String phoneNumber = documentSnapshot1.getString("phoneNumber");
                                                    if (phoneNumber != null && !phoneNumber.isEmpty()) {
                                                        // Send OTP to the registered phone number
                                                        sendOTPForPasswordChange(phoneNumber);
                                                    } else {
                                                        Toast.makeText(this, "No registered phone number found", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Failed to fetch phone number: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to check phone verification status", Toast.LENGTH_SHORT).show();
                    });
        });


        // Check phone verification status
        checkPhoneVerificationStatus();

        // Set click listener for the button
        verifyPhoneButton.setOnClickListener(v -> {
            // Send OTP to the registered phone number
            sendOTPForPhoneVerification();
            showOTPDialogForPhoneVerification(); // Show the OTP dialog
        });


        // In onCreate(), replace the anonymous TextWatchers with these:

        phoneTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String phone = s.toString().trim();
                if (phone.length() < 10) {
                    errorPhone.setText("Phone number must be 10 digits");
                    errorPhone.setVisibility(View.VISIBLE);
                } else {
                    errorPhone.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        phonePasswordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString().trim();
                if (TextUtils.isEmpty(password)) {
                    errorPhonePassword.setText("Enter your password");
                    errorPhonePassword.setVisibility(View.VISIBLE);
                } else {
                    errorPhonePassword.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        phoneInput.addTextChangedListener(phoneTextWatcher);
        phonePassword.addTextChangedListener(phonePasswordWatcher);





        setupPasswordToggle(passwordUsername, R.drawable.ass);
        setupPasswordToggle(emailPassword, R.drawable.ass);
        setupPasswordToggle(newPasswordInput, R.drawable.ass);
        setupPasswordToggle(currentPasswordInput, R.drawable.ass);
        setupPasswordToggle(phonePassword, R.drawable.ass);


        // Live validation remains unchanged
        setupValidationOnFocusChange(usernameInput, errorUsername, this::validateUsername);
        setupValidationOnFocusChange(emailInput, errorEmail, this::validateEmail);
        setupValidationOnFocusChange(newPasswordInput, errorNewPassword, this::validatePassword);



        //CHANGE DP
        profileImage = findViewById(R.id.profile_image);
        ImageButton changeDpButton = findViewById(R.id.change_dp);

        changeDpButton.setOnClickListener(v -> openImagePicker());
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);

        updateAccountTextViews();
        updateSidebarInfo();
        checkAuthProvider();
        resetUIState();

    }





    private void setupCountryCodeSpinner() {
        // List of countries with their codes and flag drawable resources
        List<Country> countries = new ArrayList<>();
        countries.add(new Country("+63", R.drawable.ic_phil)); // Philippines
        countries.add(new Country("+82", R.drawable.ic_korea)); // South Korea
        countries.add(new Country("+86", R.drawable.ic_china)); // China
        countries.add(new Country("+91", R.drawable.ic_india)); // India
        countries.add(new Country("+98", R.drawable.ic_iran)); // Iran
        countries.add(new Country("+81", R.drawable.ic_japan)); // Japan
        countries.add(new Country("+65", R.drawable.ic_singapore)); // Singapore
        countries.add(new Country("+66", R.drawable.ic_thailand)); // Thailand
        countries.add(new Country("+92", R.drawable.ic_pakistan)); // Pakistan

        // Create and set the adapter
        CountryAdapter adapter = new CountryAdapter(this, countries);
        spinnerCountryCode.setAdapter(adapter);
    }





    // Method to load 2FA status from Firestore
    private void load2FAStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean is2FAEnabled = Boolean.TRUE.equals(documentSnapshot.getBoolean("is2FAEnabled"));
                        switch2FA.setChecked(is2FAEnabled);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load 2FA status", Toast.LENGTH_SHORT).show();
                });
    }

    // Method to update 2FA status in Firestore
    private void update2FAStatus(boolean isEnabled) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .update("is2FAEnabled", isEnabled)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "2FA " + (isEnabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update 2FA status", Toast.LENGTH_SHORT).show();
                });
    }

    private void openSection(EditText inputField, EditText passwordField, AppCompatButton button) {
        // If the clicked section is already open, hide it
        if (currentEditingSection == inputField) {
            closeCurrentSection();
            return;
        }

        // Close any currently open section
        if (currentEditingSection != null) closeCurrentSection();

        // Open new section
        currentEditingSection = inputField;
        inputField.setVisibility(View.VISIBLE);
        passwordField.setVisibility(View.VISIBLE);
        button.setVisibility(View.VISIBLE);

        errorUsername.setVisibility(View.GONE);
        errorPassUsername.setVisibility(View.GONE);
        errorEmail.setVisibility(View.GONE);
        errorPassEmail.setVisibility(View.GONE);
        errorNewPassword.setVisibility(View.GONE);
        errorCurrentPassword.setVisibility(View.GONE);
    }

    private void closeCurrentSection() {
        if (currentEditingSection == usernameInput) {
            usernameInput.setVisibility(View.GONE);
            passwordUsername.setVisibility(View.GONE);
            changeUsernameBtn.setVisibility(View.GONE);
            errorUsername.setVisibility(View.GONE);
            errorPassUsername.setVisibility(View.GONE);
        } else if (currentEditingSection == emailInput) {
            emailInput.setVisibility(View.GONE);
            emailPassword.setVisibility(View.GONE);
            changeEmailBtn.setVisibility(View.GONE);
            errorEmail.setVisibility(View.GONE);
            errorPassEmail.setVisibility(View.GONE);
        } else if (currentEditingSection == newPasswordInput) {
            newPasswordInput.setVisibility(View.GONE);
            currentPasswordInput.setVisibility(View.GONE);
            changePassBtn.setVisibility(View.GONE);
            errorNewPassword.setVisibility(View.GONE);
            errorCurrentPassword.setVisibility(View.GONE);
        } else if (currentEditingSection == phoneInput) {
            phoneInput.setVisibility(View.GONE);
            spinnerCountryCode.setVisibility(View.GONE); // Hide the Spinner
            phonePassword.setVisibility(View.GONE);
            changePhoneBtn.setVisibility(View.GONE);
            errorPhone.setVisibility(View.GONE);
            errorPhonePassword.setVisibility(View.GONE);
        }
        currentEditingSection = null;
    }

    private void resetFields(EditText mainInput, EditText passwordInput) {
        // Clear input fields
        mainInput.setText("");
        passwordInput.setText("");

        // Hide error messages
        errorUsername.setVisibility(View.GONE);
        errorPassUsername.setVisibility(View.GONE);
        errorEmail.setVisibility(View.GONE);
        errorPassEmail.setVisibility(View.GONE);
        errorNewPassword.setVisibility(View.GONE);
        errorCurrentPassword.setVisibility(View.GONE);

        // Hide the section
        closeCurrentSection();
    }


    private void setupPasswordToggle(EditText passwordField, int startIcon) {
        passwordField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordField.getRight() - passwordField.getCompoundDrawables()[2].getBounds().width())) {

                    if (passwordField.getTransformationMethod() instanceof PasswordTransformationMethod) {
                        // Show password
                        passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(startIcon, 0, R.drawable.hide, 0);
                    } else {
                        // Hide password
                        passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(startIcon, 0, R.drawable.ic_hide_pass, 0);
                    }

                    passwordField.setSelection(passwordField.getText().length());
                    return true;
                }
            }
            return false;
        });
    }



    private boolean validateUsername() {
        String username = usernameInput.getText().toString().trim();

        if (username.isEmpty()) {
            errorUsername.setText("Username cannot be empty.");
            errorUsername.setVisibility(View.VISIBLE);
            return false;
        }
        if (username.length() > 25) {
            errorUsername.setText("Username must not exceed 25 characters.");
            errorUsername.setVisibility(View.VISIBLE);
            return false;
        }

        errorUsername.setVisibility(View.GONE);
        return true;
    }


    private boolean validateEmail() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            errorEmail.setText("Email cannot be empty.");
            errorEmail.setVisibility(View.VISIBLE);
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorEmail.setText("Enter a valid email");
            errorEmail.setVisibility(View.VISIBLE);
            return false;
        }

        errorEmail.setVisibility(View.GONE);
        return true;
    }



    private boolean validatePassword() {
        return validatePasswordField(newPasswordInput, errorNewPassword) && validatePasswordField(currentPasswordInput, errorCurrentPassword);
    }

    private void setupValidationOnFocusChange(EditText field, TextView errorField, Runnable validationMethod) {
        field.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) validationMethod.run();
        });

        field.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validationMethod.run();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }


    private boolean validatePasswordField(EditText field, TextView errorField) {
        String password = field.getText().toString().trim();

        if (field == newPasswordInput) {
            if (password.isEmpty()) {
                errorField.setText("Password cannot be empty.");
                errorField.setVisibility(View.VISIBLE);
                return false;
            }
            if (password.length() < 6) {
                errorField.setText("Password must be at least 6 characters long.");
                errorField.setVisibility(View.VISIBLE);
                return false;
            }
            if (!password.matches(".*[A-Z].*")) {
                errorField.setText("Password must contain at least one uppercase letter.");
                errorField.setVisibility(View.VISIBLE);
                return false;
            }
            if (!password.matches(".*\\d.*")) {
                errorField.setText("Password must contain at least one number.");
                errorField.setVisibility(View.VISIBLE);
                return false;
            }
            if (!password.matches(".*[@#$%^&+=!].*")) {
                errorField.setText("Password must contain at least one special character (@#$%^&+=!).");
                errorField.setVisibility(View.VISIBLE);
                return false;
            }

            errorField.setVisibility(View.GONE);
        }
        return true;
    }



    @Override
    public void onBackPressed() {
        // Hide all error messages
        errorUsername.setVisibility(View.GONE);
        errorPassUsername.setVisibility(View.GONE);
        errorEmail.setVisibility(View.GONE);
        errorPassEmail.setVisibility(View.GONE);
        errorNewPassword.setVisibility(View.GONE);
        errorCurrentPassword.setVisibility(View.GONE);

        super.onBackPressed();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData(); // Get selected image URI
            showImagePreviewDialog(imageUri); // Show preview before upload
        }
    }


    private void uploadProfileImage(Uri imageUri) {
        if (imageUri == null) return;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile picture...");
        progressDialog.setCancelable(false); // Prevent the user from dismissing the dialog
        progressDialog.show();

        // Firebase Storage path: profile_images/userID.jpg
        StorageReference fileRef = FirebaseStorage.getInstance().getReference("profile_images/" + user.getUid() + ".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // ✅ Update Firebase Authentication profile picture
                    user.updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(uri).build())
                            .addOnSuccessListener(aVoid -> {
                                // ✅ Store in Firestore
                                FirebaseFirestore.getInstance().collection("users")
                                        .document(user.getUid())
                                        .update("profileImage", uri.toString())
                                        .addOnSuccessListener(aVoid1 -> {
                                            progressDialog.dismiss();
                                            // ✅ Update UI
                                            Glide.with(this).load(uri).circleCrop().into(profileImage);
                                            updateSidebarInfo();
                                            Toast.makeText(MyAccount.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(MyAccount.this, "Failed to save image URL in Firestore", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(MyAccount.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                            });
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(MyAccount.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                });
    }



    //image preview, circle frame
    private void showImagePreviewDialog(Uri imageUri) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.profile_preview_popup);
        dialog.setCancelable(true);

        ImageView previewImage = dialog.findViewById(R.id.imagePreview);
        Button setProfileButton = dialog.findViewById(R.id.btnSetProfile);
        ImageButton closeButton = dialog.findViewById(R.id.btnClose);

        Glide.with(this).load(imageUri).override(300, 300).circleCrop().into(previewImage);

        setProfileButton.setOnClickListener(v -> {
            uploadProfileImage(imageUri); // Upload image to Firebase
            dialog.dismiss();
        });

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }



    private void updateAccountTextViews() {
        TextView displayUser1 = findViewById(R.id.displayuser1);
        TextView displayUser2 = findViewById(R.id.displayuser2);
        TextView displayEmail1 = findViewById(R.id.displayemail1);
        TextView displayEmail2 = findViewById(R.id.displayemail2);
        TextView numPlantsText = findViewById(R.id.num_plants);
        TextView displayPhone = findViewById(R.id.textView41); // Phone number TextView
        ImageView profileImage = findViewById(R.id.profile_image);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(MyAccount.this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reload user to get latest email & profile updates
        user.reload().addOnSuccessListener(aVoid -> {
            if (user.isEmailVerified()) {
                displayEmail1.setText(user.getEmail());
                displayEmail2.setText(user.getEmail());
            }

            // Load profile image
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .into(profileImage);
            } else {
                profileImage.setImageResource(R.drawable.profile);
            }
        });

        // Fetch username, phone number, and plant count from Firestore
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch username
                        String username = documentSnapshot.getString("username");
                        if (username != null) {
                            displayUser1.setText(username);
                            displayUser2.setText(username);
                        } else {
                            displayUser1.setText("No username found");
                            displayUser2.setText("No username found");
                        }

                        // Fetch phone number
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            // Display phone number with country code
                            displayPhone.setText(phoneNumber);
                        } else {
                            displayPhone.setText("No phone number found");
                        } //THIS IS THE LAST ONE

                        // Fetch number of plants
                        FirebaseFirestore.getInstance().collection("plants")
                                .whereEqualTo("userId", user.getUid())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    int plantCount = queryDocumentSnapshots.size();
                                    numPlantsText.setText(String.valueOf(plantCount));
                                })
                                .addOnFailureListener(e -> numPlantsText.setText("0"));
                    }
                })
                .addOnFailureListener(e -> {
                    displayUser1.setText("Error loading username");
                    numPlantsText.setText("0");
                });
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
        FirebaseAuth.getInstance().signOut();
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






    private void reAuthenticate(String password, EditText activePasswordField, Runnable onSuccess) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> onSuccess.run())
                    .addOnFailureListener(e -> {
                        // Clear all error fields first
                        errorPassUsername.setVisibility(View.GONE);
                        errorPassEmail.setVisibility(View.GONE);
                        errorCurrentPassword.setVisibility(View.GONE);
                        errorPhonePassword.setVisibility(View.GONE);

                        // Show error message only under the active field
                        if (activePasswordField == passwordUsername) {
                            errorPassUsername.setText("Wrong password entered.");
                            errorPassUsername.setVisibility(View.VISIBLE);
                        } else if (activePasswordField == emailPassword) {
                            errorPassEmail.setText("Wrong password entered.");
                            errorPassEmail.setVisibility(View.VISIBLE);
                        } else if (activePasswordField == currentPasswordInput) {
                            errorCurrentPassword.setText("Wrong password entered.");
                            errorCurrentPassword.setVisibility(View.VISIBLE);
                        } else if (activePasswordField == phonePassword) {
                            errorPhonePassword.setText("Wrong password entered.");
                            errorPhonePassword.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }









    //CHANGE PHONE
    private void sendOTPForPhoneChange(String phoneNumber) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                120, // Timeout duration
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        progressDialog.dismiss();
                        verifyOTPForPhoneChange(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressDialog.dismiss();
                        Toast.makeText(MyAccount.this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        progressDialog.dismiss();
                        phoneChangeVerificationId = verificationId; // Store the verification ID for phone change
                        showOTPDialogForPhoneChange();
                    }
                });
    }

    private void showOTPDialogForPhoneChange() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_otp_phone_change, null);
        builder.setView(dialogView);

        // Initialize views
        EditText otpInput = dialogView.findViewById(R.id.otpInput);
        Button verifyButton = dialogView.findViewById(R.id.verifyButton);
        Button resendButton = dialogView.findViewById(R.id.resendButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        TextView timerText = dialogView.findViewById(R.id.timerText);

        // Create the dialog
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Prevent dialog from being dismissed by tapping outside
        dialog.setCanceledOnTouchOutside(false); // Prevent dialog from being dismissed by tapping outside
        dialog.show();

        // Start the timer
        startOTPTimer(timerText, resendButton);

        // Set the Verify button click listener
        verifyButton.setOnClickListener(v -> {
            String otpCode = otpInput.getText().toString().trim();
            if (!otpCode.isEmpty() && otpCode.length() == 6) {
                verifyOTPForPhoneChange(otpCode);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });

        // Set the Resend button click listener
        resendButton.setOnClickListener(v -> {
            // Fetch the new phone number
            String newPhone = phoneInput.getText().toString().trim();
            Country selectedCountry = (Country) spinnerCountryCode.getSelectedItem();
            String countryCode = selectedCountry.getCode();
            String formattedPhoneNumber = countryCode + newPhone;

            // Resend OTP
            sendOTPForPhoneChange(formattedPhoneNumber);
            startOTPTimer(timerText, resendButton);
        });

        // Set the Cancel button click listener
        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void verifyOTPForPhoneChange(String otpCode) {
        if (phoneChangeVerificationId == null || otpCode.isEmpty() || otpCode.length() != 6) {
            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the PhoneAuthCredential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneChangeVerificationId, otpCode);
        verifyOTPForPhoneChange(credential);
    }

    private void verifyOTPForPhoneChange(PhoneAuthCredential credential) {
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Fetch the new phone number
                    String newPhone = phoneInput.getText().toString().trim();
                    Country selectedCountry = (Country) spinnerCountryCode.getSelectedItem();
                    String countryCode = selectedCountry.getCode();
                    String formattedNewPhoneNumber = countryCode + newPhone;

                    // Send OTP to the new phone number for verification
                    sendOTPForNewPhoneVerification(formattedNewPhoneNumber);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "OTP verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendOTPForNewPhoneVerification(String newPhoneNumber) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP to new phone...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                newPhoneNumber,
                120, // Timeout duration
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        progressDialog.dismiss();
                        updatePhoneNumber(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressDialog.dismiss();
                        Toast.makeText(MyAccount.this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        progressDialog.dismiss();
                        phoneChangeVerificationId = verificationId; // Store the verification ID for new phone verification
                        showOTPDialogForNewPhoneVerification();
                    }
                });
    }

    private void showOTPDialogForNewPhoneVerification() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_otp_phone_change, null);
        builder.setView(dialogView);

        EditText otpInput = dialogView.findViewById(R.id.otpInput);
        Button verifyButton = dialogView.findViewById(R.id.verifyButton);

        AlertDialog dialog = builder.create();
        dialog.show();

        verifyButton.setOnClickListener(v -> {
            String otpCode = otpInput.getText().toString().trim();
            if (!otpCode.isEmpty() && otpCode.length() == 6) {
                verifyOTPForNewPhoneVerification(otpCode);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOTPForNewPhoneVerification(String otpCode) {
        if (phoneChangeVerificationId == null || otpCode.isEmpty() || otpCode.length() != 6) {
            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the PhoneAuthCredential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneChangeVerificationId, otpCode);
        updatePhoneNumber(credential);
    }




    private void openPhoneSection() {
        // If the phone section is already open, hide it
        if (currentEditingSection == phoneInput) {
            closeCurrentSection();
            return;
        }

        // Close any currently open section
        if (currentEditingSection != null) closeCurrentSection();

        // Open the phone section
        currentEditingSection = phoneInput;
        phoneInput.setVisibility(View.VISIBLE);
        spinnerCountryCode.setVisibility(View.VISIBLE); // Show the Spinner
        phonePassword.setVisibility(View.VISIBLE);
        changePhoneBtn.setVisibility(View.VISIBLE);

        // Hide error messages
        errorPhone.setVisibility(View.GONE);
        errorPhonePassword.setVisibility(View.GONE);
    }



    private void validatePhoneChange() {
        String newPhone = phoneInput.getText().toString().trim();
        String password = phonePassword.getText().toString().trim();

        // Validate phone number length (10 digits)
        if (newPhone.length() != 10) {
            errorPhone.setText("Phone number must be 10 digits");
            errorPhone.setVisibility(View.VISIBLE);
            return;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            errorPhonePassword.setText("Enter your password");
            errorPhonePassword.setVisibility(View.VISIBLE);
            return;
        }

        // Re-authenticate the user with their current password
        reAuthenticate(password, phonePassword, () -> {
            // Fetch the selected country code
            Country selectedCountry = (Country) spinnerCountryCode.getSelectedItem();
            String countryCode = selectedCountry.getCode();

            // Format the new phone number with the country code
            String formattedPhoneNumber = countryCode + newPhone;

            // Send OTP to the new phone number
            sendOTPForNewPhoneVerification(formattedPhoneNumber);
        });
    }


    //CHANGE PASSWORD

    private void sendOTPForPasswordChange(String phoneNumber) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Ensure this includes the country code
                120, // Timeout duration
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        progressDialog.dismiss();
                        verifyOTPForPasswordChange(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressDialog.dismiss();
                        Toast.makeText(MyAccount.this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        progressDialog.dismiss();
                        passwordChangeVerificationId = verificationId; // Store the verification ID for password change
                        showOTPDialogForPasswordChange();
                    }
                });
    }

    private void showOTPDialogForPasswordChange() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_otp_password_change, null);
        builder.setView(dialogView);

        // Initialize views
        EditText otpInput = dialogView.findViewById(R.id.otpInput);
        Button verifyButton = dialogView.findViewById(R.id.verifyButton);
        Button resendButton = dialogView.findViewById(R.id.resendButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        TextView timerText = dialogView.findViewById(R.id.timerText);

        // Create the dialog
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Prevent dialog from being dismissed by tapping outside
        dialog.setCanceledOnTouchOutside(false); // Prevent dialog from being dismissed by tapping outside
        dialog.show();

        // Start the timer
        startOTPTimer(timerText, resendButton);

        // Set the Verify button click listener
        verifyButton.setOnClickListener(v -> {
            String otpCode = otpInput.getText().toString().trim();
            if (!otpCode.isEmpty() && otpCode.length() == 6) {
                verifyOTPForPasswordChange(otpCode);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });

        // Set the Resend button click listener
        resendButton.setOnClickListener(v -> {
            // Fetch the registered phone number from Firestore
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String phoneNumber = documentSnapshot.getString("phoneNumber");
                            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                                // Resend OTP
                                sendOTPForPasswordChange(phoneNumber);
                                startOTPTimer(timerText, resendButton); // Restart the timer
                            } else {
                                Toast.makeText(this, "No registered phone number found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to fetch phone number", Toast.LENGTH_SHORT).show();
                    });
        });

        // Set the Cancel button click listener
        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void verifyOTPForPasswordChange(String otpCode) {
        if (passwordChangeVerificationId == null || otpCode.isEmpty() || otpCode.length() != 6) {
            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the PhoneAuthCredential
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(passwordChangeVerificationId, otpCode);
        verifyOTPForPasswordChange(credential);
    }

    private void verifyOTPForPasswordChange(PhoneAuthCredential credential) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying OTP...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // First verify the OTP by signing in with the credential
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    // Now get the current user
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        progressDialog.dismiss();
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Reauthenticate with email/password (required for password change)
                    String email = user.getEmail();
                    String currentPassword = currentPasswordInput.getText().toString().trim();

                    if (email == null || email.isEmpty()) {
                        progressDialog.dismiss();
                        Toast.makeText(this, "No email found for reauthentication", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AuthCredential emailCredential = EmailAuthProvider.getCredential(email, currentPassword);

                    user.reauthenticate(emailCredential)
                            .addOnSuccessListener(aVoid -> {
                                // Now update the password
                                String newPassword = newPasswordInput.getText().toString().trim();
                                user.updatePassword(newPassword)
                                        .addOnSuccessListener(aVoid1 -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();

                                            // Call the Firebase Function to send notification email
                                            sendPasswordChangeNotificationEmail();

                                            resetFields(newPasswordInput, currentPasswordInput);
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                            Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Reauthentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "OTP verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void sendPasswordChangeNotificationEmail() {
        // Get Firebase Functions instance
        FirebaseFunctions functions = FirebaseFunctions.getInstance();

        // Create the data to send (empty in this case since we just need the auth context)
        Map<String, Object> data = new HashMap<>();

        // Call the Firebase Function
        functions.getHttpsCallable("sendPasswordChangeNotification")
                .call(data)
                .addOnSuccessListener(result -> {
                    Log.d("PasswordChangeEmail", "Notification email sent successfully");
                })
                .addOnFailureListener(e -> {
                    Log.w("PasswordChangeEmail", "Failed to send notification email", e);
                    // Don't show error to user since password change was successful
                });
    }








    private void verifyOTPAndChangePassword(PhoneAuthCredential credential, String newPassword) {
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // OTP verification successful, update the password
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                                resetFields(newPasswordInput, currentPasswordInput);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "OTP verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void updatePhoneNumber(PhoneAuthCredential credential) {
        user.updatePhoneNumber(credential).addOnSuccessListener(aVoid -> {
            String newPhone = phoneInput.getText().toString().trim();
            Country selectedCountry = (Country) spinnerCountryCode.getSelectedItem();
            String countryCode = selectedCountry.getCode();
            String formattedPhoneNumber = countryCode + newPhone;

            // Update phone number in Firestore and mark as verified
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .update(
                            "phoneNumber", formattedPhoneNumber,
                            "isPhoneVerified", true // Mark as verified immediately
                    )
                    .addOnSuccessListener(aVoid1 -> {
                        Toast.makeText(this, "Phone number updated and verified!", Toast.LENGTH_SHORT).show();

                        // Hide the verify button since the number is now verified
                        verifyPhoneButton.setVisibility(View.GONE);

                        resetPhoneChangeFields();
                        updateAccountTextViews();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update phone number in Firestore", Toast.LENGTH_SHORT).show();
                    });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to update phone number in Firebase Authentication", Toast.LENGTH_SHORT).show();
        });
    }
    private void resetPhoneChangeFields() {
        // Remove text change listeners temporarily
        phoneInput.removeTextChangedListener(phoneTextWatcher);
        phonePassword.removeTextChangedListener(phonePasswordWatcher);

        // Clear input fields
        phoneInput.setText("");
        phonePassword.setText("");

        // Hide error messages
        errorPhone.setVisibility(View.GONE);
        errorPhonePassword.setVisibility(View.GONE);

        // Hide the section
        phoneInput.setVisibility(View.GONE);
        phonePassword.setVisibility(View.GONE);
        changePhoneBtn.setVisibility(View.GONE);
        spinnerCountryCode.setVisibility(View.GONE);

        // Re-add text change listeners
        phoneInput.addTextChangedListener(phoneTextWatcher);
        phonePassword.addTextChangedListener(phonePasswordWatcher);

        // Reset the current editing section
        currentEditingSection = null;
    }


    private void checkUsernameExists(String newUsername, Runnable onSuccess) {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("username", newUsername)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        errorUsername.setText("Username is already taken");
                        errorUsername.setVisibility(View.VISIBLE);
                    } else {
                        onSuccess.run();
                    }
                });
    }

    private void checkEmailExists(String newEmail, Runnable onSuccess) {
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(newEmail)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().getSignInMethods() != null &&
                            !task.getResult().getSignInMethods().isEmpty()) {
                        errorEmail.setText("Email is already registered");
                        errorEmail.setVisibility(View.VISIBLE);
                    } else {
                        onSuccess.run();
                    }
                });
    }



    //PHONE VERIFICATION
    private void checkPhoneVerificationStatus() {
        if (isGoogleSignedIn()) {
            verifyPhoneButton.setVisibility(View.GONE);
            return;
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean isPhoneVerified = Boolean.TRUE.equals(documentSnapshot.getBoolean("isPhoneVerified"));
                        if (isPhoneVerified) {
                            verifyPhoneButton.setVisibility(View.GONE); // Hide the button if verified
                        } else {
                            verifyPhoneButton.setVisibility(View.VISIBLE); // Show the button if not verified
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to check phone verification status", Toast.LENGTH_SHORT).show();
                });
    }



    private void sendOTPForPhoneVerification() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false); // Prevent the user from dismissing the dialog
        progressDialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            progressDialog.dismiss();
            return;
        }

        // Fetch the registered phone number from Firestore
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String phoneNumber = documentSnapshot.getString("phoneNumber");
                        if (phoneNumber != null && !phoneNumber.isEmpty()) {
                            // Send OTP to the registered phone number
                            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                    phoneNumber,
                                    60, // Timeout duration
                                    TimeUnit.SECONDS,
                                    this,
                                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                        @Override
                                        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                            progressDialog.dismiss();
                                            verifyPhoneNumberWithOTP(credential.getSmsCode());
                                        }

                                        @Override
                                        public void onVerificationFailed(@NonNull FirebaseException e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(MyAccount.this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                            progressDialog.dismiss();
                                            MyAccount.this.verificationId = verificationId;
                                            showOTPDialogForPhoneVerification();
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(this, "No registered phone number found", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to fetch phone number", Toast.LENGTH_SHORT).show();
                });
    }


    private void showOTPDialogForPhoneVerification() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_otp_phone_verification, null);
        builder.setView(dialogView);

        // Initialize views
        EditText otpInput = dialogView.findViewById(R.id.otpInput);
        Button verifyButton = dialogView.findViewById(R.id.verifyButton);
        Button resendButton = dialogView.findViewById(R.id.resendButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        TextView timerText = dialogView.findViewById(R.id.timerText);

        // Create the dialog
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // Prevent dialog from being dismissed by tapping outside
        dialog.setCanceledOnTouchOutside(false); // Prevent dialog from being dismissed by tapping outside
        dialog.show();

        // Start the timer
        startOTPTimer(timerText, resendButton);

        // Set the Verify button click listener
        verifyButton.setOnClickListener(v -> {
            String otpCode = otpInput.getText().toString().trim();
            if (!otpCode.isEmpty() && otpCode.length() == 6) {
                verifyPhoneNumberWithOTP(otpCode);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            }
        });

        // Set the Resend button click listener
        resendButton.setOnClickListener(v -> {
            sendOTPForPhoneVerification(); // Resend OTP
            startOTPTimer(timerText, resendButton); // Restart the timer
        });

        // Set the Cancel button click listener
        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void startOTPTimer(TextView timerText, Button resendButton) {
        final int[] timeLeft = {60}; // 60 seconds
        timerText.setVisibility(View.VISIBLE);
        resendButton.setEnabled(false);

        CountDownTimer countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText("Resend OTP in " + timeLeft[0] + " seconds");
                timeLeft[0]--;
            }

            @Override
            public void onFinish() {
                timerText.setVisibility(View.GONE);
                resendButton.setEnabled(true);
            }
        }.start();
    }


    private void verifyPhoneNumberWithOTP(String otpCode) {
        if (verificationId == null || otpCode.isEmpty() || otpCode.length() != 6) {
            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpCode);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // First try to link the credential
        user.linkWithCredential(credential)
                .addOnSuccessListener(aVoid -> {
                    // Successfully linked - update verification status
                    updatePhoneVerificationStatus(true);
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        // Phone number already linked to another account
                        // First unlink any existing phone provider
                        unlinkExistingPhoneProvider(user, credential);
                    } else {
                        Toast.makeText(this, "OTP verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void unlinkExistingPhoneProvider(FirebaseUser user, PhoneAuthCredential credential) {
        // Find and unlink any existing phone provider
        for (UserInfo userInfo : user.getProviderData()) {
            if (userInfo.getProviderId().equals("phone")) {
                user.unlink(userInfo.getProviderId())
                        .addOnSuccessListener(aVoid -> {
                            // Now try linking again
                            linkPhoneCredential(user, credential);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to unlink existing phone number", Toast.LENGTH_SHORT).show();
                        });
                return;
            }
        }

        // If no phone provider found, try linking directly
        linkPhoneCredential(user, credential);
    }

    private void linkPhoneCredential(FirebaseUser user, PhoneAuthCredential credential) {
        user.linkWithCredential(credential)
                .addOnSuccessListener(aVoid -> {
                    updatePhoneVerificationStatus(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to verify phone number: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updatePhoneVerificationStatus(boolean isVerified) {
        FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .update("isPhoneVerified", isVerified)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Phone number verified!", Toast.LENGTH_SHORT).show();
                    verifyPhoneButton.setVisibility(View.GONE);
                    checkPhoneVerificationStatus();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update verification status", Toast.LENGTH_SHORT).show();
                });
    }



    //google signed in
    private boolean isGoogleSignedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (UserInfo userInfo : user.getProviderData()) {
                if (userInfo.getProviderId().equals("google.com")) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkAuthProvider() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Check if user has email/password provider (manual registration)
        boolean hasEmailPassword = false;
        for (UserInfo userInfo : user.getProviderData()) {
            if (userInfo.getProviderId().equals("password")) {
                hasEmailPassword = true;
                break;
            }
        }

        // Check if user has Google provider
        boolean hasGoogle = false;
        for (UserInfo userInfo : user.getProviderData()) {
            if (userInfo.getProviderId().equals("google.com")) {
                hasGoogle = true;
                break;
            }
        }

        if (hasEmailPassword) {
            // User registered manually (with or without Google) - allow editing
            enableEditingUI();
        } else if (hasGoogle) {
            // Check if this Google account email exists in Firestore as a manually registered user
            checkIfGoogleEmailWasManuallyRegistered(user.getEmail());
        }
    }

    private void checkIfGoogleEmailWasManuallyRegistered(String email) {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // This email was manually registered - treat as merged account
                        enableEditingUI();
                    } else {
                        // Pure Google sign-in - disable editing
                        disableEditingUI();
                    }
                });
    }
    //WALA PANG NABAGO

    private void enableEditingUI() {
        findViewById(R.id.username_edit).setVisibility(View.VISIBLE);
        findViewById(R.id.email_edit).setVisibility(View.VISIBLE);
        findViewById(R.id.password_edit).setVisibility(View.VISIBLE);
        findViewById(R.id.phone_edit).setVisibility(View.VISIBLE);
        findViewById(R.id.googleSignInMessage).setVisibility(View.GONE);
        switch2FA.setVisibility(View.VISIBLE);

        showPasswordSection();
        showPhoneSection();
    }

    private void disableEditingUI() {
        // This is now handled by setupForGoogleUser()
        findViewById(R.id.account_edit_sections).setVisibility(View.GONE);
        googleSignInMessage.setVisibility(View.VISIBLE);
        findViewById(R.id.manage_google).setVisibility(View.VISIBLE);
        findViewById(R.id.change_dp).setVisibility(View.GONE);
        switch2FA.setVisibility(View.GONE);
    }


    private void hidePasswordSection() {
        // Hide password label and icon
        findViewById(R.id.textView39).setVisibility(View.GONE); // "Password" text
        findViewById(R.id.image4).setVisibility(View.GONE);     // Password icon
        findViewById(R.id.password_edit).setVisibility(View.GONE); // Edit button
        findViewById(R.id.imageView21).setVisibility(View.GONE);

        // Hide verify button if it exists in this section
        findViewById(R.id.verifyPhoneButton).setVisibility(View.GONE);
    }

    private void hidePhoneSection() {
        // Hide phone label and icon
        findViewById(R.id.textView43).setVisibility(View.GONE); // "Phone Number" text
        findViewById(R.id.image5).setVisibility(View.GONE);     // Phone icon
        findViewById(R.id.phone_edit).setVisibility(View.GONE); // Edit button
        findViewById(R.id.verifyPhoneButton).setVisibility(View.GONE); // Verify button
        findViewById(R.id.imageView22).setVisibility(View.GONE);

        // Hide the phone number display text
        findViewById(R.id.textView41).setVisibility(View.GONE);

        // Hide verify button if it exists in this section
        findViewById(R.id.verifyPhoneButton).setVisibility(View.GONE);

    }


    // Replace the Consumer-based implementation with this interface approach
    private interface AccountLinkCallback {
        void onResult(boolean hasManualRegistration);
    }

    private void checkIfAccountIsLinked(AccountLinkCallback callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callback.onResult(false);
            return;
        }

        // Check if user has any provider other than Google
        for (UserInfo userInfo : user.getProviderData()) {
            if (userInfo.getProviderId().equals("password")) {
                callback.onResult(true);
                return;
            }
        }
        callback.onResult(false);
    }

    // Then modify your resetUIState method to use this interface
    private void resetUIState() {
        if (isGoogleSignedIn()) {
            // Pure Google account - show minimal UI
            disableEditingUI();

            // Show Google-specific UI
            googleSignInMessage.setVisibility(View.VISIBLE);
            findViewById(R.id.manage_google).setVisibility(View.VISIBLE);

            // Keep profile image change visible
            findViewById(R.id.change_dp).setVisibility(View.GONE);

            // Hide all editable sections
            findViewById(R.id.username_edit).setVisibility(View.GONE);
            findViewById(R.id.email_edit).setVisibility(View.GONE);
            findViewById(R.id.password_edit).setVisibility(View.GONE);
            findViewById(R.id.phone_edit).setVisibility(View.GONE);

            // Hide verify phone button
            verifyPhoneButton.setVisibility(View.GONE);

            // Hide 2FA switch
            switch2FA.setVisibility(View.GONE);
        } else {
            // Regular email/password user
            enableEditingUI();
            googleSignInMessage.setVisibility(View.GONE);
            findViewById(R.id.manage_google).setVisibility(View.GONE);
        }
    }

    private void setupGoogleAccountManagement() {
        TextView googleMessage = findViewById(R.id.googleSignInMessage);
        MaterialButton manageAccountButton = findViewById(R.id.manage_google);

        googleMessage.setVisibility(View.VISIBLE);
        manageAccountButton.setVisibility(View.VISIBLE);
        findViewById(R.id.change_dp).setVisibility(View.GONE);

        manageAccountButton.setOnClickListener(v -> {
            openGoogleAccountPersonalInfo();
        });
    }

    private void openGoogleAccountPersonalInfo() {
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleAccount != null) {
            String url = "https://myaccount.google.com/personal-info?authuser=" +
                    Uri.encode(googleAccount.getEmail());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {  // Changed from ActivityException to ActivityNotFoundException
                Toast.makeText(this, "No browser available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showPasswordSection() {
        findViewById(R.id.textView39).setVisibility(View.VISIBLE); // "Password" text
        findViewById(R.id.image4).setVisibility(View.VISIBLE);    // Password icon
        findViewById(R.id.password_edit).setVisibility(View.VISIBLE);
        findViewById(R.id.imageView21).setVisibility(View.VISIBLE);
    }

    private void showPhoneSection() {
        findViewById(R.id.textView43).setVisibility(View.VISIBLE); // "Phone Number" text
        findViewById(R.id.image5).setVisibility(View.VISIBLE);     // Phone icon
        findViewById(R.id.phone_edit).setVisibility(View.VISIBLE);
        findViewById(R.id.imageView22).setVisibility(View.VISIBLE);
        findViewById(R.id.textView41).setVisibility(View.VISIBLE); // Phone number display
    }

    private void setupForGoogleUser() {
        // Hide the entire edit sections container
        findViewById(R.id.account_edit_sections).setVisibility(View.GONE);

        // Show the Google message
        googleSignInMessage.setVisibility(View.VISIBLE);
        findViewById(R.id.change_dp).setVisibility(View.GONE);

        // Show and setup the "Manage Google Account" button
        MaterialButton manageAccountButton = findViewById(R.id.manage_google);
        manageAccountButton.setVisibility(View.VISIBLE);
        manageAccountButton.setOnClickListener(v -> openGoogleAccountPersonalInfo());

        // Keep profile image change visible
        findViewById(R.id.change_dp).setVisibility(View.VISIBLE);

        // Hide 2FA switch
        switch2FA.setVisibility(View.GONE);

        // Update account info
        updateAccountTextViews();
        updateSidebarInfo();
    }
}