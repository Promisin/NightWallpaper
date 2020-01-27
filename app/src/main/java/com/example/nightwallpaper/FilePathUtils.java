package com.example.nightwallpaper;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

public class FilePathUtils {
    private String dayWallpaperPath;
    private String nightWallpaperPath;
    private String fileDir;
    public FilePathUtils(Context context) {
        if (Build.VERSION.SDK_INT >= 29){
            fileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        }
        else{
            fileDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        dayWallpaperPath = fileDir + File.separator + "day.png";
        nightWallpaperPath = fileDir + File.separator + "night.png";
    }

    public String getDayWallpaperPath() {
        return dayWallpaperPath;
    }

    public String getNightWallpaperPath() {
        return nightWallpaperPath;
    }
}
