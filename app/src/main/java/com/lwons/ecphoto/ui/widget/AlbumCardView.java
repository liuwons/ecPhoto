package com.lwons.ecphoto.ui.widget;

import android.content.Context;
import android.support.design.card.MaterialCardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lwons.ecphoto.R;
import com.lwons.ecphoto.image.EncrypedDraweeView;
import com.lwons.ecphoto.model.Album;
import com.lwons.ecphoto.model.Photo;
import com.lwons.ecphoto.neo.Neo;
import com.lwons.ecphoto.ui.AlbumAdapter;
import com.lwons.ecphoto.util.ColorUtils;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by liuwons on 2018/10/20
 */
public class AlbumCardView extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {
    private MaterialCardView mCardView;
    private TextView mAlbumName;
    private EncrypedDraweeView mCover;

    private Album mAlbum = null;

    private AlbumAdapter.OnItemLongClickListener mLongClickListener;
    private AlbumAdapter.OnItemClickListener mClickListener;

    private Disposable mCoverPhotoDisposable;

    public AlbumCardView(Context context) {
        super(context);
        initView();
    }

    public AlbumCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AlbumCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.recycler_item_album, this);
        mCardView = findViewById(R.id.card);
        mAlbumName = findViewById(R.id.album_name);
        mCover = findViewById(R.id.cover);

        setOnClickListener(this);
        setLongClickable(true);
        setOnLongClickListener(this);
    }

    public void renderAlbum(final Album album) {
        mAlbum = album;
        mAlbumName.setText(album.name);

        if (mCoverPhotoDisposable != null && !mCoverPhotoDisposable.isDisposed()) {
            mCoverPhotoDisposable.dispose();
        }
        Neo.getInstance().loadAlbumCover(album.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Photo>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mCoverPhotoDisposable = d;
                    }

                    @Override
                    public void onSuccess(Photo photo) {
                        mCover.setVisibility(VISIBLE);
                        mCover.setEncryptedImageURI(photo.encryptedUri);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mCover.setVisibility(INVISIBLE);
                        mCardView.setCardBackgroundColor(ColorUtils.generateColor(getContext(), album.name));
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (mClickListener != null && mAlbum != null) {
            mClickListener.onItemClick(mAlbum);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mLongClickListener != null && mAlbum != null) {
            mLongClickListener.onItemLongClick(mAlbum);
            return true;
        }
        return false;
    }

    public void setOnItemLongClickListener(AlbumAdapter.OnItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    public void setOnItemClickListener(AlbumAdapter.OnItemClickListener listener) {
        mClickListener = listener;
    }
}
