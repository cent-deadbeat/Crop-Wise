package com.example.soilmate;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private SpringDotsIndicator dotsIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);

        // List of slide layouts
        List<Integer> slideLayouts = Arrays.asList(
                R.layout.fragment_slide,
                R.layout.fragment_slide2
        );

        // Set up adapter
        SlideAdapter adapter = new SlideAdapter(slideLayouts, viewPager);
        viewPager.setAdapter(adapter);
        dotsIndicator.attachTo(viewPager);
    }
}

