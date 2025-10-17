package com.example.soilmate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Signup extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPassword, editTextConfirmPassword, editTextContact;
    private TextView errorUsername, errorEmail, errorPassword, errorConfirmPassword, errorContact;
    private Button buttonRegister, btnOTP;

    private Spinner spinnerCountryCode;

    private boolean isPasswordVisible = false, isConfirmPasswordVisible = false;
    private boolean isOTPDialogShowing = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String verificationId;

    private boolean isOTPVerified = false; // Add this flag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);




        TextView termsConditions = findViewById(R.id.TermsConditions);
        CheckBox checkBoxTerms = findViewById(R.id.CheckBoxTerms);

        termsConditions.setOnClickListener(v -> showTermsDialog(checkBoxTerms));

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextContact = findViewById(R.id.editTextContact);
        errorContact = findViewById(R.id.errorContact);
        btnOTP = findViewById(R.id.btnOTP);
        buttonRegister = findViewById(R.id.buttonRegister);
        spinnerCountryCode = findViewById(R.id.spinnerCountryCode);

        errorUsername = findViewById(R.id.errorUsername);
        errorEmail = findViewById(R.id.errorEmail);
        errorPassword = findViewById(R.id.errorPassword);
        errorConfirmPassword = findViewById(R.id.errorConfirmPassword);


        // Set up the country code spinner
        setupCountryCodeSpinner();

        // Set touch listeners for toggling password visibility
        setTogglePasswordVisibility(editTextPassword);
        setTogglePasswordVisibility(editTextConfirmPassword);

        addTextWatcher(editTextUsername, errorUsername);
        addTextWatcher(editTextEmail, errorEmail);
        addTextWatcher(editTextPassword, errorPassword);
        addTextWatcher(editTextConfirmPassword, errorConfirmPassword);

        // Set click listener for OTP button
        btnOTP.setOnClickListener(v -> sendVerificationCode());

        // Set click listener for Register button
        buttonRegister.setOnClickListener(v -> validateForm());

        // Login
        TextView loginTextView = findViewById(R.id.login);
        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Signup.this, Login.class);
            startActivity(intent);
        });


        // Add TextWatcher for live validation
        editTextContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();

                // Validate phone number length based on the selected country code
                String countryCode = spinnerCountryCode.getSelectedItem().toString();
                int expectedLength = getExpectedPhoneNumberLength(countryCode);

                if (input.length() < expectedLength) {
                    errorContact.setText("Phone number must be " + (expectedLength) + " digits");
                    errorContact.setVisibility(View.VISIBLE);
                } else {
                    errorContact.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme);
    }



    //COUNTRY CODE NUMBER LENGTH
    private int getExpectedPhoneNumberLength(String countryCode) {
        switch (countryCode) {
            case "+63": // Philippines
                return 10;
            case "+82": // South Korea
                return 9;
            case "+86": // China
                return 11;
            case "+91": // India
                return 10;
            case "+98": // Iran
                return 10;
            case "+84": // Vietnam
                return 9;
            case "+81": // Japan
                return 10;
            case "+65": // Singapore
                return 8;
            case "+66": // Thailand
                return 9;
            case "+92": // Pakistan
                return 10;
            default:
                return 10;
        }
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

    private void sendVerificationCode() {
        String phoneNumber = editTextContact.getText().toString().trim();

        // Check if the phone number starts with "+63" and has 10 digits after it
        if (phoneNumber.isEmpty() || !phoneNumber.startsWith("+63")) {
            editTextContact.setError("Enter a valid phone number starting with +63");
            editTextContact.requestFocus();
            return;
        }

        // Ensure the phone number is exactly 13 characters long (+63 + 10 digits)
        if (phoneNumber.length() != 13) {
            editTextContact.setError("Enter a valid 10-digit phone number yes and ?after +63");
            editTextContact.requestFocus();
            return;
        }

        // Show a progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending verification code...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Send verification code
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Use the formatted phone number directly
                60, // Timeout duration
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        progressDialog.dismiss();
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        progressDialog.dismiss();
                        Toast.makeText(Signup.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        progressDialog.dismiss();
                        verificationId = s;
                        showOTPDialog();
                    }
                });
    }

    private void showOTPDialog() {
        isOTPDialogShowing = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter OTP");

        // Include the formatted phone number in the dialog message
        builder.setMessage("We have sent an OTP to " + editTextContact.getText().toString().trim());

        final EditText otpInput = new EditText(this);
        otpInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(otpInput);

        builder.setPositiveButton("Verify", (dialog, which) -> {
            String code = otpInput.getText().toString().trim();
            if (code.isEmpty() || code.length() < 6) {
                otpInput.setError("Enter valid code");
                otpInput.requestFocus();
            } else {
                verifyCode(code);
            }
        });

        builder.setNegativeButton("Resend", (dialog, which) -> {
            // Reset the OTP verification flag and disable the Register button
            isOTPVerified = false;
            buttonRegister.setEnabled(false);
            sendVerificationCode();
        });

        builder.setCancelable(false); // Prevent dialog from being dismissed by tapping outside

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // OTP verification successful
                        isOTPDialogShowing = false;
                        isOTPVerified = true; // Set the flag to true
                        Toast.makeText(Signup.this, "OTP verified successfully", Toast.LENGTH_SHORT).show();

                        // Enable the Register button
                        buttonRegister.setEnabled(true);
                    } else {
                        // OTP verification failed
                        Toast.makeText(Signup.this, "OTP verification failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        // Do nothing if the OTP dialog is showing
        if (isOTPDialogShowing) {
            return;
        }
        super.onBackPressed();
    }

    private void validateForm() {
        boolean isValid = true;

        // Reset errors
        errorUsername.setVisibility(View.GONE);
        errorEmail.setVisibility(View.GONE);
        errorPassword.setVisibility(View.GONE);
        errorConfirmPassword.setVisibility(View.GONE);

        // Get input values
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String countryCode = spinnerCountryCode.getSelectedItem().toString();
        String phoneNumber = editTextContact.getText().toString().trim();

        // Check if the contact number is filled in
        // Validate phone number length based on the selected country code
        int expectedLength = getExpectedPhoneNumberLength(countryCode);
        if (TextUtils.isEmpty(phoneNumber)) {
            editTextContact.setError("Phone number is required");
            editTextContact.requestFocus();
            isValid = false;
        } else if (phoneNumber.length() != expectedLength) {
            editTextContact.setError("Enter a valid " + expectedLength + "-digit phone number");
            editTextContact.requestFocus();
            isValid = false;
        }

        // Find the Terms and Conditions checkbox
        CheckBox checkBoxTerms = findViewById(R.id.CheckBoxTerms);

        // Username validation
        if (TextUtils.isEmpty(username)) {
            errorUsername.setText("Username is required");
            errorUsername.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (username.length() > 25) {
            errorUsername.setText("Username cannot exceed 25 characters");
            errorUsername.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Email validation
        if (TextUtils.isEmpty(email)) {
            errorEmail.setText("Email is required");
            errorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorEmail.setText("Enter a valid email");
            errorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Password validation
        if (TextUtils.isEmpty(password)) {
            errorPassword.setText("Password is required");
            errorPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (password.length() < 8) {
            errorPassword.setText("Password must be at least 8 characters");
            errorPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!password.matches(".*[A-Z].*")) {
            errorPassword.setText("Must have at least 1 capital letter");
            errorPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!password.matches(".*[0-9].*")) {
            errorPassword.setText("Must have at least 1 number");
            errorPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!password.matches(".*[!@#$%^&+=?-].*")) {
            errorPassword.setText("Must have at least 1 special character");
            errorPassword.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Confirm Password validation
        if (TextUtils.isEmpty(confirmPassword)) {
            errorConfirmPassword.setText("Confirm Password is required");
            errorConfirmPassword.setVisibility(View.VISIBLE);
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            errorConfirmPassword.setText("Passwords do not match");
            errorConfirmPassword.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Terms and Conditions validation
        if (!checkBoxTerms.isChecked()) {
            Toast.makeText(this, "You must agree to the Terms and Conditions", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        if (isValid) {
            toggleLoading(true);
            checkUsernameAndEmail(username, email, password);
        }
    }

    private void showTermsDialog(CheckBox checkBox) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_terms_conditions, null);
        builder.setView(dialogView);

        // Initialize views
        TextView termsText = dialogView.findViewById(R.id.termsText);
        Button agreeButton = dialogView.findViewById(R.id.agreeButton);

        // Set the terms and conditions text
        termsText.setText(getString(R.string.terms_and_conditions));

        // Create the dialog
        AlertDialog dialog = builder.create();

        // Disable the Agree button initially
        agreeButton.setEnabled(false);

        // Add a scroll listener to enable the Agree button when the user scrolls to the bottom
        ScrollView scrollView = dialogView.findViewById(R.id.scrollView);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            if (!scrollView.canScrollVertically(1)) { // 1 means scrolling down
                agreeButton.setEnabled(true); // Enable the button when scrolled to the bottom
            }
        });

        // Set the Agree button click listener
        agreeButton.setOnClickListener(v -> {
            checkBox.setChecked(true);
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();

        // Adjust dialog size to prevent full-screen coverage
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void setTogglePasswordVisibility(EditText editText) {
        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable endDrawable = editText.getCompoundDrawables()[2]; // Get drawableEnd
                if (endDrawable != null && event.getRawX() >= (editText.getRight() - endDrawable.getBounds().width())) {
                    if (editText.getTransformationMethod() instanceof PasswordTransformationMethod) {
                        editText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ass, 0, R.drawable.ic_hide_pass, 0);
                    } else {
                        editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ass, 0, R.drawable.hide, 0);
                    }
                    editText.setSelection(editText.getText().length()); // Move cursor to end
                    return true;
                }
            }
            return false;
        });
    }

    private void addTextWatcher(EditText editText, TextView errorTextView) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();

                if (editText == editTextUsername) {
                    if (input.isEmpty()) {
                        errorTextView.setText("Username is required");
                        errorTextView.setVisibility(View.VISIBLE);
                    } else if (input.length() > 25) {
                        errorTextView.setText("Username must not exceed 25 characters");
                        errorTextView.setVisibility(View.VISIBLE);
                    } else {
                        errorTextView.setVisibility(View.GONE);
                    }
                } else if (editText == editTextEmail) {
                    if (input.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                        errorTextView.setText("Enter a valid email");
                        errorTextView.setVisibility(View.VISIBLE);
                    } else {
                        errorTextView.setVisibility(View.GONE);
                    }
                } else if (editText == editTextContact) {
                    // Automatically add "+63" if the user starts typing without it
                    if (!input.startsWith("+63")) {
                        editTextContact.setText("+63");
                        editTextContact.setSelection(3); // Move cursor to the end
                        return;
                    }

                    // Limit input to 13 characters (+63 + 10 digits)
                    if (input.length() > 13) {
                        editTextContact.setText(input.substring(0, 13)); // Truncate to 13 characters
                        editTextContact.setSelection(13); // Move cursor to the end
                    }

                    // Validate phone number length
                    if (input.length() < 13) {
                        errorTextView.setText("Phone number must be 10 digits yes and? after +63");
                        errorTextView.setVisibility(View.VISIBLE);
                    } else {
                        errorTextView.setVisibility(View.GONE);
                    }
                } else if (editText == editTextPassword) {
                    if (input.isEmpty()) {
                        errorTextView.setText("Password is required");
                        errorTextView.setVisibility(View.VISIBLE);
                    } else if (input.length() < 8) {
                        errorTextView.setText("Password must be at least 8 characters");
                        errorTextView.setVisibility(View.VISIBLE);
                    } else if (!input.matches(".*[A-Z].*")) {
                        errorTextView.setText("Must have at least 1 capital letter");
                        errorTextView.setVisibility(View.VISIBLE);
                    } else if (!input.matches(".*[0-9].*")) {
                        errorTextView.setText("Must have at least 1 number");
                        errorTextView.setVisibility(View.VISIBLE);
                    } else if (!input.matches(".*[!@#$%^&_*+=?-].*")) {
                        errorTextView.setText("Must have at least 1 special character");
                        errorTextView.setVisibility(View.VISIBLE);
                    } else {
                        errorTextView.setVisibility(View.GONE);
                    }
                } else if (editText == editTextConfirmPassword) {
                    if (input.isEmpty()) {
                        errorTextView.setText("Confirm Password is required");
                        errorTextView.setVisibility(View.VISIBLE);
                    } else if (!input.equals(editTextPassword.getText().toString().trim())) {
                        errorTextView.setText("Passwords do not match");
                        errorTextView.setVisibility(View.VISIBLE);
                    } else {
                        errorTextView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void registerUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), email, username);
                        }
                    } else {
                        toggleLoading(false);
                        errorEmail.setText("Registration failed: " + task.getException().getMessage());
                        errorEmail.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void saveUserToFirestore(String userId, String email, String username) {
        Country selectedCountry = (Country) spinnerCountryCode.getSelectedItem();
        String countryCode = selectedCountry.getCode(); // Get the country code from the selected Country object
        String phoneNumber = countryCode + editTextContact.getText().toString().trim();

        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("phoneNumber", phoneNumber);
        user.put("userId", userId);

        db.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    toggleLoading(false);
                    Intent intent = new Intent(Signup.this, VerificationSent.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    toggleLoading(false);
                    errorEmail.setText("Error saving user data: " + e.getMessage());
                    errorEmail.setVisibility(View.VISIBLE);
                });
    }

    private void checkUsernameAndEmail(String username, String email, String password) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            toggleLoading(false);
                            errorUsername.setText("Username is already taken");
                            errorUsername.setVisibility(View.VISIBLE);
                        } else {
                            checkEmailExists(username, email, password);
                        }
                    } else {
                        toggleLoading(false);
                        Exception e = task.getException();
                        Toast.makeText(Signup.this, "Error: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                    }
                });

    }


    private void checkEmailExists(String username, String email, String password) {
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().getSignInMethods() != null &&
                    !task.getResult().getSignInMethods().isEmpty()) {
                toggleLoading(false);
                errorEmail.setText("Email is already registered");
                errorEmail.setVisibility(View.VISIBLE);
            } else {
                registerUser(email, password, username);
            }
        });
    }

    private void toggleLoading(boolean isLoading) {
        if (isLoading) {
            buttonRegister.setVisibility(View.GONE);
            findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
        } else {
            buttonRegister.setVisibility(View.VISIBLE);
            findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }
}