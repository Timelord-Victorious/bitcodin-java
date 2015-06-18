package com.bitmovin.bitcodin.api.job;

import java.util.Vector;

import com.google.gson.annotations.Expose;

public class JobConfig {
    @Expose
    public int inputId;
    @Expose
    public int encodingProfileId;
    @Expose
    public Vector<String> manifestTypes = new Vector<String>();
}
