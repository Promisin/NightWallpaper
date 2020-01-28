package com.example.nightwallpaper;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.example.nightwallpaper.MyApplication.getInstance;

public class MainActivity extends Activity {
    private Switch modeSwitch;
    private Button saveDayButton;
    private Button saveNightButton;
    private SharedPreferences sharedPreferences;
    private WallpaperManager wallpaperManager;
    private ImageView loadingAnimImv;
    private ImageView finishAnimImv;
    private Animation loadingAnimation;
    private Animation finishAnimation;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int START_LOADING = 0x01;
    private static final int STOP_LOADING = 0x02;
    private Handler animHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_LOADING:
                    loadingAnimImv.setVisibility(View.VISIBLE);
                    loadingAnimImv.startAnimation(loadingAnimation);
                    break;
                case STOP_LOADING:
                    loadingAnimImv.clearAnimation();
                    loadingAnimImv.setVisibility(View.INVISIBLE);
                    finishAnimImv.setVisibility(View.VISIBLE);
                    finishAnimImv.startAnimation(finishAnimation);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 10);
        }
        sharedPreferences = ((MyApplication)getInstance()).getSharedPreferences();
        wallpaperManager = ((MyApplication) getInstance()).getWallpaperManager();
        modeSwitch = findViewById(R.id.switch_mode);
        saveDayButton = findViewById(R.id.save_day_button);
        saveNightButton = findViewById(R.id.save_night_button);
        modeSwitch.setChecked(sharedPreferences.getBoolean("mode", false));
        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    ((MyApplication)getInstance()).setNightWallpaper();
                }
                else{
                    ((MyApplication)getInstance()).setDayWallpaper();
                }
            }
        });
        saveDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentAsDay();
            }
        });
        saveNightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentAsNight();
            }
        });
        loadingAnimImv = findViewById(R.id.loading_anim_imv);
        loadingAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate_anim);
        finishAnimImv = findViewById(R.id.finish_anim_imv);
        finishAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.finish_anim);
        Intent intent = new Intent(this, ConfigurationListenService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        modeSwitch.setChecked(sharedPreferences.getBoolean("mode", false));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==10){
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(MainActivity.this, "已获取权限", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(MainActivity.this, "权限请求失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void saveCurrentAsDay() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Drawable currentDrawable = wallpaperManager.getDrawable();
                Bitmap currentBitmap = ((BitmapDrawable) currentDrawable).getBitmap();
                savePNG(currentBitmap, ((MyApplication)getInstance()).getDayWallpaperPath());
            }
        }).start();
    }

    public void saveCurrentAsNight() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Drawable currentDrawable = wallpaperManager.getDrawable();
                Bitmap currentBitmap = ((BitmapDrawable) currentDrawable).getBitmap();
                savePNG(currentBitmap, ((MyApplication)getInstance()).getNightWallpaperPath());
            }
        }).start();
    }

    private void savePNG(Bitmap bitmap, String path){
        Message msgStart = new Message();
        msgStart.what = START_LOADING;
        animHandler.sendMessage(msgStart);
        File nightWallpaper = new File(path);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(nightWallpaper);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message msgStop = new Message();
        msgStop.what = STOP_LOADING;
        animHandler.sendMessage(msgStop);
    }
}
