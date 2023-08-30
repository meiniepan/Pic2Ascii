package com.example.meiniepan.pic2ascii;

import static com.example.meiniepan.pic2ascii.CommonUtil.generateFileName;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.microshow.rxffmpeg.RxFFmpegCommandList;
import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    private Bitmap bitmap;
    private String filepath;
    String path = "";
    private int CHOOSE_REQUEST_COLOR = 500;
    private int CHOOSE_REQUEST_VIDEO = 600;
    private MyRxFFmpegSubscriber myRxFFmpegSubscriber;
    private static MyRxFFmpegSubscriber2 myRxFFmpegSubscriber2;
    private static final String SD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    private static String asciiFramesFolder;
    private static File file1;
    private File file2;
    private static String outputVideoPath;
    private static String framesFolder;
    private static String tempFolder;
    private String input0;
    private String gifPath;
    private File file3;
    private String filesDir;
    ProgressBar progressBar;
    private boolean isGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = getApplicationContext();
        filesDir = context.getFilesDir().getAbsolutePath() + File.separator;
        imageView = findViewById(R.id.image);
        progressBar = findViewById(R.id.pb);
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.aaa);

    }

    public void doPick(View view) {
        CommonUtil.choosePhoto(this, PictureConfig.CHOOSE_REQUEST);
    }

    public void doPickVideo(View view) {
        isGif = false;
        CommonUtil.chooseVideo(this, CHOOSE_REQUEST_VIDEO);
    }

    public void doPickGif(View view) {
        isGif = true;
        CommonUtil.chooseVideo(this, CHOOSE_REQUEST_VIDEO);
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

                Toast.makeText(this, "处理中", Toast.LENGTH_SHORT).show();
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
                    }
                });
            } else if (requestCode == CHOOSE_REQUEST_VIDEO) {
                progressBar.setVisibility(View.VISIBLE);

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
                runFFmpegRxJava(path);
            }
        }
    }


    @SuppressLint("CheckResult")
    public void doSave(View view) {
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        CommonUtil.saveBitmap2file(bitmap, MainActivity.this);
                    } else {
                        // Oups permission denied
                        Toast.makeText(this, "未打开存储权限", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public static <T> ObservableTransformer<T, T> switchSchedulers() {
        return upstream -> upstream.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io()).doOnSubscribe(disposable -> {
        }).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread());
    }

    public void doReward(View view) {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.reward);
        imageView.setImageBitmap(bitmap);
    }

    public void initPermission() {
        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .requestEach(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(permission -> { // will emit 2 Permission objects
                    if (permission.granted) {
                        // `permission.name` is granted !
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        finish();
                    } else {
                        finish();
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void runFFmpegRxJava(String inputPath) {


        Observable.fromCallable(() -> {
            String fileName = generateFileName() + ".mp4";
            outputVideoPath = SD_PATH + fileName;
            gifPath = SD_PATH + generateFileName() + ".gif";
            File filePic = new File(outputVideoPath);


            framesFolder = filesDir + "1";
            asciiFramesFolder = filesDir + "2";
            tempFolder = filesDir + "3";
            input0 = tempFolder + "/aaa.png";
            File file0 = new File(input0);
            file1 = new File(framesFolder);
            file2 = new File(asciiFramesFolder);
            file3 = new File(tempFolder);
            if (!file1.exists()) {
                file1.mkdirs();
            }


            if (!file2.exists()) {
                file2.mkdirs();
            }
            deleteDirectory(file1);
            deleteDirectory(file2);
            if (!file3.exists()) {
                file3.mkdirs();
            }
            deleteDirectory(file3);
            try {
//            if (!filePic.exists()) {
//                filePic.getParentFile().mkdirs();
//                filePic.createNewFile();
//            }
                if (!file0.exists()) {
                    file0.getParentFile().mkdirs();
                    file0.createNewFile();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // 提取视频帧
            String extractFramesCommand = "ffmpeg -i " + inputPath + " " + framesFolder + "/frame_%04d.bmp";
            String[] commands1 = extractFramesCommand.split(" ");


            myRxFFmpegSubscriber = new MyRxFFmpegSubscriber(this);
            //开始执行FFmpeg命令


            RxFFmpegCommandList ffmpegCommand = new RxFFmpegCommandList();
            ffmpegCommand.add("-framerate");
            ffmpegCommand.add("20");
            ffmpegCommand.add("-i");
            ffmpegCommand.add(asciiFramesFolder + "/frame_%04d.png");
            ffmpegCommand.add("-vf");
            ffmpegCommand.add("palettegen");
            ffmpegCommand.add("-y");
            ffmpegCommand.add(input0);

            RxFFmpegCommandList ffmpegCommand2 = new RxFFmpegCommandList();
            ffmpegCommand2.add("-framerate");
            ffmpegCommand2.add("20");
            ffmpegCommand2.add("-i");
            ffmpegCommand2.add(asciiFramesFolder + "/frame_%04d.png");
            ffmpegCommand2.add("-i");
            ffmpegCommand2.add(input0);
            ffmpegCommand2.add("-filter_complex");
            ffmpegCommand2.add("paletteuse");
            ffmpegCommand2.add(gifPath);

            RxFFmpegInvoke.getInstance()
                    .runCommand(commands1, null);

            File[] files = file1.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    Bitmap bitmap1 = CommonUtil.createAsciiPic2(files[i], MainActivity.this);
                    String path = asciiFramesFolder + "/" + files[i].getName();
                    CommonUtil.saveBitmap(bitmap1, MainActivity.this, path);
                }

            }

            String ffmpegCommand3 = "ffmpeg -framerate " + 24 + " -i " + asciiFramesFolder + "/frame_%04d.png -c:v libx264 -preset ultrafast -crf 18 -pix_fmt yuv420p " + outputVideoPath;
            String[] commands3 = ffmpegCommand3.split(" ");

            if (isGif) {
                RxFFmpegInvoke.getInstance().runCommand(ffmpegCommand.build(), null);
                RxFFmpegInvoke.getInstance().runCommand(ffmpegCommand2.build(), null);
            } else {
                RxFFmpegInvoke.getInstance()
                        .runCommand(commands3, null);
            }


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
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "处理完成", Toast.LENGTH_SHORT).show();
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();


    }

    private String getNum(int i) {
        String r = "frame_" + i;
        if (i >= 1000) {
            r = r;
        } else if (i >= 100) {
            r = "frame_0" + i;
        } else if (i >= 10) {
            r = "frame_00" + i;
        } else {
            r = "frame_000" + i;
        }
        return r;
    }

    public class MyRxFFmpegSubscriber extends RxFFmpegSubscriber {

        private WeakReference<MainActivity> mWeakReference;

        public MyRxFFmpegSubscriber(MainActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onFinish() {
            final MainActivity mactivity = mWeakReference.get();


//            String ffmpegCommand = "ffmpeg -framerate " + 24 + " -i " + asciiFramesFolder + "/frame_%04d.bmp -c:v libx264 -preset ultrafast -crf 18 -pix_fmt yuv420p " + outputVideoPath;
//            String[] commands3 = ffmpegCommand.split(" ");
//
//            myRxFFmpegSubscriber2 = new MyRxFFmpegSubscriber2(mactivity);
//            RxFFmpegInvoke.getInstance()
//                    .runCommandRxJava(commands3)
//                    .subscribe(myRxFFmpegSubscriber2);
            RxFFmpegCommandList ffmpegCommand = new RxFFmpegCommandList();
            ffmpegCommand.add("-framerate");
            ffmpegCommand.add("10");
            ffmpegCommand.add("-i");
            ffmpegCommand.add(asciiFramesFolder + "/frame_%04d.png");
            ffmpegCommand.add("-vf");
            ffmpegCommand.add("palettegen");
            ffmpegCommand.add("-y");
            ffmpegCommand.add(input0);

            RxFFmpegCommandList ffmpegCommand2 = new RxFFmpegCommandList();
            ffmpegCommand2.add("-framerate");
            ffmpegCommand2.add("10");
            ffmpegCommand2.add("-i");
            ffmpegCommand2.add(asciiFramesFolder + "/frame_%04d.png");
            ffmpegCommand2.add("-i");
            ffmpegCommand2.add(input0);
            ffmpegCommand2.add("-filter_complex");
            ffmpegCommand2.add("paletteuse");
            ffmpegCommand2.add(gifPath);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    RxFFmpegInvoke.getInstance().runCommand(ffmpegCommand.build(), null);
                    RxFFmpegInvoke.getInstance().runCommand(ffmpegCommand2.build(), null);
                }
            }, 5000);

//            convertAsciiFramesToVideo(asciiFramesFolder, outputVideoPath);
            if (mactivity != null) {
                mactivity.cancelProgressDialog("处理成功");
            }
        }

        @Override
        public void onProgress(int progress, long progressTime) {
            final MainActivity mactivity = mWeakReference.get();
            if (mactivity != null) {
                //progressTime 可以在结合视频总时长去计算合适的进度值
                mactivity.setProgressDialog(progress, progressTime);
            }
        }

        @Override
        public void onCancel() {
            final MainActivity mactivity = mWeakReference.get();
            if (mactivity != null) {
                mactivity.cancelProgressDialog("已取消");
            }
        }

        @Override
        public void onError(String message) {
            final MainActivity mactivity = mWeakReference.get();
            if (mactivity != null) {
                mactivity.cancelProgressDialog("出错了 onError：" + message);
            }
            Log.e("TAG===", "onError: " + message);
        }
    }

    public class MyRxFFmpegSubscriber2 extends RxFFmpegSubscriber {

        private WeakReference<MainActivity> mWeakReference;

        public MyRxFFmpegSubscriber2(MainActivity activity) {
            mWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onFinish() {
            final MainActivity mactivity = mWeakReference.get();

            if (mactivity != null) {
                mactivity.cancelProgressDialog("处理成功");
            }
        }

        @Override
        public void onProgress(int progress, long progressTime) {
            final MainActivity mactivity = mWeakReference.get();
            if (mactivity != null) {
                //progressTime 可以在结合视频总时长去计算合适的进度值
                mactivity.setProgressDialog(progress, progressTime);
            }
        }

        @Override
        public void onCancel() {
            final MainActivity mactivity = mWeakReference.get();
            if (mactivity != null) {
                mactivity.cancelProgressDialog("已取消");
            }
        }

        @Override
        public void onError(String message) {
            final MainActivity mactivity = mWeakReference.get();
            if (mactivity != null) {
                mactivity.cancelProgressDialog("出错了 onError：" + message);
            }
            Log.e("TAG===", "onError: " + message);
        }
    }

    private void cancelProgressDialog(String s) {
        Toast.makeText(this, "处理完成", Toast.LENGTH_SHORT).show();
    }

    private void setProgressDialog(int progress, long progressTime) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myRxFFmpegSubscriber != null) {
            myRxFFmpegSubscriber.dispose();
        }
    }

    public static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
    }


    private void convertAsciiFramesToVideo(String inputFolder, String outputVideoPath) {
        RxFFmpegCommandList ffmpegCommand = new RxFFmpegCommandList();
        ffmpegCommand.add("-r");
        ffmpegCommand.add("24");
        ffmpegCommand.add("-i");
        ffmpegCommand.add(inputFolder + "/frame_%04d.png");
        ffmpegCommand.add("-pix_fmt");
        ffmpegCommand.add("yuv420p");
        ffmpegCommand.add(outputVideoPath);

        myRxFFmpegSubscriber2 = new MyRxFFmpegSubscriber2(this);
        //开始执行FFmpeg命令
        RxFFmpegInvoke.getInstance()
                .runCommandRxJava(ffmpegCommand.build())
                .subscribe(myRxFFmpegSubscriber2);
    }
}
