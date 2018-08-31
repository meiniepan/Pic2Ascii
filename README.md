抖音上炫代码的不少，有些真的让人叹为观止，作为一个androider，当我看到下面这段舞蹈的时候，终于忍不住了，想要通过android实现一样的效果。


 ![image](https://github.com/meiniepan/Pic2Ascii/blob/master/raw/jilejingtu.gif)

这么好玩的东西，为啥就没有大佬做呢，原因可能有两个，一是真的难，二是出力不讨好，难以达到最终效果，一番尝试后，技术问题都解决了，但并没有达到电脑端美感，手机屏幕还是太小了。。
这是电脑端的静态图


![image](https://github.com/meiniepan/Pic2Ascii/blob/master/raw/ASCII-微信图片_20180821140237.gif)

这是手机端的
![image](https://github.com/meiniepan/Pic2Ascii/blob/master/raw/微信图片_20180830113218.jpg)

还有一个普通的头像，做成ascii图后，简直美到窒息


![image](https://github.com/meiniepan/Pic2Ascii/blob/master/raw/ASCII-微信图片_20180817091237.png)

下面开始分析代码，首先根据图片像素灰度转为ascii字符，这在网上有现成的java代码，android上只需要改一点api就可以，代码如下
       public static Bitmap createAsciiPic(final String path, Context context) {
        final String base = "#8XOHLTI)i=+;:,.";// 字符串由复杂到简单
//        final String base = "#,.0123456789:;@ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";// 字符串由复杂到简单
        StringBuilder text = new StringBuilder();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        Bitmap image = BitmapFactory.decodeFile(path);  //读取图片
        int width0 = image.getWidth();
        int height0 = image.getHeight();
        int width1, height1;
        int scale = 7;
        if (width0 <= width / scale) {
            width1 = width0;
            height1 = height0;
        } else {
            width1 = width / scale;
            height1 = width1 * height0 / width0;
        }
        image = scale(path, width1, height1);  //读取图片
        //输出到指定文件中
        for (int y = 0; y < image.getHeight(); y += 2) {
            for (int x = 0; x < image.getWidth(); x++) {
                final int pixel = image.getPixel(x, y);
                final int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
                final float gray = 0.299f * r + 0.578f * g + 0.114f * b;
                final int index = Math.round(gray * (base.length() + 1) / 255);
                String s = index >= base.length() ? " " : String.valueOf(base.charAt(index));
                text.append(s);
            }
            text.append("\n");
        }
        return textAsBitmap(text, context);
//        return image;
    }


这样处理完得到的ascii文本，但我们需要的是ascii图片，那我们需要怎么做呢，截屏？请读者思考10秒钟，想想自己的解决方案。我这里通过TextPanit和staticLayout实现的，也可以new一个TextView，写入文本，然后把Textview的缓冲区转换为图片，但是这种staticLayout的方式更底层，更有效，代码如下

public static Bitmap textAsBitmap(StringBuilder text, Context context) {

        TextPaint textPaint = new TextPaint();

// textPaint.setARGB(0x31, 0x31, 0x31, 0);

        textPaint.setColor(Color.BLACK);

        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.MONOSPACE);

        textPaint.setTextSize(12);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         //

        StaticLayout layout = new StaticLayout(text, textPaint, width,

                Layout.Alignment.ALIGN_CENTER, 1f, 0.0f, true);

        Bitmap bitmap = Bitmap.createBitmap(layout.getWidth() + 20,

                layout.getHeight() + 20, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        canvas.translate(10, 10);

        canvas.drawColor(Color.WHITE);

//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);//绘制透明色

        layout.draw(canvas);

        Log.d("textAsBitmap",

                String.format("1:%d %d", layout.getWidth(), layout.getHeight()));

        return bitmap;

    }


相对于电脑端有无边无际的txt编辑框，android里text是有字数限制的，所以原始图片如果像素过多的话就要进行尺寸压缩。而且textPaint的这个设置特别重要textPaint.setTypeface(Typeface.MONOSPACE);字体对效果的影响太大了，失之毫厘谬以千里，这是一个大坑，说多了都是时间。
我在项目里集成了一个图片选择库，可以直接把拍的照片转化为ascii图，碰到一个问题就是拍照图片拿到后都会自动旋转90度，很是困惑，虽然找到了处理方法，但系统为啥要作旋转处理，还请知道的大神告知原因。处理代码如下

/**
     * 读取照片旋转角度
     *
     * @param path 照片路径
     * @return 角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }


public static String amendRotatePhoto(String originpath, Context context) {

        // 取得图片旋转角度
        int angle = readPictureDegree(originpath);

        // 把原图压缩后得到Bitmap对象
        if (angle != 0) {
            Bitmap bmp = getCompressPhoto(originpath);
            Bitmap bitmap = rotaingImageView(angle, bmp);
            return savePhotoToSD(bitmap, context);
        } else {
            return originpath;
        }

    }


public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        Bitmap returnBm = null;
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            bitmap.recycle();
        }
        return returnBm;
    }

按说拿到ascii图后，想要把整个视频转换成ascii字符视频就很简单了。只要把视频逐帧抽成图片，图片转换后，再合成为视频播放出来，但我视频库用的不多，希望有能力的朋友可以帮助完成最后一步。
最后，也希望朋友们能把一些有趣的想法实践到android项目中来，让搬砖之余，有更多的乐趣。
