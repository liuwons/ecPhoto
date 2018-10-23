package com.lwons.ecphoto.ui.widget;

import android.content.Context;
import android.support.design.card.MaterialCardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.lwons.ecphoto.R;
import com.lwons.ecphoto.model.Photo;
import com.lwons.ecphoto.ui.PhotoAdapter;

/**
 * Created by liuwons on 2018/10/20
 */
public class PhotoCardView extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {
    private MaterialCardView mCardView;
    private SimpleDraweeView mImage;

    private Photo mPhoto = null;

    private PhotoAdapter.OnItemLongClickListener mLongClickListener;
    private PhotoAdapter.OnItemClickListener mClickListener;

    public PhotoCardView(Context context) {
        super(context);
        initView();
    }

    public PhotoCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PhotoCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.recycler_item_photo, this);
        mCardView = findViewById(R.id.card);
        mImage = findViewById(R.id.img);

        setOnClickListener(this);
        setLongClickable(true);
        setOnLongClickListener(this);
    }

    public void renderPhoto(Photo photo) {
        mPhoto = photo;
        mImage.setImageURI(photo.originUri);
    }

    @Override
    public void onClick(View v) {
        if (mClickListener != null && mPhoto != null) {
            mClickListener.onItemClick(mPhoto);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mLongClickListener != null && mPhoto != null) {
            mLongClickListener.onItemLongClick(mPhoto);
            return true;
        }
        return false;
    }

    public void setOnItemLongClickListener(PhotoAdapter.OnItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    public void setOnItemClickListener(PhotoAdapter.OnItemClickListener listener) {
        mClickListener = listener;
    }
}
