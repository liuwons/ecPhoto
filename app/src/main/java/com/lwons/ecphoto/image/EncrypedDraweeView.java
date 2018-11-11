package com.lwons.ecphoto.image;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.lwons.ecphoto.neo.Neo;

import java.io.File;
import java.util.List;

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

    @Override
    public void setImageURI(Uri uri, @Nullable final Object callerContext) {
        if (uri.getScheme().equals(EcpImageConstants.SCHEMA_ECP)) {
            List<String> segs = uri.getPathSegments();
            String albumId = segs.get(0);
            String photoId = segs.get(1);
            final Uri realUri = Uri.fromFile(new File(Neo.getInstance().getDecryptPathInCache(albumId, photoId)));
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

                        }

                        @Override
                        public void onComplete() {
                            EncrypedDraweeView.super.setImageURI(realUri, callerContext);
                        }
                    });

        } else {
            super.setImageURI(uri, callerContext);
        }
    }
}
