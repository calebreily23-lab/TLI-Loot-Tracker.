
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

    StringBuilder recent = new StringBuilder();
    java.util.ArrayDeque<String> lastLines =
        new java.util.ArrayDeque<>();

    int totalLines = 0;

    try (java.io.BufferedReader reader =
             new java.io.BufferedReader(
                 new java.io.FileReader(file))) {

        String line;

        while ((line = reader.readLine()) != null) {
            totalLines++;

            if (!line.trim().isEmpty()) {
                // Keep only the newest 30 nonempty lines.
                if (lastLines.size() >= 30) {
                    lastLines.removeFirst();
                }

                // Limit each line so the popup stays manageable.
                lastLines.addLast(
                    line.length() > 250
                        ? line.substring(0, 250)
                        : line
                );
            }
        }

        for (String recentLine : lastLines) {
            recent.append(recentLine).append("\n");
        }

        String modified =
            new java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.US
            ).format(new java.util.Date(file.lastModified()));

        return "Log size: " + file.length() + " bytes"
            + "\nLast modified: " + modified
            + "\nTotal lines: " + totalLines
            + "\n\nNewest 30 nonempty lines:\n"
            + recent.toString();

    } catch (Exception e) {
        return "Log scan error: " + e.getMessage();
    }
}

    public void destroy() {
        // Called when Shizuku stops this UserService.
    }
}
