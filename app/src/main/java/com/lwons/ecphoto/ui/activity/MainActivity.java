package com.lwons.ecphoto.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;

import com.lwons.ecphoto.R;
import com.lwons.ecphoto.model.Album;
import com.lwons.ecphoto.neo.Neo;
import com.lwons.ecphoto.ui.AlbumAdapter;
import com.lwons.ecphoto.ui.menu.MenuAdapter;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AlbumAdapter.OnItemLongClickListener, AlbumAdapter.OnItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int ALBUM_COLUMN = 2;

    private static final int REQ_READ_EXTERNAL_STORAGE = 5648;
    private static final int REQ_WRITE_EXTERNAL_STORAGE = 5649;

    private FloatingActionButton mFloatingActionButton;
    private RelativeLayout mLoadingHolder;
    private RelativeLayout mErrorHolder;

    private RecyclerView mAlbumRecycler;
    private AlbumAdapter mAlbumAdapter;
    private GridLayoutManager mAlbumLayoutManager;

    private ExpandableListView mMenuView;
    private MenuAdapter mMenuAdapter;

    private Disposable mAlbumDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        if (permissionCheck()) {
            onPermissionGet();
        }
    }

    private void initViews() {
        mLoadingHolder = findViewById(R.id.loading_holder);
        mErrorHolder = findViewById(R.id.error_holder);

        mAlbumRecycler = findViewById(R.id.album_list);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mFloatingActionButton = findViewById(R.id.float_action_button);
        mFloatingActionButton.setOnClickListener(this);

        mAlbumAdapter = new AlbumAdapter();
        mAlbumAdapter.setOnItemClickListener(this);
        mAlbumAdapter.setOnLongClickListener(this);
        mAlbumLayoutManager = new GridLayoutManager(this, ALBUM_COLUMN);
        mAlbumRecycler.setLayoutManager(mAlbumLayoutManager);
        mAlbumRecycler.setAdapter(mAlbumAdapter);

        mMenuView = findViewById(R.id.main_menu);
        View menuHeader = LayoutInflater.from(this).inflate(R.layout.menu_header, null);
        mMenuView.addHeaderView(menuHeader);
        mMenuAdapter = new MenuAdapter();
        mMenuAdapter.loadMenu(MenuAdapter.getDefaultMenu(this));
        mMenuView.setAdapter(mMenuAdapter);
    }

    private void onPermissionGet() {
        Neo.getInstance().init(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            try {
                                Neo.getInstance().setAuth("lwons", "test");
                            } catch (Exception e) {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setMessage(getResources().getString(R.string.error_encrypt_not_support))
                                        .show();
                            }
                            if (Neo.getInstance().encryptInited()) {
                                onPrepared();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void onPrepared() {
        mLoadingHolder.setVisibility(View.GONE);

        observeAlbums();
    }

    private void observeAlbums() {
        if (mAlbumDisposable != null && !mAlbumDisposable.isDisposed()) {
            mAlbumDisposable.dispose();
        }
        Neo.getInstance().loadAllAlbums()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Album>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mAlbumDisposable = d;
                    }

                    @Override
                    public void onNext(List<Album> albums) {
                        mAlbumAdapter.setAlbums(albums);
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void onFloatingActionButtonClicked() {
        if (!Neo.getInstance().inited()) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.error_app_not_init))
                    .show();
            return;
        }
        if (!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                || !checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.error_no_permission))
                    .show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.create_album_dialog_content, null);

        new AlertDialog.Builder(this)
                .setView(view)
                .setTitle(R.string.dialog_create_album_title)
                .setPositiveButton(R.string.dialog_create_album_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextInputEditText editText = view.findViewById(R.id.text_input);
                        String name = editText.getText().toString();
                        Neo.getInstance().addAlbum(name)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Album>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                    }

                                    @Override
                                    public void onNext(Album album) {
                                        showAlert("succeed");
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        showAlert("error");
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });
                    }
                })
                .setNegativeButton(R.string.dialog_create_album_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .show();
    }

    @Override
    public void onClick(View v) {
        if (v == mFloatingActionButton) {
            onFloatingActionButtonClicked();
        }
    }

    private boolean permissionCheck() {
        if (!checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                    getResources().getString(R.string.permission_read_external_storage),
                    REQ_READ_EXTERNAL_STORAGE);
            return false;
        }
        if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    getResources().getString(R.string.permission_write_external_storage),
                    REQ_WRITE_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(final String permission, String explain, final int reqCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            new AlertDialog.Builder(this).setMessage(explain)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, reqCode);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, reqCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_READ_EXTERNAL_STORAGE || requestCode == REQ_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissionCheck()) {
                    onPermissionGet();
                }
            }
        }
    }

    @Override
    public void onItemLongClick(Album album) {

    }

    @Override
    public void onItemClick(Album album) {
        Intent intent = new Intent(this, AlbumBrowseActivity.class);
        intent.putExtra(AlbumBrowseActivity.ALBUM_NAME, album.name);
        intent.putExtra(AlbumBrowseActivity.ALBUM_ID, album.id);
        startActivity(intent);
    }
}
