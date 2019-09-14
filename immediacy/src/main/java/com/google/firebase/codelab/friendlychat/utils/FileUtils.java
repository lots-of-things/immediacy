package com.google.firebase.codelab.friendlychat.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import com.google.firebase.codelab.friendlychat.constants.Constant;

public class FileUtils {

    public static File createFileWithExtension(String extension) {
        File path = Environment.getExternalStoragePublicDirectory(
                "NearbyChat");
        File file = new File(path, DatabaseUtils.getCurrentUUID() + "-" + Calendar.getInstance()
                .getTimeInMillis() + "." + extension);

        if (!path.exists()) {
            path.mkdirs();
        }
        try {

            Log.w(Constant.NEARBY_CHAT, "File: " + file.getAbsolutePath());

            file.createNewFile();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
