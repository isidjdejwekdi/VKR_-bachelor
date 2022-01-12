package com.labters.documentscannerandroid.api;

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
                //Указание базового URL
                .baseUrl(BASE_URL)
                //Добавление конвертера json формата
                .addConverterFactory(GsonConverterFactory.create())
                //добавление адаптера для библиотеки RxJava
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        //создать класс из интерфеса с указанными параметрами
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
