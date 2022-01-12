package com.labters.documentscannerandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.labters.documentscannerandroid.api.ConvertAPI;
import com.labters.documentscannerandroid.api.FileViewActivity;
import com.labters.documentscannerandroid.api.convert_model.request.NewConversionRequestBody;
import com.labters.documentscannerandroid.api.convert_model.response.StartANewConvertion;
import com.labters.documentscannerandroid.api.convert_model.response.StatusOfTheConversion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class PhotoActivity extends AppCompatActivity {

    private static final String BASE_URL = "https://filexch.tk/static/";
    private static final String CON_URL = "https://api.convertio.co";
    private SubsamplingScaleImageView imageView;
    private Toolbar toolbar;
    private Bitmap photo;
    private static String filename;
    private boolean isToolbarVisible;
    private TextView saveImg;

    public static void start(Context caller, String filename){
        Intent intent = new Intent(caller, PhotoActivity.class);
        caller.startActivity(intent);
        PhotoActivity.filename = filename;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_photo);

        imageView = findViewById(R.id.image);

        toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        Bitmap defaultPDFBmp = BitmapFactory.decodeResource(getResources()
                , R.drawable.pdf_icon);

        Bitmap defaultDOCBmp = BitmapFactory.decodeResource(getResources()
                , R.drawable.doc_icon);

        if (filename.endsWith(".pdf")){
            ImageSource imageSource = ImageSource.bitmap(defaultPDFBmp);
            imageView.setImage(imageSource);
            hideProgressBar();

        } else if (filename.endsWith(".doc")){
            ImageSource imageSource = ImageSource.bitmap(defaultDOCBmp);
            imageView.setImage(imageSource);
            hideProgressBar();
        } else{
            FileViewActivity fileViewActivity = new FileViewActivity(imageView);
            fileViewActivity.setReference(new WeakReference<>(PhotoActivity.this));
            fileViewActivity.execute(BASE_URL, filename);
            Log.e("TAG", "viewed");
        }

        toolbar.post(() ->{
            if (!isFinishing()) {
                hideActionBar();
            }
        });


        imageView.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() != MotionEvent.ACTION_DOWN
                    && motionEvent.getAction() != MotionEvent.ACTION_UP)
                     {
                if (isToolbarVisible) {
                    hideActionBar();
                }
            }
            return false;
        });


        imageView.setOnClickListener(v -> {
            if (!isToolbarVisible) {
                showActionBar();
            }
        });
    }

    private void showActionBar() {
        toolbar.animate().translationY(0).setDuration(300).start();
        isToolbarVisible = true;
    }

    private void hideActionBar() {
        toolbar.animate().translationY(-toolbar.getHeight()).setDuration(300).start();
        isToolbarVisible = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save){

            showProgressBar();
            SaveFile saveFile = new SaveFile();
            saveFile.setReference(new WeakReference<>(PhotoActivity.this));
            saveFile.execute(BASE_URL, filename);

            hideActionBar();
        } else if (item.getItemId() == R.id.action_save_doc && filename.endsWith(".pdf")){

            //String CON_URL = "https://api.convertio.co";
            showProgressBar();
            CompositeDisposable disposable = new CompositeDisposable();

            String apikey = "d74a402429cdb5c09b583b91d7f75779";
            //"e4964fb1f981a0bc786dadf74e671352"
            String file = BASE_URL + filename;
            String outputformat = "doc";

            NewConversionRequestBody newConversionRequestBody = new NewConversionRequestBody();
            newConversionRequestBody.apikey = apikey;
            newConversionRequestBody.input = "url";
            newConversionRequestBody.file = file;
            newConversionRequestBody.outputformat = outputformat;

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(CON_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            ConvertAPI convertAPI = retrofit.create(ConvertAPI.class);

            final String[] newId = new String[1];

            disposable.add(convertAPI.getId(newConversionRequestBody)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BiConsumer<StartANewConvertion, Throwable>() {
                        @Override
                        public void accept(StartANewConvertion startANewConvertion, Throwable throwable) throws Exception {
                            if (startANewConvertion == null){
                                Toast.makeText(getApplicationContext(), "get error", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                String id = startANewConvertion.getData().getId();

                                Thread.sleep(2000);

                                CompositeDisposable secondDisposable = new CompositeDisposable();

                                secondDisposable.add(convertAPI.getUrl(id)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new BiConsumer<StatusOfTheConversion, Throwable>() {
                                            @Override
                                            public void accept(StatusOfTheConversion statusOfTheConversion, Throwable throwable) throws Exception {

                                                if (statusOfTheConversion == null){
                                                    Toast.makeText(getApplicationContext(), "get url error", Toast.LENGTH_LONG).show();
                                                    return;
                                                }
                                                String url = statusOfTheConversion.getData().getOutput().getUrl();

                                                showProgressBar();

                                                Intent browserIntent = new
                                                        Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                startActivity(browserIntent);

                                                savingResult(200);
                                            }


                                        }));
                            }
                        }
                    }));

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
        return super.onOptionsItemSelected(item);
    }

    public void saveDoc(ConvertAPI convertAPI, String id) {
        CompositeDisposable disposable = new CompositeDisposable();

        disposable.add(convertAPI.getUrl(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BiConsumer<StatusOfTheConversion, Throwable>() {
                    @Override
                    public void accept(StatusOfTheConversion statusOfTheConversion, Throwable throwable) throws Exception {

                        String url = statusOfTheConversion.getData().getOutput().getUrl();

                        showProgressBar();

                        Intent browserIntent = new
                                Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);

                        savingResult(200);
                    }
                }));
    }

    public void hideProgressBar(){
        findViewById(R.id.progress).setVisibility(View.GONE);
    }

    public void showProgressBar(){
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void savingResult(Integer code){
        switch (code){
            case 1:
                Toast.makeText(this, "file " + filename + " saved",
                        Toast.LENGTH_LONG).show();
                hideProgressBar();
                break;
            case 0:
                Toast.makeText(this, "SAVING ERROR",
                    Toast.LENGTH_LONG).show();
                break;
            case 200:
                Toast.makeText(this, "Browser opened",
                        Toast.LENGTH_LONG).show();
                hideProgressBar();
                break;


        }

    }

    public static class SaveFile extends AsyncTask<String, Integer, Integer> {
        private Bitmap defaultBmp;
        private String base;
        private String filename;
        private URL url;
        private SubsamplingScaleImageView imageViewPlus;
        private WeakReference<PhotoActivity> photoActivity;

        public void setReference(WeakReference<PhotoActivity> photoActivity){
            this.photoActivity = photoActivity;
        }

        @Override
        protected Integer doInBackground(String... urls) {
            try {
                base = urls[0];
                filename = urls[1];

                url = new URL(base + filename);
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                File basePath = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);

                String savePath = basePath +"/"+filename;

                FileOutputStream fos = new FileOutputStream(savePath);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (IOException e){
                e.printStackTrace();
                return 500;
            }
            return 200;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
                try {
                    if (integer == 200){}
                        photoActivity.get().savingResult(1);

                    if (integer == 500)
                        photoActivity.get().savingResult(0);
                } catch (NullPointerException e){
                    e.printStackTrace();
                }

        }
    }
}





