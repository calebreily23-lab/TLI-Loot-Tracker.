
package com.tlitracker.app;

import java.io.File;
import rikka.shizuku.Shizuku;

public class LogReaderService extends ILogReader.Stub {

    public LogReaderService() {
        super();
    }

    @Override
    public String checkLog() {
        String path =
            "/sdcard/Android/data/com.xd.TLglobal/files/"
            + "UE4Game/UE_game/UE_game/Saved/Logs/UE_game.log";

        File file = new File(path);

        if (!file.exists()) {
            return "Game log not found or access denied.";
        }

        if (!file.canRead()) {
            return "Game log exists but cannot be read.";
        }

        return "Game log accessible!\n"
            + "Size: " + file.length() + " bytes";
    }



@Override
public String scanLog() {
    String path =
        "/sdcard/Android/data/com.xd.TLglobal/files/"
        + "UE4Game/UE_game/UE_game/Saved/Logs/UE_game.log";

    File file = new File(path);

    if (!file.exists() || !file.canRead()) {
        return "Cannot read game log.";
    }

    String[] keywords = {
        "pickup", "pick up", "loot", "drop",
        "item", "inventory", "bag", "currency",
        "reward", "flame", "resonance",
        "collect", "obtain", "gain"
    };

    int[] counts = new int[keywords.length];
    int totalLines = 0;
    int matchingLines = 0;
    int filteredLines = 0;

    java.util.ArrayDeque<String> samples =
        new java.util.ArrayDeque<>();

    try (java.io.BufferedReader reader =
             new java.io.BufferedReader(
                 new java.io.FileReader(file))) {

        String line;

        while ((line = reader.readLine()) != null) {
            totalLines++;

            String lower = line.toLowerCase(
                java.util.Locale.ROOT);

            // Exclude repetitive asset and font loading.
            if (lower.contains("assetload")
                    || lower.contains("font_load")
                    || lower.contains("loadwidgetbybpname")) {
                filteredLines++;
                continue;
            }

            boolean matched = false;

            for (int i = 0; i < keywords.length; i++) {
                if (lower.contains(keywords[i])) {
                    counts[i]++;
                    matched = true;
                }
            }

            if (matched) {
                matchingLines++;

                // Keep the latest 20 matching entries.
                if (samples.size() >= 20) {
                    samples.removeFirst();
                }

                samples.addLast(
                    line.length() > 300
                        ? line.substring(0, 300)
                        : line
                );
            }
        }

        StringBuilder result = new StringBuilder();

        result.append("Log size: ")
            .append(file.length())
            .append(" bytes\n");

        result.append("Total lines: ")
            .append(totalLines)
            .append("\n");

        result.append("Filtered lines: ")
            .append(filteredLines)
            .append("\n");

        result.append("Matching lines: ")
            .append(matchingLines)
            .append("\n\n");

        result.append("KEYWORD COUNTS:\n");

        for (int i = 0; i < keywords.length; i++) {
            result.append(keywords[i])
                .append(": ")
                .append(counts[i])
                .append("\n");
        }

        result.append("\nLATEST 20 MATCHES:\n");

        for (String sample : samples) {
            result.append(sample).append("\n");
        }

        if (samples.isEmpty()) {
            result.append("No matching entries found.");
        }

        return result.toString();

    } catch (Exception e) {
        return "Log scan error: " + e.getMessage();
    }
}


    public void destroy() {
        // Called when Shizuku stops this UserService.
    }
}
