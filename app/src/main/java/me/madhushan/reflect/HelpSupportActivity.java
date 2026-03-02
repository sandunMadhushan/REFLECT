package me.madhushan.reflect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HelpSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        setupFaqToggle(R.id.faq_1, R.id.faq_1_answer, R.id.faq_1_arrow, true);
        setupFaqToggle(R.id.faq_2, R.id.faq_2_answer, R.id.faq_2_arrow, false);
        setupFaqToggle(R.id.faq_3, R.id.faq_3_answer, R.id.faq_3_arrow, false);
        setupFaqToggle(R.id.faq_4, R.id.faq_4_answer, R.id.faq_4_arrow, false);

        // Email Support
        LinearLayout btnEmail = findViewById(R.id.btn_email_support);
        btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@reflect.app"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Reflect App Support Request");
            try {
                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show();
            }
        });

        // Report a Bug
        LinearLayout btnBug = findViewById(R.id.btn_report_bug);
        btnBug.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:bugs@reflect.app"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Reflect App Bug Report");
            try {
                startActivity(Intent.createChooser(intent, "Report Bug"));
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show();
            }
        });

        // Privacy Policy
        LinearLayout btnPrivacy = findViewById(R.id.btn_privacy);
        btnPrivacy.setOnClickListener(v ->
                Toast.makeText(this, "Privacy Policy coming soon.", Toast.LENGTH_SHORT).show());
    }

    private void setupFaqToggle(int rowId, int answerId, int arrowId, boolean startExpanded) {
        LinearLayout row   = findViewById(rowId);
        TextView answer    = findViewById(answerId);
        ImageView arrow    = findViewById(arrowId);

        // Set initial state
        answer.setVisibility(startExpanded ? View.VISIBLE : View.GONE);
        arrow.setRotation(startExpanded ? 270f : 90f);

        row.setOnClickListener(v -> {
            boolean isOpen = answer.getVisibility() == View.VISIBLE;
            if (isOpen) {
                answer.setVisibility(View.GONE);
                arrow.setRotation(90f);
            } else {
                answer.setVisibility(View.VISIBLE);
                arrow.setRotation(270f);
            }
        });
    }
}


