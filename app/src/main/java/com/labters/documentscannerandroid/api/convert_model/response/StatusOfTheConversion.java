package com.labters.documentscannerandroid.api.convert_model.response;

import com.labters.documentscannerandroid.api.convert_model.response.data.Data;

public class StatusOfTheConversion {
    int code;
    String status;
    Data data;

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public Data getData() {
        return data;
    }
}
