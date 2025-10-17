package com.example.soilmate;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangeEmailVerifSent extends AppCompatActivity {

    private FirebaseUser user;
    private ImageView verifiedIcon, mailIcon;
    private TextView messageText;
    private Button resendButton;
    private String newEmail;
    private Handler handler = new Handler();
    private CountDownTimer resendTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email_verif_sent);

        user = FirebaseAuth.getInstance().getCurrentUser();
        verifiedIcon = findViewById(R.id.verifiedIcon);
        mailIcon = findViewById(R.id.imageView8);
        messageText = findViewById(R.id.message);
        resendButton = findViewById(R.id.resendEmail);

        // Initial UI state
        verifiedIcon.setVisibility(View.GONE);
        mailIcon.setVisibility(View.VISIBLE);

        newEmail = getIntent().getStringExtra("newEmail");
        if (newEmail == null) {
            finish();
            return;
        }

        messageText.setText("Verification sent to " + newEmail);
        setupResendButton();
        startVerificationCheck();
    }

    private void setupResendButton() {
        startResendCountdown();
        resendButton.setOnClickListener(v -> {
            resendButton.setEnabled(false);
            resendVerification();
        });
    }

    private void startResendCountdown() {
        resendTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                resendButton.setText("Resend in " + millisUntilFinished / 1000 + "s");
            }
            public void onFinish() {
                resendButton.setText("Resend Email");
                resendButton.setEnabled(true);
            }
        }.start();
    }
    //working code

    private void resendVerification() {
        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://soilmate-1b770.web.app/verify-success.html")
                .setHandleCodeInApp(false)
                .build();

        user.verifyBeforeUpdateEmail(newEmail, actionCodeSettings)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Verification email resent", Toast.LENGTH_SHORT).show();
                        startResendCountdown();
                    } else {
                        resendButton.setEnabled(true);
                        Toast.makeText(this, "Failed to resend", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startVerificationCheck() {
        // No need to run it immediately. We'll check onResume instead.
    }


    @Override
    protected void onResume() {
        super.onResume();

        startEmailVerificationCheck(); // Continuously check if email is verified
    }

    private void startEmailVerificationCheck() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = auth.getCurrentUser();

                if (currentUser == null) {
                    Log.d("EmailChange", "User is null. Prompting re-login...");

                    // Stop checking and show the login screen
                    runOnUiThread(() -> promptReLogin());
                    return;
                }

                // Reload user data to get the latest email verification status
                currentUser.reload().addOnCompleteListener(task -> {
                    FirebaseUser updatedUser = FirebaseAuth.getInstance().getCurrentUser();

                    if (updatedUser != null && updatedUser.isEmailVerified() &&
                            updatedUser.getEmail() != null &&
                            updatedUser.getEmail().equalsIgnoreCase(newEmail)) {

                        Log.d("EmailChange", "Email verified, showing dialog.");
                        runOnUiThread(() -> showVerifiedState());
                    } else {
                        // Keep checking every 3 seconds if email is not verified yet
                        handler.postDelayed(this, 3000);
                    }
                });
            }
        }, 3000);
    }


    private void promptReLogin() {
        new AlertDialog.Builder(this)
                .setTitle("Session Expired")
                .setMessage("Your session has expired due to an email change. Please log in again.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(ChangeEmailVerifSent.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }



    private void showVerifiedState() {
        Log.d("EmailChange", "Email successfully changed to: " + newEmail);

        runOnUiThread(() -> {
            verifiedIcon.setVisibility(View.VISIBLE);
            mailIcon.setVisibility(View.GONE);
            resendButton.setVisibility(View.GONE);
            messageText.setText("Email changed to " + newEmail);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Success")
                    .setMessage("Please log in again")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> {
                        dialog.dismiss();

                        handler.postDelayed(() -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(ChangeEmailVerifSent.this, Login.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }, 500);
                    });

            AlertDialog alertDialog = builder.create();

            if (!isFinishing() && !isDestroyed()) {
                alertDialog.show();
            } else {
                Log.e("EmailChange", "Activity is finishing, dialog not shown.");
            }
        });
    }






    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        super.onDestroy();
    }
}