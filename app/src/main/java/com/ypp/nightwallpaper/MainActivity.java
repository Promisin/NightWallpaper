package com.ypp.nightwallpaper;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.ypp.nightwallpaper.MyApplication.getInstance;
import static com.ypp.nightwallpaper.MyApplication.hasChanged;

public class MainActivity extends Activity {
    private Switch modeSwitch;
    private Button saveDayButton;
    private Button saveNightButton;
    private Button stopButton;
    private WallpaperManager wallpaperManager;
    private ImageView loadingAnimImv;
    private ImageView finishAnimImv;
    private TextView tvGuide;
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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 10);
        }
        hasChanged = false;
        wallpaperManager = ((MyApplication) getInstance()).getWallpaperManager();
        modeSwitch = findViewById(R.id.switch_mode);
        saveDayButton = findViewById(R.id.save_day_button);
        saveNightButton = findViewById(R.id.save_night_button);
        stopButton = findViewById(R.id.stop_button);
        modeSwitch.setChecked(((MyApplication)getInstance()).getState());
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
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ConfigurationListenService.class);
                stopService(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }
        });
        loadingAnimImv = findViewById(R.id.loading_anim_imv);
        loadingAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate_anim);
        finishAnimImv = findViewById(R.id.finish_anim_imv);
        finishAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.finish_anim);
        tvGuide = findViewById(R.id.tv_guide);
        tvGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGuide();
            }
        });
    }

    private void showGuide() {
        final FrameLayout decorView = (FrameLayout)MainActivity.this.getWindow().getDecorView();
        View rootView = findViewById(android.R.id.content);
        int width = rootView.getWidth();
        int height = rootView.getHeight();
        final GuideView guideView = new GuideView(MainActivity.this);
        decorView.addView(guideView);
        ViewGroup.LayoutParams layoutParams = guideView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        guideView.setLayoutParams(layoutParams);
        guideView.setClickable(true);
        guideView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guideView.invalidate();
                if (guideView.pageNum == 5) {
                    decorView.removeView(guideView);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        registerReceiver(configReceiver, filter);
        ((MyApplication)getInstance()).setForeground(true);
        modeSwitch.setChecked(((MyApplication)getInstance()).getState());
        if (hasChanged){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            Intent intentRecreate = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intentRecreate);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((MyApplication)getInstance()).setForeground(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(configReceiver);
    }

    private BroadcastReceiver configReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!((MyApplication)getInstance()).isForeground()){
                hasChanged = !hasChanged;
                return;
            }
            Configuration configuration = getResources().getConfiguration();
            int mSysThemeConfig = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (mSysThemeConfig==Configuration.UI_MODE_NIGHT_YES ||
                    mSysThemeConfig==Configuration.UI_MODE_NIGHT_NO){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                Intent intentRecreate = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intentRecreate);
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                finish();
            }
        }
    };

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

    private class GuideView extends View{
        private Paint paint;
        private Rect rect;
        private RectF rectF;
        private int pageNum;
        private final int margin = 50;
        private final String[] instructions = {"切换昼/夜壁纸",
                "保存当前壁纸为日间壁纸", "保存当前壁纸为夜间壁纸", "停止服务按钮", "壁纸模式会跟随系统夜间模式切换"};
        private final View[] views = {modeSwitch, saveDayButton, saveNightButton, stopButton};
        PorterDuffXfermode porterDuffXfermode;
        public GuideView(Context context) {
            super(context);
            paint = new Paint();
            rect = new Rect();
            rectF = new RectF();
            pageNum = 0;
            porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.saveLayer(0,0,getWidth(),getHeight(),null);
            canvas.drawARGB(200, 0, 0, 0 );
            paint.setTextSize(50);
            if (pageNum < 4) {
                views[pageNum].getGlobalVisibleRect(rect);
                rectF.set(rect);
                paint.setXfermode(porterDuffXfermode);
                canvas.drawRoundRect(rectF, 10, 10, paint);
                paint.setXfermode(null);
                paint.setColor(Color.WHITE);
                canvas.drawText(instructions[pageNum], margin, 2 * margin, paint);

            } else if (pageNum < 5) {
                paint.setColor(Color.WHITE);
                canvas.drawText(instructions[pageNum], margin, getHeight()-margin, paint);
            }
            pageNum++;
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
