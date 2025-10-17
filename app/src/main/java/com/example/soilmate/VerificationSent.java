package com.example.soilmate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.appcompat.widget.AppCompatButton;
import com.google.firebase.functions.FirebaseFunctions;


public class VerificationSent extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextView messageTextView, backToLogin;
    private ImageView emailIcon, verifiedIcon;
    private AppCompatButton resendEmail;
    private Handler handler = new Handler();
    private int countdownTime = 60; // 60 seconds countdown

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_sent);


        auth = FirebaseAuth.getInstance();


        messageTextView = findViewById(R.id.message);
        emailIcon = findViewById(R.id.imageView8);
        verifiedIcon = findViewById(R.id.verifiedIcon);
        resendEmail = findViewById(R.id.resendEmail);


        verifiedIcon.setVisibility(View.GONE);

        startCountdown();

        resendEmail.setOnClickListener(v -> resendVerificationEmail());


        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        // Disable back button
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkEmailVerification();
    }

    private void checkEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        user.reload().addOnCompleteListener(task -> {
            if (user.isEmailVerified()) {
                // Email is verified
                messageTextView.setText("Email verified successfully!");
                emailIcon.setVisibility(View.GONE);
                verifiedIcon.setVisibility(View.VISIBLE);
                resendEmail.setVisibility(View.GONE);

                // Log out the user
                auth.signOut();
                Log.d("VerificationSent", "User logged out after email verification.");

                // Redirect to Login activity after 5 seconds
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(VerificationSent.this, Login.class);
                    startActivity(intent);
                    finish();
                }, 5000);
            } else {
                // Email is not verified, check again after 3 seconds
                new Handler().postDelayed(this::checkEmailVerification, 3000);
            }
        });
    }


    private void startCountdown() {
        resendEmail.setEnabled(false); // Disable button initially
        resendEmail.setText("Resend in 60s"); // Show countdown text

        new Thread(() -> {
            while (countdownTime > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countdownTime--;

                runOnUiThread(() -> {
                    if (countdownTime == 0) {
                        resendEmail.setEnabled(true); // Enable button after 60s
                        resendEmail.setVisibility(View.VISIBLE);
                        resendEmail.setText("Resend Email");
                    } else {
                        resendEmail.setText("Resend in " + countdownTime + "s");
                    }
                });
            }
        }).start();
    }

    private void resendVerificationEmail() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            FirebaseFunctions.getInstance()
                    .getHttpsCallable("resendVerificationEmail") // 🔥 Call Firebase Function
                    .call()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(VerificationSent.this, "Verification email resent!", Toast.LENGTH_SHORT).show();
                            resendEmail.setEnabled(false);
                            countdownTime = 60;
                            startCountdown();
                        } else {
                            Toast.makeText(VerificationSent.this, "Failed to resend email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}
