package com.example.myapplication;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        MultiDex.install(this);
        DexUtils.loadDex(base);
        super.attachBaseContext(base);

    }

}
