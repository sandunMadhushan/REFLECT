
package com.madhushan.reflect;

import static me.madhushan.reflect.R.*;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import me.madhushan.reflect.R;

public class visionboard extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visionboard);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            // Handle navigation here
            return true;
        });
    }
}
