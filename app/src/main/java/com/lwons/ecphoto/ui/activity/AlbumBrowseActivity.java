package com.lwons.ecphoto.ui.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.imnjh.imagepicker.SImagePicker;
import com.lwons.ecphoto.R;
import com.lwons.ecphoto.model.Photo;
import com.lwons.ecphoto.neo.Neo;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by liuwons on 2018/10/19
 */
public class AlbumBrowseActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String ALBUM_NAME = "album_name";

    private FloatingActionButton mFloatingActionButton;
    private RecyclerView mPhotoRecycler;

    private String mAlbumName;

    private Disposable mAlbumDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_browse);

        mPhotoRecycler = findViewById(R.id.album_list);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mFloatingActionButton = findViewById(R.id.float_action_button);
        mFloatingActionButton.setOnClickListener(this);

        mAlbumName = getIntent().getStringExtra(ALBUM_NAME);
        if (TextUtils.isEmpty(mAlbumName)) {
            showError("internal error");
            return;
        }

        observeAlbum();
    }

    private void observeAlbum() {
        if (mAlbumDisposable != null && !mAlbumDisposable.isDisposed()) {
            mAlbumDisposable.dispose();
        }
        Neo.getInstance().loadPhotos(mAlbumName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Photo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mAlbumDisposable = d;
                    }

                    @Override
                    public void onNext(List<Photo> photos) {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void addPhoto() {
        SImagePicker
                .from(this)
                .maxCount(9)
                .rowCount(3)
                .pickMode(SImagePicker.MODE_IMAGE)
                .forResult(0);
    }

    private void showError(String message) {

    }

    @Override
    public void onClick(View v) {
        if (v == mFloatingActionButton) {
            addPhoto();
        }
    }
}
