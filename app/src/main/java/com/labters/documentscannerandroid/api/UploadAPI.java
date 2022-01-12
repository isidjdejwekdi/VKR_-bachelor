package com.labters.documentscannerandroid.api;

import com.labters.documentscannerandroid.api.model.SaveResponse;
import com.labters.documentscannerandroid.api.model.FilesListResponse;

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

    @GET("/api/v1/file/get")
    Single<FilesListResponse> getFilesList();
}
