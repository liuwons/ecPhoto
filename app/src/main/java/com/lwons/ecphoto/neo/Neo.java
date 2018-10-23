package com.lwons.ecphoto.neo;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.imnjh.imagepicker.util.SystemUtil;
import com.lwons.ecphoto.data.DataManager;
import com.lwons.ecphoto.encry.Encryptor;
import com.lwons.ecphoto.encry.EncryptorCallback;
import com.lwons.ecphoto.model.Album;
import com.lwons.ecphoto.model.Photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by liuwons on 2018/10/20
 */
public class Neo {
    private static final String ENCRYPTED_FILE_SUFFIX = ".ecp";

    private static Neo sInstance;

    // name of SharedPreference
    private static final String PREF_NAME = "lwons.ecphoto";
    // base file path of all album data
    private static final String PREF_KEY_BASE_FPATH = "base_path";

    private Encryptor mEncryptor;
    private DataManager mDataManager;

    private boolean mInited = false;
    private File mBasePath;
    private String mEncryptedFileIdPrefix = "";
    private int mLastEncrypedFileIndex = 0;

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

                mEncryptedFileIdPrefix = String.valueOf(System.currentTimeMillis());
                mLastEncrypedFileIndex = 0;
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

    public synchronized Observable<Integer> encryptPhoto(Context context, final String album, final String uri) {
        if (!mInited) {
            return Observable.error(new NeoException("not init yet"));
        }

        if (mEncryptor == null) {
            return Observable.error(new NeoException("encryptor not init yet"));
        }

        if (TextUtils.isEmpty(uri)) {
            return Observable.error(new NeoException("read file failed"));
        }

        final InputStream inputStream;
        int size = 0;
        try {
            inputStream = context.getContentResolver().openInputStream(Uri.parse(uri));
            size = inputStream.available();
        } catch (FileNotFoundException e) {
            return Observable.error(new NeoException("file not found"));
        } catch (IOException e) {
            return Observable.error(new NeoException("read file failed"));
        }

        final String photoId = generateEncryptedPhotoId();
        final String outputPath = generateEncryptedFilePath(album, photoId);
        File outputFile = new File(outputPath);
        final FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            return Observable.error(new NeoException("create file failed"));
        }


        final int finalSize = size;
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(final ObservableEmitter<Integer> e) throws Exception {
                mEncryptor.encrypt(inputStream, outputStream, new EncryptorCallback() {
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
                        photo.originUri = uri;
                        photo.album = album;
                        photo.createTime = System.currentTimeMillis();
                        photo.encryptedFilePath = outputPath;
                        photo.photoId = photoId;
                        photo.size = finalSize;
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
        final FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(inputFile);
        } catch (FileNotFoundException e) {
            return Observable.error(new NeoException("file not found"));
        }

        final OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(outputPath);
        } catch (FileNotFoundException e) {
            return Observable.error(new NeoException("create file failed"));
        }

        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(final ObservableEmitter<Integer> e) throws Exception {
                mEncryptor.decrypt(inputStream, outputStream, new EncryptorCallback() {
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

    public synchronized Observable<List<Album>> loadAllAlbums() {
        return mDataManager.loadAlbums();
    }

    public synchronized Observable<List<Photo>> loadPhotos(String albumName) {
        return mDataManager.loadPhotos(albumName);
    }

    private long generateEncryptedFileIndex() {
        return mLastEncrypedFileIndex ++;
    }

    private String generateEncryptedPhotoId() {
        return mEncryptedFileIdPrefix + generateEncryptedFileIndex();
    }

    private String createEncryptedFileName(String album, String photoId) {
        return photoId + ENCRYPTED_FILE_SUFFIX;
    }

    private String generateEncryptedFilePath(String albumName, String photoId) {
        String filename = createEncryptedFileName(albumName, photoId);
        File albumPath = new File(mBasePath, albumName);
        if (!albumPath.exists()) {
            albumPath.mkdirs();
        }
        return new File(albumPath, filename).getAbsolutePath();
    }
}
