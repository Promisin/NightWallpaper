package com.example.nightwallpaper;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyApplication extends Application {
    final private boolean DAY_MODE = false;
    final private boolean NIGHT_MODE = true;
    private static Application myApplication;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private WallpaperManager wallpaperManager;
    private BroadcastReceiver broadcastReceiver;
    private String fileDir = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "NightWallpaper";
    private String nightWallpaperPath = fileDir + File.separator + "night.png";
    private String dayWallpaperPath = fileDir + File.separator + "day.png";

    @SuppressLint("WrongConstant")
    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        wallpaperManager = WallpaperManager.getInstance(this);
        sharedPreferences = getSharedPreferences("config", MODE_APPEND);
        editor = sharedPreferences.edit();
        editor.apply();
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                getPrimaryWallpaper();
                try {
                    File dir = new File(fileDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File dir = new File(fileDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
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

    public void saveCurrentAsDay() {
        Drawable currentDrawable = wallpaperManager.getDrawable();
        Bitmap currentBitmap = ((BitmapDrawable) currentDrawable).getBitmap();
        savePNG(currentBitmap, dayWallpaperPath);
    }

    public void saveCurrentAsNight() {
        Drawable currentDrawable = wallpaperManager.getDrawable();
        Bitmap currentBitmap = ((BitmapDrawable) currentDrawable).getBitmap();
        savePNG(currentBitmap, nightWallpaperPath);
    }

    private void savePNG(Bitmap bitmap, String path){
        File nightWallpaper = new File(path);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(nightWallpaper);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

}
