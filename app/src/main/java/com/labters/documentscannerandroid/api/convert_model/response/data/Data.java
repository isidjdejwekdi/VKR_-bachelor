package com.labters.documentscannerandroid.api.convert_model.response.data;

import com.labters.documentscannerandroid.api.convert_model.response.data.output.Output;

public class Data {

    String id;
    String step;
    int step_percent;
    int minutes;
    Output output;

    public String getId() {
        return id;
    }

    public String getStep() {
        return step;
    }

    public int getStep_percent() {
        return step_percent;
    }

    public int getMinutes() {
        return minutes;
    }

    public Output getOutput() {
        return output;
    }
}
