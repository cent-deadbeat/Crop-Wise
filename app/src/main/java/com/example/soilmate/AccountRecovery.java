package com.example.soilmate;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;


public class AccountRecovery extends AppCompatActivity {

    private EditText editTextEmail;
    private TextView errorEmail, backlogin;
    private Button sendButton;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_recovery);

        //firebase
        auth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.email);
        errorEmail = findViewById(R.id.errorEmail);
        sendButton = findViewById(R.id.send);

        // Live Validation
        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Send button
        sendButton.setOnClickListener(v -> {
            if (validateEmail()) {
                sendPasswordResetEmail();
            }
        });




    }

    private boolean validateEmail() {
        String email = editTextEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            errorEmail.setText("Email is required");
            errorEmail.setVisibility(View.VISIBLE);
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorEmail.setText("Enter a valid email");
            errorEmail.setVisibility(View.VISIBLE);
            return false;
        } else {
            errorEmail.setVisibility(View.GONE);
            return true;
        }
    }

    private void sendPasswordResetEmail() {
        String email = editTextEmail.getText().toString().trim().toLowerCase();

        toggleLoading(true);

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    toggleLoading(false);

                    if (task.isSuccessful()) {

                        Intent intent = new Intent(AccountRecovery.this, RecoverySent.class);
                        startActivity(intent);
                        finish();
                    } else {

                        Exception e = task.getException();
                        String errorMessage = (e != null) ? e.getMessage() : "Failed to send reset email.";

                        errorEmail.setText(errorMessage);
                        errorEmail.setVisibility(View.VISIBLE);
                    }
                });
    }






    private void toggleLoading(boolean isLoading) {
        if (isLoading) {
            sendButton.setVisibility(View.GONE);
            findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        } else {
            sendButton.setVisibility(View.VISIBLE);
            findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
        }
    }


}