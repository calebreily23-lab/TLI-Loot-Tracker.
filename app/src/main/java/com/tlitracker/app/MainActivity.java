package com.tlitracker.app;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int CAPTURE_REQUEST = 100;
    private String selectedMode = "live";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 48, 32, 32);

        TextView title = new TextView(this);
        title.setText("Torchlight: Infinite Loot Scanner");
        title.setTextSize(22);
        layout.addView(title);

        addButton(layout, "Start Loot Scanner", "live");
        addButton(layout, "Save Inventory Baseline", "baseline");
        addButton(layout, "Compare Inventory", "compare");

        Button stop = new Button(this);
        stop.setText("Stop Scanner");
        stop.setOnClickListener(v ->
            stopService(new Intent(this, CaptureService.class)));
        layout.addView(stop);

        setContentView(layout);
    }

    private void addButton(LinearLayout layout, String label, String mode) {
        Button button = new Button(this);
        button.setText(label);
        button.setOnClickListener(v -> {
            selectedMode = mode;
            MediaProjectionManager manager =
                (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            startActivityForResult(manager.createScreenCaptureIntent(), CAPTURE_REQUEST);
        });
        layout.addView(button);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_REQUEST &&
            resultCode == RESULT_OK && data != null) {

            Intent service = new Intent(this, CaptureService.class);
            service.putExtra("mode", selectedMode);
            service.putExtra("result", resultCode);
            service.putExtra("permission", data);
            startForegroundService(service);
        }
    }
}
