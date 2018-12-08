package com.lwons.ecphoto.neo;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.lwons.ecphoto.data.DataManager;
import com.lwons.ecphoto.encry.Encryptor;
import com.lwons.ecphoto.encry.EncryptorCallback;
import com.lwons.ecphoto.image.EcpImageConstants;
import com.lwons.ecphoto.model.Album;
import com.lwons.ecphoto.model.Photo;
import com.lwons.ecphoto.util.EcpFormatUtils;
import com.lwons.ecphoto.util.FileUtils;
import com.lwons.ecphoto.util.L;

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
import io.reactivex.Single;

/**
 * Created by liuwons on 2018/10/20
 */
public class Neo {
    private static final String TAG = Neo.class.getSimpleName();

    private static Neo sInstance;

    // name of SharedPreference
    private static final String PREF_NAME = "lwons.ecphoto";
    // base file path of all album data
    private static final String PREF_KEY_BASE_FPATH = "base_path";

    private Encryptor mEncryptor;
    private DataManager mDataManager;

    private boolean mInited = false;
    private File mBasePath;
    private File mCachePath;
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

                mCachePath = context.getCacheDir();
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
                    editor.apply();
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

    public synchronized Observable<Integer> encryptFile(Context context, final String outFile, final String inUri) {
        if (!mInited) {
            return Observable.error(new NeoException("not init yet"));
        }

        if (mEncryptor == null) {
            return Observable.error(new NeoException("encryptor not init yet"));
        }

        if (TextUtils.isEmpty(inUri)) {
            return Observable.error(new NeoException("read file failed"));
        }

        final InputStream inputStream;
        try {
            inputStream = context.getContentResolver().openInputStream(Uri.parse(inUri));
        } catch (FileNotFoundException e) {
            return Observable.error(new NeoException("file not found"));
        }

        File outputFile = new File(outFile);
        final FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            return Observable.error(new NeoException("create file failed"));
        }

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
                        e.onComplete();
                    }
                });
            }
        });
    }

    public synchronized Observable<Integer> encryptPhoto(Context context, final String albumId, final String uri) {
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
        final String outputPath = getEncryptedFilePath(albumId, photoId);
        final File outputDir = new File(outputPath).getParentFile();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            return Observable.error(new FileNotFoundException("create folder failed"));
        }
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
                        photo.albumId = albumId;
                        photo.createTime = System.currentTimeMillis();
                        photo.encryptedFilePath = outputPath;
                        photo.encryptedUri = EcpImageConstants.SCHEME_ECP_PREFIX + albumId + EcpImageConstants.URI_SEPARATOR + photoId;
                        photo.photoId = photoId;
                        photo.size = finalSize;
                        mDataManager.addPhoto(photo);
                    }
                });
            }
        });
    }

    public synchronized Observable<Integer> decryptPhoto(final String albumId, final String photoId, final String outputPath) {
        L.d(TAG, "decryptPhoto [out]" + outputPath);
        if (!mInited) {
            return Observable.error(new NeoException("not init yet"));
        }

        if (mEncryptor == null) {
            return Observable.error(new NeoException("encryptor not init yet"));
        }

        String inPath = getEncryptedFilePath(albumId, photoId);

        if (TextUtils.isEmpty(outputPath)) {
            return Observable.error(new NeoException("read file failed"));
        }

        final File inputFile = new File(inPath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            return Observable.error(new NeoException("file not exists"));
        }
        final FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(inputFile);
        } catch (FileNotFoundException e) {
            return Observable.error(new NeoException("file not found"));
        }

        File outputDir = new File(outputPath).getParentFile();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            return Observable.error(new NeoException("create folder failed: " + outputDir.getAbsolutePath()));
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

    public synchronized Observable<Boolean> deleteAlbum(String albumId) {
        return mDataManager.deleteAlbum(albumId);
    }

    public synchronized Observable<Album> addAlbum(String albumName) {
        String id = FileUtils.generateId(albumName);
        File dir = new File(mBasePath, id);
        if (!dir.exists() && !dir.mkdirs()) {
            return Observable.error(new FileNotFoundException("create folder failed"));
        }
        Album album = new Album();
        album.name = albumName;
        album.id = id;
        album.mCreateTime = System.currentTimeMillis();
        return mDataManager.addAlbum(album);
    }

    public synchronized Observable<List<Album>> loadAllAlbums() {
        return mDataManager.loadAlbums();
    }

    public synchronized Observable<List<Photo>> loadPhotos(String albumName) {
        return mDataManager.loadPhotos(albumName);
    }

    public synchronized Single<Photo> loadAlbumCover(String albumId) {
        return mDataManager.loadFirstPhotoInAlbum(albumId);
    }

    private long generateEncryptedFileIndex() {
        return mLastEncrypedFileIndex ++;
    }

    private String generateEncryptedPhotoId() {
        return mEncryptedFileIdPrefix + generateEncryptedFileIndex();
    }

    private String getEncryptedFileName(String photoId) {
        return photoId + EcpImageConstants.ECP_ENCRYPTED_FILE_SUFFIX;
    }

    private String getEncryptedFilePath(String albumId, String photoId) {
        String filename = getEncryptedFileName(photoId);
        File albumPath = new File(mBasePath, albumId);
        if (!albumPath.exists()) {
            albumPath.mkdirs();
        }
        return new File(albumPath, filename).getAbsolutePath();
    }

    /**
     *  Decrypt photo to cache dir
     * @param encryptedPhotoUri Uri of encrypted photo, in the form of ecp://album_id/photo_id
     * @return Observable of result
     */
    public Observable<Integer> decrypt2cache(Uri encryptedPhotoUri) {
        if (encryptedPhotoUri.getScheme().equals(EcpImageConstants.SCHEME_ECP)) {
            String albumId = EcpFormatUtils.getAlbumId(encryptedPhotoUri);
            String photoId = EcpFormatUtils.getPhotoId(encryptedPhotoUri);
            return decryptPhoto(albumId, photoId, getDecryptPathInCache(albumId, photoId));
        } else {
            return Observable.error(new Exception("Uri id not ecp scheme"));
        }
    }

    public synchronized String getDecryptPathInCache(String albumId, String photoId) {
        String path = new File(mCachePath, albumId).getAbsolutePath();
        String fileName = photoId + EcpImageConstants.ECP_ENCRYPTED_FILE_SUFFIX + ".img";
        return new File(path, fileName).getAbsolutePath();
    }

    public Uri getDecryptedUri(Uri encryptedPhotoUri) {
        return Uri.fromFile(
                new File(
                        getDecryptPathInCache(
                                EcpFormatUtils.getAlbumId(encryptedPhotoUri),
                                EcpFormatUtils.getPhotoId(encryptedPhotoUri))));
    }
}
