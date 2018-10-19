package com.lwons;

import android.app.Application;

import com.lwons.ecphoto.db.DatabaseManager;

/**
 * Created by liuwons on 2018/10/19
 */
public class EcApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        DatabaseManager.getInstance().init(getApplicationContext());
    }
}
