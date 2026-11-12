
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
        "dropmgr", "currency", "collect",
        "bagv2", "gain"
    };

    int[] counts = new int[keywords.length];
    int totalLines = 0;
    int matchingLines = 0;

    java.util.ArrayDeque<String> recent =
        new java.util.ArrayDeque<>();

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

            boolean matched = false;

            for (int i = 0; i < keywords.length; i++) {
                if (lower.contains(keywords[i])) {
                    counts[i]++;
                    matched = true;
                }
            }

            if (matched) {
                matchingLines++;

                StringBuilder sample =
                    new StringBuilder();

                sample.append(
                    "\n--- MATCH AT LINE "
                    + totalLines + " ---\n"
                );

                // Include two preceding lines.
                for (String previous : recent) {
                    sample.append(previous).append("\n");
                }

                sample.append(line).append("\n");

                // Keep the newest 15 matched events.
                if (samples.size() >= 15) {
                    samples.removeFirst();
                }

                samples.addLast(sample.toString());
            }

            if (recent.size() >= 2) {
                recent.removeFirst();
            }

            recent.addLast(line);
        }

        StringBuilder result = new StringBuilder();

        result.append("Log size: ")
            .append(file.length())
            .append(" bytes\n");

        result.append("Total lines: ")
            .append(totalLines)
            .append("\n");

        result.append("Matching lines: ")
            .append(matchingLines)
            .append("\n\n");

        result.append("TARGETED COUNTS:\n");

        for (int i = 0; i < keywords.length; i++) {
            result.append(keywords[i])
                .append(": ")
                .append(counts[i])
                .append("\n");
        }

        result.append("\nLATEST 15 EVENTS:\n");

        for (String sample : samples) {
            result.append(sample);
        }

        if (samples.isEmpty()) {
            result.append("No targeted events found.");
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
