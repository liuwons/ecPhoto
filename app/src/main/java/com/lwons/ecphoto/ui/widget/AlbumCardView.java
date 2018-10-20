package com.lwons.ecphoto.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lwons.ecphoto.R;
import com.lwons.ecphoto.model.Album;

/**
 * Created by liuwons on 2018/10/20
 */
public class AlbumCardView extends RelativeLayout {
    private TextView mAlbumName;
    private SimpleDraweeView mCover;

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
        LayoutInflater.from(getContext()).inflate(R.layout.layout_album, this);
        mAlbumName = findViewById(R.id.album_name);
        mCover = findViewById(R.id.cover);
    }

    public void renderAlbum(Album album) {
        mAlbumName.setText(album.name);
    }
}
