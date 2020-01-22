package com.example.nightwallpaper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import static com.example.nightwallpaper.MyApplication.getInstance;

public class MainActivity extends Activity {
    private Switch modeSwitch;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = ((MyApplication)getInstance()).getSharedPreferences();
        editor = ((MyApplication)getInstance()).getEditor();
        modeSwitch = findViewById(R.id.switch_mode);
        modeSwitch.setChecked(sharedPreferences.getBoolean("mode",false));
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
    }
}
