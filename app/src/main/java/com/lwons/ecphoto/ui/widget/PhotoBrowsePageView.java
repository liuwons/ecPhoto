package com.lwons.ecphoto.ui.widget;

import android.content.Context;
import android.graphics.PointF;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.imnjh.imagepicker.util.SystemUtil;
import com.imnjh.imagepicker.widget.subsamplingview.ImageSource;
import com.imnjh.imagepicker.widget.subsamplingview.OnImageEventListener;
import com.imnjh.imagepicker.widget.subsamplingview.SubsamplingScaleImageView;
import com.lwons.ecphoto.neo.Neo;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * File description
 */
public class PhotoBrowsePageView extends FrameLayout {

    /**
     * if aspect ratio is grater than 3
     * load picture as long image
     */
    private static final int LONG_IMG_ASPECT_RATIO = 3;
    private static final int LONG_IMG_MINIMUM_LENGTH = 1500;

    private SubsamplingScaleImageView originImageView;

    public PhotoBrowsePageView(Context context) {
        super(context);
        init(context);
    }

    public PhotoBrowsePageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoBrowsePageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        originImageView = new SubsamplingScaleImageView(context);
        addView(originImageView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        originImageView.setOnImageEventListener(new OnImageEventListener() {
            @Override
            public void onImageLoaded(int width, int height) {
                adjustPictureScale(originImageView, width, height);
            }
        });
    }

    public void setMaxScale(float maxScale) {
        originImageView.setMaxScale(maxScale);
    }

    public void setOnClickListener(OnClickListener listener) {
        originImageView.setOnClickListener(listener);
    }

    public void setEncryptedImageUri(final Uri encryptedImageUri) {
        Neo.getInstance().decrypt2cache(encryptedImageUri)
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
                        originImageView.setImage(ImageSource.uri(Neo.getInstance().getDecryptedUri(encryptedImageUri)));
                    }
                });
    }

    public void setOriginImage(ImageSource imageSource) {
        originImageView.setImage(imageSource);
    }

    public SubsamplingScaleImageView getOriginImageView() {
        return originImageView;
    }

    private static void adjustPictureScale(SubsamplingScaleImageView view, int width, int height) {
        if (height >= LONG_IMG_MINIMUM_LENGTH
                && height / width >= LONG_IMG_ASPECT_RATIO) {
            float scale = SystemUtil.displaySize.x / (float) width;
            float centerX = SystemUtil.displaySize.x / 2;
            view.setScaleAndCenterWithAnim(scale, new PointF(centerX, 0.0f));
            view.setDoubleTapZoomScale(scale);
        }
    }
}
