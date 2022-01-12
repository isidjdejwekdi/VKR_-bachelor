package com.labters.documentscannerandroid.api;

import com.labters.documentscannerandroid.api.convert_model.request.NewConversionRequestBody;
import com.labters.documentscannerandroid.api.convert_model.response.StartANewConvertion;
import com.labters.documentscannerandroid.api.convert_model.response.StatusOfTheConversion;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ConvertAPI {

    @POST("/convert")
    Single<StartANewConvertion> getId(@Body NewConversionRequestBody newConversionRequestBody);

    @GET("/convert/{id}/status")
    Single<StatusOfTheConversion> getUrl(@Path("id") String id);
}
