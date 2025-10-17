package com.example.soilmate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    private ImageView logo;
    private TextView title;
    private String fullText = "SoilMate"; // Full title text
    private int index = 0; // Track position of letters
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);
        title = findViewById(R.id.title);

        // Delay the logo animation by 500ms
        handler.postDelayed(() -> {
            Animation moveLeft = AnimationUtils.loadAnimation(this, R.anim.move_left);
            logo.startAnimation(moveLeft);

            // When logo animation ends, start text animation
            moveLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    title.setVisibility(View.VISIBLE);
                    title.setText(""); // Start empty
                    index = 0;
                    handler.postDelayed(letterByLetter, 100); // Start letter animation

                    // Delay before switching to next activity
                    handler.postDelayed(() -> {
                        if (isUserLoggedIn()) {
                            startActivity(new Intent(Splash.this, MyPlants.class));
                        } else {
                            startActivity(new Intent(Splash.this, Login.class));
                        }
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    }, 1000); // Delay before transition
                }

                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }, 500); // Half-second delay before logo animation starts
    }

    // Runnable to reveal text letter by letter
    private Runnable letterByLetter = new Runnable() {
        @Override
        public void run() {
            if (index < fullText.length()) {
                title.append(String.valueOf(fullText.charAt(index))); // Add letter
                index++;
                handler.postDelayed(this, 40); // Delay between letters
            }
        }
    };

    // Check if user is logged in
    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        return sharedPreferences.getBoolean("isLoggedIn", false);
    }
}
