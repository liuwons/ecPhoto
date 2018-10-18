package com.lwons.ecphoto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.lwons.ecphoto.encry.Encryptor;
import com.lwons.ecphoto.encry.EncryptorCallback;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Encryptor.getInstance().init("liuwons", "1234");
        } catch (Exception e) {
            e.printStackTrace();
        }

        EncryptorCallback callback = new EncryptorCallback() {
            @Override
            public void onFail(Throwable throwable) {
                Log.e(TAG, "fail");
                throwable.printStackTrace();
            }

            @Override
            public void onUpdate(int progress) {
                Log.e(TAG, "update:" + progress);
            }

            @Override
            public void onFinish() {
                Log.e(TAG, "finish");
            }
        };

        Log.e(TAG, "start encry");
        Encryptor.getInstance().processFile(new File("/sdcard/Download/a.jpg"), new File("/sdcard/Download/b.jpg"), Encryptor.MODE_ENCRYPT, callback);

        Log.e(TAG, "start decry");
        Encryptor.getInstance().processFile(new File("/sdcard/Download/b.jpg"), new File("/sdcard/Download/c.jpg"), Encryptor.MODE_DECRYPT, callback);
        Log.e(TAG, "end");
    }
}
