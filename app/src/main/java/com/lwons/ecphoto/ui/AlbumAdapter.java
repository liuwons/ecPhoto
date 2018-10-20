package com.lwons.ecphoto.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.lwons.ecphoto.model.Album;
import com.lwons.ecphoto.ui.widget.AlbumCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuwons on 2018/10/20
 */
public class AlbumAdapter extends RecyclerView.Adapter {
    private boolean mAllLoaded = false;
    private List<Album> mAlbumList;

    public AlbumAdapter() {
        mAlbumList = new ArrayList<>();
    }

    @NonNull
    @Override
    public synchronized RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        AlbumCardView cardView = new AlbumCardView(parent.getContext());
        return new AlbumViewHolder(cardView);
    }

    @Override
    public synchronized void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ((AlbumViewHolder)viewHolder).update(mAlbumList.get(i));
    }

    @Override
    public synchronized int getItemCount() {
        return mAlbumList.size();
    }

    public synchronized void addAlbums(List<Album> albums) {
        int start = mAlbumList.size();
        mAlbumList.addAll(albums);
        notifyItemRangeChanged(start, albums.size());
    }

    public synchronized void clearAll() {
        mAlbumList.clear();
        mAllLoaded = false;
    }

    public synchronized void loadFinish() {
        mAllLoaded = true;
    }

    private class AlbumViewHolder extends RecyclerView.ViewHolder {
        private AlbumCardView mCardView;

        public AlbumViewHolder(@NonNull AlbumCardView itemView) {
            super(itemView);
            mCardView = itemView;
        }

        public void update(Album album) {
            mCardView.renderAlbum(album);
        }
    }
}
