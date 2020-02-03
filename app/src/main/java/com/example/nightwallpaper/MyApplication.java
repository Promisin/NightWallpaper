package com.example.nightwallpaper;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.WallpaperManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyApplication extends Application {
    private static Application myApplication;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private WallpaperManager wallpaperManager;
    private String nightWallpaperPath;
    private String dayWallpaperPath;

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        wallpaperManager = WallpaperManager.getInstance(this);
        sharedPreferences = getSharedPreferences("config", MODE_APPEND);
        editor = sharedPreferences.edit();
        editor.apply();
        FilePathUtils fpu = new FilePathUtils(getApplicationContext());
        nightWallpaperPath = fpu.getNightWallpaperPath();
        dayWallpaperPath = fpu.getDayWallpaperPath();
    }

    private void getPrimaryWallpaper() {
        Drawable primaryDrawable = wallpaperManager.getDrawable();
        Bitmap primaryBitmap = ((BitmapDrawable) primaryDrawable).getBitmap();
        File dayWallpaper = new File(dayWallpaperPath);
        if (!dayWallpaper.exists()) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(dayWallpaper);
                primaryBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setNightWallpaper() {
        editor.putBoolean("mode", true);
        editor.apply();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getPrimaryWallpaper();
                try {
                    File nightWallpaper = new File(nightWallpaperPath);
                    if (nightWallpaper.exists()) {
                        Bitmap nightBitmap = BitmapFactory.decodeStream(new FileInputStream(nightWallpaper));
                        wallpaperManager.setBitmap(nightBitmap);
                    } else {
                        Bitmap nightBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.full_black_bg);
                        FileOutputStream fileOutputStream = new FileOutputStream(nightWallpaper);
                        nightBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        wallpaperManager.setBitmap(nightBitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setDayWallpaper() {
        editor.putBoolean("mode", false);
        editor.apply();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File dayWallpaper = new File(dayWallpaperPath);
                    if (dayWallpaper.exists()) {
                        Bitmap dayBitmap = BitmapFactory.decodeStream(new FileInputStream(dayWallpaper));
                        wallpaperManager.setBitmap(dayBitmap);
                    } else {
                        wallpaperManager.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.full_black_bg));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public boolean getState(){
        return sharedPreferences.getBoolean("mode", false);
    }
    public static Application getInstance() {
        return myApplication;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public SharedPreferences.Editor getEditor() {
        return editor;
    }

    public WallpaperManager getWallpaperManager() {
        return wallpaperManager;
    }

    public String getNightWallpaperPath() {
        return nightWallpaperPath;
    }

    public String getDayWallpaperPath() {
        return dayWallpaperPath;
    }

}
