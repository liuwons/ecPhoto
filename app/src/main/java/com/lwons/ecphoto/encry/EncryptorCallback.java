package com.lwons.ecphoto.encry;

/**
 * Created by liuwons on 2018/10/18
 */
public interface EncryptorCallback {
    void onFail(Throwable throwable);
    void onUpdate(int progress);
    void onFinish();
}
