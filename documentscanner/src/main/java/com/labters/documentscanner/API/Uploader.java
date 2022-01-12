package com.labters.documentscanner.API;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Uploader {
    final private String BASE_URL = "https://filexch.tk/";
    final private UploadAPI uploadAPI;

    public Uploader() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        uploadAPI = retrofit.create(UploadAPI.class);

           }

    public MultipartBody.Part getfileToUpload(File file){
        RequestBody requestBody = RequestBody.create(MediaType.parse("*/*"), file);

        return MultipartBody.Part.createFormData("file", file.getName(), requestBody);
    }

    public UploadAPI getUploadAPI() {
        return uploadAPI;
    }
}
