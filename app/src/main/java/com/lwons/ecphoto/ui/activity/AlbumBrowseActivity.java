package com.lwons.ecphoto.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.imnjh.imagepicker.SImagePicker;
import com.imnjh.imagepicker.activity.PhotoPickerActivity;
import com.lwons.ecphoto.R;
import com.lwons.ecphoto.model.Photo;
import com.lwons.ecphoto.neo.Neo;
import com.lwons.ecphoto.ui.PhotoAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by liuwons on 2018/10/19
 */
public class AlbumBrowseActivity extends AppCompatActivity implements View.OnClickListener, PhotoAdapter.OnItemClickListener, PhotoAdapter.OnItemLongClickListener {
    public static final String ALBUM_NAME = "album_name";
    public static final String ALBUM_ID = "album_id";

    private static final int REQ_CODE_PICK_IMAGE = 5650;

    private static final int IMAGE_PICKER_ONCE_MAX_COUNT = 10;

    private static final int COL_COUNT = 4;

    private FloatingActionButton mFloatingActionButton;
    private RecyclerView mPhotoRecycler;
    private PhotoAdapter mPhotoAdapter;

    private String mAlbumName;
    private String mAlbumId;
    private List<Photo> mPhotos;

    private Disposable mPhotoDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_browse);

        mPhotos = new ArrayList<>();

        mPhotoRecycler = findViewById(R.id.photo_list);
        GridLayoutManager layoutManager = new GridLayoutManager(this, COL_COUNT);
        mPhotoRecycler.setLayoutManager(layoutManager);
        mPhotoAdapter = new PhotoAdapter();
        mPhotoAdapter.setOnItemClickListener(this);
        mPhotoAdapter.setOnItemLongClickListener(this);
        mPhotoRecycler.setAdapter(mPhotoAdapter);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mFloatingActionButton = findViewById(R.id.float_action_button);
        mFloatingActionButton.setOnClickListener(this);

        mAlbumName = getIntent().getStringExtra(ALBUM_NAME);
        mAlbumId = getIntent().getStringExtra(ALBUM_ID);
        if (TextUtils.isEmpty(mAlbumName)) {
            showError("internal error");
            return;
        }

        observeAlbum();
    }

    private void observeAlbum() {
        if (mPhotoDisposable != null && !mPhotoDisposable.isDisposed()) {
            mPhotoDisposable.dispose();
        }
        Neo.getInstance().loadPhotos(mAlbumId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Photo>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mPhotoDisposable = d;
                    }

                    @Override
                    public void onNext(List<Photo> photos) {
                        mPhotoAdapter.setPhotos(photos);
                        mPhotos = photos;
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void pickPhoto() {
        SImagePicker
                .from(this)
                .rowCount(3)
                .maxCount(IMAGE_PICKER_ONCE_MAX_COUNT)
                .showCamera(true)
                .showOriginal(false)
                .pickMode(SImagePicker.MODE_IMAGE)
                .pickText(R.string.image_picker_select)
                .forResult(REQ_CODE_PICK_IMAGE);
    }

    private void showError(String message) {

    }

    @Override
    public void onClick(View v) {
        if (v == mFloatingActionButton) {
            pickPhoto();
        }
    }

    private void onAddPhotos(List<String> photoUris) {
        for (String uri : photoUris) {
            Neo.getInstance().encryptPhoto(this, mAlbumId, uri)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Integer integer) {
                            Log.e("liuwons", "update:" + integer);
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Log.e("liuwons", "encryptPhoto error");
                        }

                        @Override
                        public void onComplete() {
                            Log.e("liuwons", "encryptPhoto succeed");
                        }
                    });
        }
    }

    private void onPickedPhotos(List<String> photos) {
        List<String> uris = new ArrayList<>(photos.size());
        for (String path : photos) {
            if (!TextUtils.isEmpty(path)) {
                if (path.startsWith("/")) {
                    uris.add("file://" + path);
                } else {
                    uris.add(path);
                }
            }
        }
        onAddPhotos(uris);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQ_CODE_PICK_IMAGE) {
            final ArrayList<String> pathList =
                    data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT_SELECTION);
            onPickedPhotos(pathList);
        }
    }

    @Override
    public void onItemClick(Photo photo) {
        Intent intent = new Intent(this, PhotoBrowseActivity.class);
        intent.putStringArrayListExtra(PhotoBrowseActivity.KEY_PHOTO_URIS, getPhotoUris());
        startActivity(intent);
    }

    private ArrayList<String> getPhotoUris() {
        ArrayList<String> uris = new ArrayList<>();
        for (Photo photo : mPhotos) {
            uris.add(photo.originUri);
        }
        return uris;
    }

    @Override
    public void onItemLongClick(Photo photo) {

    }
}
