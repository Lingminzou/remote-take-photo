package com.example.android.camera2basic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;


public class PostPhoto {

    private static final String TAG = "PostPhoto";

    private String photoPath;

    public PostPhoto(String photoPath) {

        Log.d(TAG, "post photo: " + photoPath);

        this.photoPath = photoPath;

        new Thread(new handleThread()).start();
    }

    private class handleThread implements Runnable {

        @Override
        public void run() {

            String outPath = photoPath.replace("pic.jpg", "post.jpg");

            compressImage(photoPath, outPath);

            try {
                postPhoto(outPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int computeScale(BitmapFactory.Options options, int viewWidth, int viewHeight){
        int inSampleSize = 1;
        if(viewWidth == 0 || viewWidth == 0){
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;

        Log.d(TAG, "computeScale Width = " + String.valueOf(bitmapWidth) + " " + "Height = " + String.valueOf(bitmapHeight));

        //假如Bitmap的宽度或高度大于我们设定图片的View的宽高，则计算缩放比例
        if(bitmapWidth > viewWidth || bitmapHeight > viewWidth){
            int widthScale = Math.round((float) bitmapWidth / (float) viewWidth);
            int heightScale = Math.round((float) bitmapHeight / (float) viewWidth);

            //为了保证图片不缩放变形，我们取宽高比例最小的那个
            inSampleSize = widthScale < heightScale ? widthScale : heightScale;
        }

        Log.d(TAG, "inSampleSize = " + String.valueOf(inSampleSize));

        return inSampleSize;
    }

    private void compressImage(String inPath, String outPath) {

        final int outWidth = 720;
        final int outHeight = 1280;

        /* 先进行尺寸的压缩 */
        BitmapFactory.Options options = new BitmapFactory.Options();
        //设置为true,表示解析Bitmap对象，该对象不占内存
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(inPath, options);
        //设置缩放比例
        options.inSampleSize = computeScale(options, outWidth, outHeight);

        //设置为false,解析Bitmap对象加入到内存中
        options.inJustDecodeBounds = false;

        Bitmap bitmap =  BitmapFactory.decodeFile(inPath, options);

        /* 再对图像质量进行压缩 */
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, os);

        bitmap.recycle();
        bitmap = null;

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(outPath);
            fos.write(os.toByteArray());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void postPhoto(String path) throws Exception {
        //URL url = new URL("http://httpbin.org/post");
        URL url = new URL("http://23.95.214.128:8080");

        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //添加请求头
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "hello");
        con.setRequestProperty("Content-Type", "image/jpeg");

        //发送Post请求
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());

        /* 读取文件输出 */
        FileInputStream file = new FileInputStream(path);
        BufferedInputStream bis = new BufferedInputStream(file);
        byte[] buf= new byte[1024];
        int length = 0;
        length = bis.read(buf);
        while(length != -1) {
            wr.write(buf, 0, length);
            length = bis.read(buf);
        }
        bis.close();
        file.close();

        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
    }
}
