
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

    StringBuilder results = new StringBuilder();
    int matches = 0;

    try (java.io.BufferedReader reader =
             new java.io.BufferedReader(
                 new java.io.FileReader(file))) {

        String line;

        while ((line = reader.readLine()) != null) {
            String lower = line.toLowerCase(
                java.util.Locale.ROOT);

            if (lower.contains("pickup")
                    || lower.contains("pick up")
                    || lower.contains("inventory")
                    || lower.contains("flame sand")
                    || lower.contains("resonance")
                    || lower.contains("itemadd")) {

                matches++;

                if (results.length() < 12000) {
                    results.append(line).append("\n");
                }
            }
        }

        return "Matching log lines: " + matches
            + "\n\n" + results.toString();

    } catch (Exception e) {
        return "Log scan error: " + e.getMessage();
    }
}

    public void destroy() {
        // Called when Shizuku stops this UserService.
    }
}
