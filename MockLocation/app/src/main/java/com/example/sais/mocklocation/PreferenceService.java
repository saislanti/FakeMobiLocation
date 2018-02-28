package com.example.sais.mocklocation;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ywq on 2017-09-13.
 */
public class PreferenceService {
    private Context mContext;

    public PreferenceService(Context context){
        this.mContext = context;
    }

    /**
     * 保存参数
     * @param pkg   包名
     * @param lat   纬度
     * @param log   经度
     * @param gflag   是否开启gps模拟
     */
    public void save(String pkg, String lat, String log, boolean gflag){
        SharedPreferences preferences = mContext.getSharedPreferences(pkg, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lat", lat);
        editor.putString("log", log);
        editor.putBoolean("gflag", gflag);
        editor.commit();
    }

    /**
     * 获取各项保存的参数
     * @return
     */
    public Map<String, String> getPreference(String pkg){
        Map<String, String> params = new HashMap<String, String>();
        SharedPreferences preferences = mContext.getSharedPreferences(pkg, Context.MODE_PRIVATE);
        params.put("lat", preferences.getString("lat",""));
        params.put("log", preferences.getString("log",""));
        params.put("gflag", String.valueOf(preferences.getBoolean("gflag", false)));
        return params;
    }
}
