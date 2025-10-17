package com.example.soilmate;

import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class RecoverySent extends AppCompatActivity {
    private TextView backlogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery_sent);

        backlogin = findViewById(R.id.backlogin);

        backlogin.setOnClickListener(v -> finish());
    }
}