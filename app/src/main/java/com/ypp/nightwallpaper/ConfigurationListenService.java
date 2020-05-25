package com.ypp.nightwallpaper;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import static com.ypp.nightwallpaper.MyApplication.getInstance;

public class ConfigurationListenService extends Service {
    private final String CHANNEL_ID = "onConfigurationChangeListenService";
    private final String CHANNEL_NAME = "onConfigurationChangeListenService";
    private final String CHANNEL_DESCRIPTION = "监听夜间模式变化";
    private final int NOTIFICATION_ID = 111;
    private static final String TAG = "ConfListen";
    private NotificationCompat.Builder builder;
    private RemoteViews viewNoti;
    private NotificationManager manager;
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
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
        channel.setDescription(CHANNEL_DESCRIPTION);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        channel.setSound(null,null);
        channel.enableVibration(false);
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);
        viewNoti = new RemoteViews(getPackageName(), R.layout.notification_layout);
        viewNoti.setTextViewText(R.id.tvState,
                                ((MyApplication)getInstance()).getState()?"夜":"昼");
        builder = new NotificationCompat.Builder(this.getApplicationContext(),CHANNEL_ID);
        builder.setSmallIcon(R.drawable.notify_icon)
                .setContentIntent(PendingIntent.getActivity(
                        this,0,
                        new Intent(this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                        0))
                .setWhen(System.currentTimeMillis())
                .setContent(viewNoti)
                .setPriority(NotificationCompat.PRIORITY_MIN);
        startForeground(NOTIFICATION_ID, builder.build());
        Log.d(TAG, "onStartCommand: ");
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
            if (mSysThemeConfig==Configuration.UI_MODE_NIGHT_YES ||
                    mSysThemeConfig==Configuration.UI_MODE_NIGHT_NO){
                switch (mSysThemeConfig){
                    case Configuration.UI_MODE_NIGHT_YES:
                        ((MyApplication)getInstance()).setNightWallpaper();
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        ((MyApplication)getInstance()).setDayWallpaper();
                        break;
                }
                builder.setContent(viewNoti)
                        .setWhen(System.currentTimeMillis());
                viewNoti.setTextViewText(R.id.tvState,
                        ((MyApplication)getInstance()).getState()?"夜":"昼");
                manager.notify(NOTIFICATION_ID, builder.build());
            }
        }
    };
}
