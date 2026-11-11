
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
    return "Game log scanner connected. Ready to scan.";
}
    public void destroy() {
        // Called when Shizuku stops this UserService.
    }
}
