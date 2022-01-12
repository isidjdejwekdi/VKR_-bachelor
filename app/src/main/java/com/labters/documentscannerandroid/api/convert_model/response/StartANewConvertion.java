package com.labters.documentscannerandroid.api.convert_model.response;

import com.labters.documentscannerandroid.api.convert_model.response.data.SimpleData;

public class StartANewConvertion {
    int code;
    String status;
    SimpleData data;//String id, int minutes

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public SimpleData getData() {
        return data;
    }
}

