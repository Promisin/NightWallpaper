package com.example.nightwallpaper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import static com.example.nightwallpaper.MyApplication.getInstance;

public class MainActivity extends Activity {
    private Switch modeSwitch;
    private Button saveDayButton;
    private Button saveNightButton;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = ((MyApplication)getInstance()).getSharedPreferences();
        editor = ((MyApplication)getInstance()).getEditor();
        modeSwitch = findViewById(R.id.switch_mode);
        saveDayButton = findViewById(R.id.save_day_button);
        saveNightButton = findViewById(R.id.save_night_button);
        Configuration configuration = getResources().getConfiguration();
        int mSysThemeConfig = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        modeSwitch.setChecked(mSysThemeConfig == Configuration.UI_MODE_NIGHT_YES);
        modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("mode",!sharedPreferences.getBoolean("mode",false));
                editor.apply();
                if (sharedPreferences.getBoolean("mode",false)){
                    ((MyApplication)getInstance()).setNightWallpaper();
                }
                else{
                    ((MyApplication)getInstance()).setDayWallpaper();
                }
                Log.d("tag", "onCheckedChanged: "+!sharedPreferences.getBoolean("mode",false));
            }
        });
        saveDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MyApplication) getInstance()).saveCurrentAsDay();
            }
        });
        saveNightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MyApplication) getInstance()).saveCurrentAsNight();
            }
        });
        Intent intent = new Intent(this, ConfigurationListenService.class);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Configuration configuration = getResources().getConfiguration();
        int mSysThemeConfig = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        modeSwitch.setChecked(mSysThemeConfig == Configuration.UI_MODE_NIGHT_YES);
    }

    /*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int mSysThemeConfig = newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (mSysThemeConfig){
            case Configuration.UI_MODE_NIGHT_YES:
                ((MyApplication)getInstance()).setNightWallpaper();
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                ((MyApplication)getInstance()).setDayWallpaper();
                break;
        }
    }*/
}
