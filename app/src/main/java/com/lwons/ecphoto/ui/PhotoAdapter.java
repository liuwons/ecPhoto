package com.lwons.ecphoto.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.lwons.ecphoto.model.Photo;
import com.lwons.ecphoto.ui.widget.PhotoCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuwons on 2018/10/20
 */
public class PhotoAdapter extends RecyclerView.Adapter {
    private List<Photo> mPhotoList;

    private OnItemLongClickListener mItemLongClickListener;
    private OnItemClickListener mItemClickListener;

    public PhotoAdapter() {
        mPhotoList = new ArrayList<>();
    }

    @NonNull
    @Override
    public synchronized RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        PhotoCardView cardView = new PhotoCardView(parent.getContext());
        if (mItemClickListener != null) {
            cardView.setOnItemClickListener(mItemClickListener);
        }
        if (mItemLongClickListener != null) {
            cardView.setOnItemLongClickListener(mItemLongClickListener);
        }
        return new PhotoViewHolder(cardView);
    }

    @Override
    public synchronized void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((PhotoViewHolder)viewHolder).update(mPhotoList.get(i));
    }

    @Override
    public synchronized int getItemCount() {
        return mPhotoList.size();
    }

    public void setOnLongClickListener(OnItemLongClickListener listener) {
        mItemLongClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public synchronized void addPhotos(List<Photo> photos) {
        int start = mPhotoList.size();
        mPhotoList.addAll(photos);
        notifyItemRangeChanged(start, photos.size());
    }

    public synchronized void setPhotos(List<Photo> photos) {
        mPhotoList.clear();
        mPhotoList.addAll(photos);
        notifyDataSetChanged();
    }

    public synchronized void clearAll() {
        mPhotoList.clear();
        notifyDataSetChanged();
    }

    private class PhotoViewHolder extends RecyclerView.ViewHolder {
        private PhotoCardView mCardView;

        public PhotoViewHolder(@NonNull PhotoCardView itemView) {
            super(itemView);
            mCardView = itemView;
        }

        public void update(Photo photo) {
            mCardView.renderPhoto(photo);
        }
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Photo photo);
    }

    public interface OnItemClickListener {
        void onItemClick(Photo photo);
    }
}
