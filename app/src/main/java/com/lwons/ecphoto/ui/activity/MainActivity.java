package com.lwons.ecphoto.ui.activity;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;

import com.lwons.ecphoto.R;
import com.lwons.ecphoto.data.DataManager;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private FloatingActionButton mFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mFloatingActionButton = findViewById(R.id.float_action_button);
        mFloatingActionButton.setOnClickListener(this);
    }

    private void onFloatingActionButtonClicked() {
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
                        DataManager.getInstance().addAlbum(name)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Boolean>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                    }

                                    @Override
                                    public void onNext(Boolean aBoolean) {
                                        if (aBoolean) {
                                            showAlert("succeed");
                                        } else {
                                            showAlert("failed");
                                        }
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
}
