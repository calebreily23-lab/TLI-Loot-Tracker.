
package com.tlitracker.app;

import android.app.Activity;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int CAPTURE_REQUEST = 100;
    private String selectedMode = "live";
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 32, 24, 32);

        TextView title = new TextView(this);
        title.setText("Torchlight: Infinite Loot Scanner");
        title.setTextSize(22);
        layout.addView(title);

        addCaptureButton("Start Loot Scanner", "live");
        addCaptureButton("Save Inventory Baseline", "baseline");
        addCaptureButton("Compare Inventory", "compare");

        Button stop = new Button(this);
        stop.setText("Stop Scanner");
        stop.setOnClickListener(v ->
            stopService(new Intent(this, CaptureService.class))
        );
        layout.addView(stop);

        Button results = new Button(this);
        results.setText("View Loot Results");
        results.setOnClickListener(v -> showResults());
        layout.addView(results);

        scroll.addView(layout);
        setContentView(scroll);
    }

    private void addCaptureButton(String label, String mode) {
        Button button = new Button(this);
        button.setText(label);
        button.setOnClickListener(v -> {
            selectedMode = mode;
            MediaProjectionManager manager =
                (MediaProjectionManager) getSystemService(
                    MEDIA_PROJECTION_SERVICE);
            startActivityForResult(
                manager.createScreenCaptureIntent(),
                CAPTURE_REQUEST);
        });
        layout.addView(button);
    }

    private void showResults() {
        JSONObject data = Ledger.data(this);
        JSONArray candidates =
            data.optJSONArray("candidates");
        JSONArray drops = data.optJSONArray("drops");

        StringBuilder output = new StringBuilder();
        output.append("LOOT RESULTS\n\n");

        int candidateCount =
            candidates == null ? 0 : candidates.length();
        int dropCount =
            drops == null ? 0 : drops.length();

        output.append("Detected text candidates: ")
            .append(candidateCount).append("\n");
        output.append("Recorded drops: ")
            .append(dropCount).append("\n\n");

        double totalFE = 0;

        if (drops != null) {
            for (int i = 0; i < drops.length(); i++) {
                JSONObject item = drops.optJSONObject(i);
                if (item == null) continue;

                double fe = item.optDouble("fe", 0);
                int qty = item.optInt("qty", 1);
                totalFE += fe * qty;
            }
        }

        output.append(String.format(
            Locale.US, "Recorded value: %.2f FE\n", totalFE));

        long start = data.optLong("start", 0);
        if (start > 0 && System.currentTimeMillis() > start) {
            double hours =
                (System.currentTimeMillis() - start) / 3600000.0;
            output.append(String.format(
                Locale.US, "Recorded FE/hour: %.2f\n",
                totalFE / hours));
        }

        output.append("\nDETECTED CANDIDATES\n");
        if (candidates == null || candidates.length() == 0) {
            output.append("No detections saved yet.\n");
        } else {
            int first = Math.max(0, candidates.length() - 100);
            for (int i = candidates.length() - 1; i >= first; i--) {
                JSONObject item = candidates.optJSONObject(i);
                if (item == null) continue;

                output.append("\n")
                    .append(item.optString("name", "Unknown"))
                    .append("\nSource: ")
                    .append(item.optString("mode", "unknown"))
                    .append("\nStatus: ")
                    .append(item.optBoolean("confirmed", false)
                        ? "Reviewed" : "Unconfirmed")
                    .append("\n")
                    .append(formatTime(item.optLong("time", 0)))
                    .append("\n");
            }
        }

        output.append("\nRECORDED DROPS\n");
        if (drops != null) {
            for (int i = drops.length() - 1; i >= 0; i--) {
                JSONObject item = drops.optJSONObject(i);
                if (item == null) continue;

                output.append("\n")
                    .append(item.optString("name", "Unknown"))
                    .append(" x")
                    .append(item.optInt("qty", 1))
                    .append(" — ")
                    .append(item.optDouble("fe", 0))
                    .append(" FE each\n");
            }
        }

        new android.app.AlertDialog.Builder(this)
            .setTitle("Loot Scanner Results")
            .setMessage(output.toString())
            .setPositiveButton("Close", null)
            .show();
    }

    private String formatTime(long timestamp) {
        if (timestamp <= 0) return "Unknown time";
        return DateFormat.getDateTimeInstance()
            .format(new Date(timestamp));
    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_REQUEST &&
            resultCode == RESULT_OK && data != null) {

            Intent service =
                new Intent(this, CaptureService.class);
            service.putExtra("mode", selectedMode);
            service.putExtra("result", resultCode);
            service.putExtra("permission", data);
            startForegroundService(service);
        }
    }
}
