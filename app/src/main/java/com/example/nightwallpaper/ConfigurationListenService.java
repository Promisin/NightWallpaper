package com.example.nightwallpaper;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;

import static com.example.nightwallpaper.MyApplication.getInstance;

public class ConfigurationListenService extends Service {
    private static final String TAG = "receiver";
    public ConfigurationListenService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        registerReceiver(configReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(configReceiver);
    }

    private BroadcastReceiver configReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: "+intent.getAction());
            Configuration configuration = getResources().getConfiguration();
            int mSysThemeConfig = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
            switch (mSysThemeConfig){
                case Configuration.UI_MODE_NIGHT_YES:
                    ((MyApplication)getInstance()).setNightWallpaper();
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    ((MyApplication)getInstance()).setDayWallpaper();
                    break;
            }
        }
    };
}
