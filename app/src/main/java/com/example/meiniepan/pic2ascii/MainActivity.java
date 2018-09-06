package com.example.meiniepan.pic2ascii;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.Constant;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    private Bitmap bitmap;
    private String filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.image);
        bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ddd);

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
                }
                filepath = CommonUtil.amendRotatePhoto(path, MainActivity.this);
//                imageView.setImageBitmap(BitmapFactory.decodeFile(filepath));
                bitmap = CommonUtil.createAsciiPic(filepath, MainActivity.this);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void doSave(View view) {
        CommonUtil.saveBitmap2file(bitmap, MainActivity.this);
    }
}
