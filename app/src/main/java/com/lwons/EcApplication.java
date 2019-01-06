package com.lwons;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.imnjh.imagepicker.PickerConfig;
import com.imnjh.imagepicker.SImagePicker;
import com.lwons.ecphoto.R;
import com.lwons.ecphoto.db.DatabaseManager;
import com.lwons.ecphoto.image.FrescoImageLoader;

/**
 * Created by liuwons on 2018/10/19
 */
public class EcApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Utils.init(this);

        Fresco.initialize(this);
        DatabaseManager.getInstance().init(getApplicationContext());
        SImagePicker.init(new PickerConfig.Builder().setAppContext(this)
                .setImageLoader(new FrescoImageLoader())
                .setToolbaseColor(getColor(R.color.colorPrimary))
                .build());
    }
}
