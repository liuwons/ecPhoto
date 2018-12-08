package com.lwons.ecphoto.image;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.lwons.ecphoto.neo.Neo;
import com.lwons.ecphoto.util.EcpFormatUtils;
import com.lwons.ecphoto.util.L;

import java.io.File;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by liuwons on 2018/10/30
 *
 * DraweeView that can load encrypted photo uri like ecp://album_id/photo_id
 */
public class EncrypedDraweeView extends SimpleDraweeView {
    private static final String TAG = EncrypedDraweeView.class.getSimpleName();

    public EncrypedDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    public EncrypedDraweeView(Context context) {
        super(context);
    }

    public EncrypedDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EncrypedDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EncrypedDraweeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setEncryptedImageURI(final String uriString) {
        Uri uri = null;
        try {
            uri = Uri.parse(uriString);
        } catch (Exception e) {
            e.printStackTrace();
            L.e(TAG, "parse uri failed: " + uriString);
            return;
        }
        if (uri == null) {
            L.e(TAG, "parse uri failed: " + uriString);
            return;
        }

        setEncryptedImageURI(uri);
    }

    public void setEncryptedImageURI(final Uri uri) {
        L.d(TAG, "setImageURI:" + uri.toString());
        if (uri == null) {
            L.e(TAG, "empty uri");
            return;
        }
        String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {
            L.e(TAG, "empty scheme");
            return;
        }
        L.d(TAG, "scheme: " + scheme);
        if (EcpImageConstants.SCHEME_ECP.equals(scheme)) {
            Neo.getInstance().decrypt2cache(uri)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Integer integer) {
                        }

                        @Override
                        public void onError(Throwable e) {
                            L.e(TAG, "decrypt error");
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {
                            Uri cachedDecryptedFileUri = Uri.fromFile(new File(EcpFormatUtils.getCacheFilePath(uri)));
                            L.d(TAG, "decrypted cache file: " + cachedDecryptedFileUri);
                            setImageURI(cachedDecryptedFileUri);
                        }
                    });

        } else {
            setImageURI(uri);
        }
    }
}
