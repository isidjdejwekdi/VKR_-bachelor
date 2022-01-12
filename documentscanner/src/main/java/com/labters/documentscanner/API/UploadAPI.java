package com.labters.documentscanner.API;


import io.reactivex.Single;
import okhttp3.MultipartBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadAPI {
    @Multipart
    @POST("/api/v1/file/save")
    Single<SaveResponse> uploadImage (@Part MultipartBody.Part file);

}
