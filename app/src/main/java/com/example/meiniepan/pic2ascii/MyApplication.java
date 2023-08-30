package com.example.meiniepan.pic2ascii;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

import io.microshow.rxffmpeg.RxFFmpegInvoke;

/**
 * @author Burning
 * @description:
 * @date :2019/12/12 0012 15:24
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId
        // 调试时，将第三个参数改为true
        Bugly.init(this, "38e029a0d2", true);
        RxFFmpegInvoke.getInstance().setDebug(true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);


        // 安装tinker
//        Beta.installTinker();installTinker
    }

}
