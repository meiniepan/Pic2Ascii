package com.example.meiniepan.pic2ascii;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.gs.buluo.common.widget.LoadingDialog;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.Constant;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    private Bitmap bitmap;
    private String filepath;
    String path = "";
    private int CHOOSE_REQUEST_COLOR = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image);
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ddd);

    }

    public void doPick(View view) {
        CommonUtil.choosePhoto(this, PictureConfig.CHOOSE_REQUEST);
    }

    public void doPick2(View view) {
        CommonUtil.choosePhoto(this, CHOOSE_REQUEST_COLOR);
    }

    @SuppressLint("CheckResult")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                String path = "";
                if (selectList != null && selectList.size() > 0) {
                    LocalMedia localMedia = selectList.get(0);
                    if (localMedia.isCompressed()) {
                        path = localMedia.getCompressPath();
                    } else if (localMedia.isCut()) {
                        path = localMedia.getCutPath();
                    } else {
                        path = localMedia.getPath();
                    }
                }
                filepath = CommonUtil.amendRotatePhoto(path, MainActivity.this);
//                imageView.setImageBitmap(BitmapFactory.decodeFile(filepath));
                bitmap = CommonUtil.createAsciiPic(filepath, MainActivity.this);
                imageView.setImageBitmap(bitmap);
            } else if (requestCode == CHOOSE_REQUEST_COLOR) {
                List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);

                if (selectList != null && selectList.size() > 0) {
                    LocalMedia localMedia = selectList.get(0);
                    if (localMedia.isCompressed()) {
                        path = localMedia.getCompressPath();
                    } else if (localMedia.isCut()) {
                        path = localMedia.getCutPath();
                    } else {
                        path = localMedia.getPath();
                    }
                }

                LoadingDialog.getInstance().show(MainActivity.this,"处理中", false);
                Observable.fromCallable(() -> {
                    filepath = CommonUtil.amendRotatePhoto(path, MainActivity.this);
                    bitmap = CommonUtil.createAsciiPicColor(filepath, MainActivity.this);
                    return bitmap;
                }).compose(switchSchedulers()).subscribeWith(new DisposableObserver<Bitmap>() {

                    @Override
                    public void onNext(Bitmap bitmap) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        imageView.setImageBitmap(bitmap);
                        LoadingDialog.getInstance().dismissDialog();
                    }
                });
            }
        }
    }

    public void doSave(View view) {
        CommonUtil.saveBitmap2file(bitmap, MainActivity.this);
    }

    public static <T> ObservableTransformer<T, T> switchSchedulers() {
        return upstream -> upstream.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).doOnSubscribe(disposable -> {
        }).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread());
    }

    public void doReward(View view) {
        bitmap =  BitmapFactory.decodeResource(getResources(),R.drawable.reward);
        imageView.setImageBitmap(bitmap);
    }
}
