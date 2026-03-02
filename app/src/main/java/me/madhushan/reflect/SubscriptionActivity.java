package me.madhushan.reflect;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SubscriptionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        LinearLayout btnUpgrade = findViewById(R.id.btn_upgrade);
        btnUpgrade.setOnClickListener(v ->
                Toast.makeText(this, "In-app purchases coming soon!", Toast.LENGTH_SHORT).show());

        findViewById(R.id.tv_restore).setOnClickListener(v ->
                Toast.makeText(this, "No purchases to restore.", Toast.LENGTH_SHORT).show());

        // Pro card tap
        LinearLayout cardPro = findViewById(R.id.card_pro);
        cardPro.setOnClickListener(v ->
                Toast.makeText(this, "Tap 'Upgrade to Pro' to subscribe.", Toast.LENGTH_SHORT).show());
    }
}

