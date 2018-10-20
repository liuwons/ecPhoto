package com.lwons.ecphoto.neo;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.lwons.ecphoto.data.DataManager;
import com.lwons.ecphoto.encry.Encryptor;
import com.lwons.ecphoto.encry.EncryptorCallback;
import com.lwons.ecphoto.model.Photo;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by liuwons on 2018/10/20
 */
public class Neo {
    private static Neo sInstance;

    // name of SharedPreference
    private static final String PREF_NAME = "lwons.ecphoto";
    // base file path of all album data
    private static final String PREF_KEY_BASE_FPATH = "base_path";

    private Encryptor mEncryptor;
    private DataManager mDataManager;

    private boolean mInited = false;
    private File mBasePath;

    public static Neo getInstance() {
        if (sInstance == null) {
            synchronized (Neo.class) {
                if (sInstance == null) {
                    sInstance = new Neo();
                }
            }
        }
        return sInstance;
    }

    private Neo() {
        mDataManager = new DataManager();
    }

    public synchronized Observable<Boolean> init(final Context context) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                if (mInited) {
                    e.onNext(true);
                    return;
                }

                SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                if (preferences.contains(PREF_KEY_BASE_FPATH)) {
                    String path = preferences.getString(PREF_KEY_BASE_FPATH, "");
                    if (TextUtils.isEmpty(path)) {
                        e.onError(new NeoException("failed to init root path"));
                        return;
                    }
                    mBasePath = new File(path);
                } else {
                    mBasePath = context.getExternalFilesDir(null);
                    if (mBasePath == null) {
                        e.onError(new NeoException("sd card no available"));
                        return;
                    }
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(PREF_KEY_BASE_FPATH, mBasePath.getAbsolutePath());
                }

                if (!mBasePath.exists() || !mBasePath.isDirectory()) {
                    e.onError(new NeoException("root path not available"));
                    return;
                }

                e.onNext(true);
                mInited = true;
            }
        });
    }

    public synchronized void setAuth(String user, String pwd) throws Exception{
        mEncryptor = new Encryptor(user, pwd);
    }

    public synchronized boolean available() {
        return mInited && mEncryptor != null;
    }

    public synchronized boolean inited() {
        return mInited;
    }

    public synchronized boolean encryptInited() {
        return mEncryptor != null;
    }

    public synchronized Observable<Integer> encryptPhoto(final String album, final String filePath) {
        if (!mInited) {
            return Observable.error(new NeoException("not init yet"));
        }

        if (mEncryptor == null) {
            return Observable.error(new NeoException("encryptor not init yet"));
        }

        if (TextUtils.isEmpty(filePath)) {
            return Observable.error(new NeoException("read file failed"));
        }

        final File inputFile = new File(filePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            return Observable.error(new NeoException("file not exists"));
        }

        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(final ObservableEmitter<Integer> e) throws Exception {
                final String outputPath = "";
                File outputFile = new File(outputPath);

                mEncryptor.encryptFile(inputFile, outputFile, new EncryptorCallback() {
                    @Override
                    public void onFail(Throwable throwable) {
                        e.onError(throwable);
                    }

                    @Override
                    public void onUpdate(int progress) {
                        e.onNext(progress);
                    }

                    @Override
                    public void onFinish() {
                        Photo photo = new Photo();
                        photo.originFilePath = filePath;
                        photo.album = album;
                        photo.createTime = System.currentTimeMillis();
                        photo.encryptedFilePath = outputPath;
                        photo.photoId = "";
                        photo.size = inputFile.length();
                        mDataManager.addPhoto(photo);
                    }
                });
            }
        });
    }

    public synchronized Observable<Integer> decryptPhoto(final Photo photo, final String outputPath) {
        if (!mInited) {
            return Observable.error(new NeoException("not init yet"));
        }

        if (mEncryptor == null) {
            return Observable.error(new NeoException("encryptor not init yet"));
        }

        if (TextUtils.isEmpty(outputPath)) {
            return Observable.error(new NeoException("read file failed"));
        }

        final File inputFile = new File(outputPath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            return Observable.error(new NeoException("file not exists"));
        }

        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(final ObservableEmitter<Integer> e) throws Exception {
                mEncryptor.decryptFile(new File(photo.encryptedFilePath), new File(outputPath), new EncryptorCallback() {
                    @Override
                    public void onFail(Throwable throwable) {
                        e.onError(throwable);
                    }

                    @Override
                    public void onUpdate(int progress) {
                        e.onNext(progress);
                    }

                    @Override
                    public void onFinish() {
                        e.onComplete();
                    }
                });
            }
        });
    }

    public synchronized Observable<Boolean> deleteEncryptedPhoto(String photoId) {
        return mDataManager.deletePhoto(photoId);
    }

    public synchronized Observable<Boolean> deleteAlbum(String albumName) {
        return mDataManager.deleteAlbum(albumName);
    }

    public synchronized Observable<Boolean> addAlbum(String albumName) {
        return mDataManager.addAlbum(albumName);
    }
}
