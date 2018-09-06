package com.example.meiniepan.pic2ascii;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.example.meiniepan.pic2ascii.utils.NormalProgressDialog;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    private Bitmap bitmap;
    private String filepath;
    private CompositeDisposable _disposable = new CompositeDisposable();

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

                    if (PictureMimeType.isVideo(localMedia.getPictureType())) {
                        //视频类型
                        createGif(path);
                    } else {
                        //图片类型
                        filepath = CommonUtil.amendRotatePhoto(path, MainActivity.this);
                        bitmap = CommonUtil.createAsciiPic(filepath, MainActivity.this);
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }
    }

    /**
     * 原视频转换为ascii字符gif
     */
    private void createGif(String path) {
        Log.d("MainActivity", "videoPath:" + path);
        NormalProgressDialog.showLoading(this);
        //获取到视频的所有帧图片
        Observable<ArrayList<Bitmap>> observable = CommonUtil
            .backgroundShootVideoThumb(MainActivity.this, path);

        observable.map(new Function<ArrayList<Bitmap>, String>() {
            @Override
            public String apply(ArrayList<Bitmap> bitmaps) {
                List<Bitmap> asciiBitmaps = new ArrayList<>();
                //所有帧图片转换为ascii字符图片
                for (int i = 0; i < bitmaps.size(); i++) {
                    Bitmap asciiBitmap = CommonUtil
                        .createAsciiPic(bitmaps.get(i), MainActivity.this);
                    asciiBitmaps.add(asciiBitmap);
                }
                String gifPath = "";
                try {
                    //所有的ascii字符图片合成为gif图
                    gifPath = CommonUtil.createGif(MainActivity.this, "ascii", asciiBitmaps, 200);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return gifPath;
            }
        })
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<String>() {
                @Override
                public void onSubscribe(Disposable d) {
                    _disposable.add(d);
                }

                @Override
                public void onNext(String gif) {
                    NormalProgressDialog.stopLoading();
                    Log.d("MainActivity", "gifPath:" + gif);
                    Glide.with(MainActivity.this).clear(imageView);
                    Glide.with(MainActivity.this).load(new File(gif)).into(imageView);
                }

                @Override
                public void onError(Throwable e) {
                }

                @Override
                public void onComplete() {
                }
            });
    }

    public void doSave(View view) {
        CommonUtil.saveBitmap2file(bitmap, System.currentTimeMillis() + "", MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (_disposable != null && !_disposable.isDisposed()) {
            _disposable.dispose();
            _disposable.clear();
        }
    }
}
