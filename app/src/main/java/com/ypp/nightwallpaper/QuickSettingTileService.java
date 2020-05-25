package com.ypp.nightwallpaper;

import android.content.SharedPreferences;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import androidx.annotation.RequiresApi;

import static com.ypp.nightwallpaper.MyApplication.getInstance;

@RequiresApi(api = Build.VERSION_CODES.N)
public class QuickSettingTileService extends TileService {
    private static String TAG = "NightWallpaper";
    private SharedPreferences.Editor editor = ((MyApplication)getInstance()).getEditor();
    //添加快捷开关
    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Tile tile = getQsTile();
        if (((MyApplication)getInstance()).getState()){
            tile.setState(Tile.STATE_ACTIVE);
        }
        else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
        Log.d(TAG, "onTileAdded: ");
    }
    
    //移除快捷开关
    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        Log.d(TAG, "onTileRemoved: ");
    }

    //点击时调用
    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (tile.getState()==Tile.STATE_ACTIVE){
            ((MyApplication)getInstance()).setDayWallpaper();
            tile.setState(Tile.STATE_INACTIVE);
            editor.putBoolean("mode",false);
            Log.d(TAG, "onClick: false");
        }
        else if (tile.getState()==Tile.STATE_INACTIVE){
            ((MyApplication)getInstance()).setNightWallpaper();
            tile.setState(Tile.STATE_ACTIVE);
            editor.putBoolean("mode",true);
            Log.d(TAG, "onClick: true");
        }
        tile.updateTile();
        editor.apply();
    }

    // 打开下拉菜单的时候调用,当快速设置按钮并没有在编辑栏拖到设置栏中不会调用
    //在TleAdded之后会调用一次
    @Override
    public void onStartListening () {
        Tile tile = getQsTile();
        if (((MyApplication)getInstance()).getState()){
            tile.setState(Tile.STATE_ACTIVE);
        }
        else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
        Log.d(TAG, "onStartListening: ");
    }

    // 关闭下拉菜单的时候调用,当快速设置按钮并没有在编辑栏拖到设置栏中不会调用
    // 在onTileRemoved移除之前也会调用移除
    @Override
    public void onStopListening () {

        Log.d(TAG, "onStopListening: ");
    }
}
