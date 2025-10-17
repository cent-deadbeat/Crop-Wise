package com.example.soilmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.firebase.auth.*;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Login extends AppCompatActivity {
    private FirebaseAuth auth;
    private EditText editTextEmail, editTextPassword;
    private TextView errorEmail, errorPassword;
    private AppCompatButton buttonLogin, googleSignInButton;

    private GoogleSignInClient mGoogleSignInClient;

    // SharedPreferences keys for fraudulent activity check
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_FAILED_ATTEMPTS = "failed_attempts";
    private static final String KEY_LAST_FAILED_TIME = "last_failed_time";

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long BLOCK_DURATION = 5 * 60 * 1000; // 5 minutes

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            // ✅ Redirect logged-in users with verified email to MyPlants
            Log.d("Login", "User is logged in and email is verified. Redirecting to MyPlants.");
            startActivity(new Intent(Login.this, MyPlants.class));
            finish();
        } else {
            Log.d("Login", "User is not logged in or email is not verified. Staying on Login screen.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase Auth instance
        auth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.emailAddress);
        editTextPassword = findViewById(R.id.password);
        errorEmail = findViewById(R.id.errorEmail);
        errorPassword = findViewById(R.id.errorPassword);
        buttonLogin = findViewById(R.id.signin);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        configureGoogleSignIn();
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        // Set up text validation
        addTextWatcher(editTextEmail, errorEmail, true);
        addTextWatcher(editTextPassword, errorPassword, false);

        setTogglePasswordVisibility(editTextPassword);

        // Login Button Click
        buttonLogin.setOnClickListener(v -> validateForm());

        // Signup Link
        findViewById(R.id.signup).setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Signup.class));
        });

        // Forgot Password Link
        findViewById(R.id.forgot).setOnClickListener(v -> {
            startActivity(new Intent(Login.this, AccountRecovery.class));
        });
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .requestProfile()  // Add this to get profile info
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // Start Google Sign-In Flow
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("GoogleSignIn", "✅ Google sign-in successful: " + account.getEmail());
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.e("GoogleSignIn", "❌ Google Sign-In failed: " + e.getStatusCode());
                Toast.makeText(this, "Google Sign-In failed! Code: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        // First check if email exists with different provider
        auth.fetchSignInMethodsForEmail(acct.getEmail())
                .addOnCompleteListener(methodsTask -> {
                    if (methodsTask.isSuccessful()) {
                        List<String> signInMethods = methodsTask.getResult().getSignInMethods();

                        // Check if email exists with password provider
                        boolean hasPasswordProvider = signInMethods != null &&
                                signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD);

                        if (hasPasswordProvider) {
                            // Automatically link accounts without showing dialog
                            linkGoogleToExistingAccount(acct.getEmail(), credential);
                        } else {
                            // No existing email or only social providers - proceed with Google sign-in
                            proceedWithGoogleSignIn(credential, acct);
                        }
                    } else {
                        // Error checking sign-in methods
                        Toast.makeText(Login.this, "Error checking account", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void proceedWithGoogleSignIn(AuthCredential credential, GoogleSignInAccount acct) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // For pure Google sign-ins, mark email as verified immediately
                            if (!user.isEmailVerified()) {
                                // Update email verification status in Firebase
                                user.reload().addOnSuccessListener(aVoid -> {
                                    if (!user.isEmailVerified()) {
                                        // For Google accounts, we consider them verified by default
                                        // No need to send verification email
                                        checkUserStatusAndProceed(user.getUid(), user.getEmail(), acct.getDisplayName());
                                    }
                                });
                            } else {
                                checkUserStatusAndProceed(user.getUid(), user.getEmail(), acct.getDisplayName());
                            }
                        }
                    } else {
                        Toast.makeText(Login.this, "Google login failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void linkGoogleToExistingAccount(String email, AuthCredential googleCredential) {
        // First sign in anonymously
        auth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Now link with Google credential
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.linkWithCredential(googleCredential)
                                    .addOnCompleteListener(linkTask -> {
                                        if (linkTask.isSuccessful()) {
                                            // Successfully linked - proceed to app
                                            FirebaseUser linkedUser = auth.getCurrentUser();
                                            if (linkedUser != null) {
                                                checkUserStatusAndProceed(linkedUser.getUid(), linkedUser.getEmail(), linkedUser.getDisplayName());
                                            }
                                        } else {
                                            Toast.makeText(Login.this, "Failed to link accounts: " +
                                                            (linkTask.getException() != null ?
                                                                    linkTask.getException().getMessage() : "Unknown error"),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    }
                });
    }

    private void checkUserStatusAndProceed(String userId, String email, String fullName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // User exists - proceed to app
                startActivity(new Intent(Login.this, MyPlants.class));
                finish();
            } else {
                // New User - save to Firestore without email verification
                Map<String, Object> user = new HashMap<>();
                user.put("userId", userId);
                user.put("email", email);
                user.put("username", fullName);
                user.put("2FA_enabled", false);

                userRef.set(user)
                        .addOnSuccessListener(aVoid -> {
                            // Mark email as verified for Google accounts
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (currentUser != null && !currentUser.isEmailVerified()) {
                                currentUser.sendEmailVerification(); // Optional: Remove if you don't want any verification
                            }

                            startActivity(new Intent(Login.this, MyPlants.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(Login.this, "Failed to create user", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            mGoogleSignInClient.signOut();
                        });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(Login.this, "Error checking account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    // Validate Form and Login User
    private void validateForm() {
        boolean isValid = true;
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        errorEmail.setVisibility(View.GONE);
        errorPassword.setVisibility(View.GONE);

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorEmail.setText("Invalid email");
            errorEmail.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Password Validation
        if (TextUtils.isEmpty(password)) {
            errorPassword.setText("Invalid password");
            errorPassword.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (isValid) {
            if (isUserBlocked()) {
                Toast.makeText(this, "Too many failed attempts. Please try again later.", Toast.LENGTH_LONG).show();
            } else {
                toggleLoading(true);
                loginUser(email, password);
            }
        }
    }

    // Check if the user is blocked due to too many failed attempts
    private boolean isUserBlocked() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0);
        long lastFailedTime = prefs.getLong(KEY_LAST_FAILED_TIME, 0);

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFailedTime < BLOCK_DURATION) {
                return true; // User is blocked
            } else {
                // Reset failed attempts if the block duration has passed
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(KEY_FAILED_ATTEMPTS, 0);
                editor.apply();
            }
        }
        return false; // User is not blocked
    }

    // Login User with Firebase Authentication
    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    Log.d("LoginDebug", "User logged in: " + user.getUid());
                    if (user.isEmailVerified()) {
                        Log.d("LoginDebug", "User email verified. Redirecting to MyPlants.");
                        startActivity(new Intent(Login.this, MyPlants.class));
                        finish();
                    } else {
                        Log.d("LoginDebug", "User email NOT verified. Redirecting to VerificationSent.");
                        startActivity(new Intent(Login.this, VerificationSent.class));
                        finish();
                    }
                } else {
                    Log.e("LoginDebug", "User is NULL after successful login!");
                }
            } else {
                Log.e("LoginDebug", "Firebase authentication failed: " + task.getException());
                errorEmail.setText("Invalid credentials");
                errorPassword.setText("Invalid credentials");
                errorEmail.setVisibility(View.VISIBLE);
                errorPassword.setVisibility(View.VISIBLE);
                toggleLoading(false);

                // Increment failed attempts
                incrementFailedAttempts();
            }
        });
    }

    // Increment failed attempts and block user if necessary
    private void incrementFailedAttempts() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int failedAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1;
        long lastFailedTime = System.currentTimeMillis();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_FAILED_ATTEMPTS, failedAttempts);
        editor.putLong(KEY_LAST_FAILED_TIME, lastFailedTime);
        editor.apply();

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            Toast.makeText(this, "Too many failed attempts. Please try again later.", Toast.LENGTH_LONG).show();
        }
    }

    // Toggle Password Visibility
    private void setTogglePasswordVisibility(EditText passwordField) {
        passwordField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable endDrawable = passwordField.getCompoundDrawables()[2];
                if (endDrawable != null && event.getRawX() >= (passwordField.getRight() - endDrawable.getBounds().width())) {
                    if (passwordField.getTransformationMethod() instanceof PasswordTransformationMethod) {
                        passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ass, 0, R.drawable.ic_hide_pass, 0);
                    } else {
                        passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ass, 0, R.drawable.hide, 0);
                    }
                    passwordField.setSelection(passwordField.getText().length());
                    return true;
                }
            }
            return false;
        });
    }

    // Live validation for email and password fields
    private void addTextWatcher(EditText editText, TextView errorTextView, boolean isEmail) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString().trim();
                if (isEmail) {
                    if (input.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                        errorTextView.setText("Invalid email");
                        errorTextView.setVisibility(View.VISIBLE);
                    } else {
                        errorTextView.setVisibility(View.GONE);
                    }
                } else {
                    if (input.isEmpty()) {
                        errorTextView.setText("Password required");
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


    //progress bar
    private void toggleLoading(boolean isLoading) {
        if (isLoading) {
            buttonLogin.setVisibility(View.GONE);
            findViewById(R.id.loginProgressBar).setVisibility(View.VISIBLE);
        } else {
            buttonLogin.setVisibility(View.VISIBLE);
            findViewById(R.id.loginProgressBar).setVisibility(View.GONE);
        }
    }
}