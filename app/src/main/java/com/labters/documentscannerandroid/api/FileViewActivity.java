package com.labters.documentscannerandroid.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.labters.documentscannerandroid.PhotoActivity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

public class FileViewActivity extends AsyncTask<String, Void, Bitmap> {
    SubsamplingScaleImageView imageViewPlus;
    ImageView imageView;
    Bitmap defaultBmp;
    private WeakReference<PhotoActivity> photoActivity;

    public void setReference(WeakReference<PhotoActivity> photoActivity){
        this.photoActivity = photoActivity;
    }

    public FileViewActivity(SubsamplingScaleImageView imageViewPlus) {
        this.imageViewPlus = imageViewPlus;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        Bitmap bmp = null;
        String base = urls[0];
        String filename = urls[1];
        URL url;

        try {

            url = new URL(base + filename);
            InputStream inputStream = url.openStream();
            bmp = BitmapFactory.decodeStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bmp;
    }

    protected void onPostExecute(Bitmap result) {
        ImageSource imageSource = ImageSource.bitmap(result);
        imageViewPlus.setImage(imageSource);

        photoActivity.get().hideProgressBar();
    }

}
