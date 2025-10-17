package com.example.soilmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class FAQDetails extends AppCompatActivity {
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faqdetails);

        // Sidebarmenu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        ImageButton menuButton = findViewById(R.id.menubtn);
        menuButton.setOnClickListener(view -> drawerLayout.openDrawer(navigationView));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.nav_plants:
                        startActivity(new Intent(FAQDetails.this, MyPlants.class));
                        break;
                    case R.id.nav_my_acc:
                        startActivity(new Intent(FAQDetails.this, Login.class));
                        break;
                    case R.id.nav_guide:
                        startActivity(new Intent(FAQDetails.this, UserGuide.class));
                        break;
                    case R.id.nav_help:
                        startActivity(new Intent(FAQDetails.this, MainActivity.class));
                        break;
                    case R.id.nav_logout:
                        startActivity(new Intent(FAQDetails.this, Login.class));
                        finish();
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });

        TextView questionTextView = findViewById(R.id.question_title);
        TextView answerTextView = findViewById(R.id.answer);

        // Get data from intent
        Intent intent = getIntent();
        String question = intent.getStringExtra("question");
        String answer = intent.getStringExtra("answer");

        // Set data
        questionTextView.setText(question);
        answerTextView.setText(answer);

        ImageButton backButton = findViewById(R.id.back);
        backButton.setOnClickListener(v -> finish());

    }
}
