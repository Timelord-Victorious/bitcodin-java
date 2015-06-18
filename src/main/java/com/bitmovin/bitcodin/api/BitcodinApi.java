/*
 * BitcodinApi.java
 *****************************************************************************
 * Copyright (C) 2015, bitmovin, All Rights Reserved
 *
 * Created on: Jun 17, 2015
 * Author: Christopher Mueller <christopher.mueller@bitmovin.net>
 *
 * This source code and its use and distribution, is subject to the terms
 * and conditions of the applicable license agreement.
 *****************************************************************************/

package com.bitmovin.bitcodin.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.bitmovin.network.http.RestClient;
import com.google.gson.Gson;

public class BitcodinApi {

    private String apiKey;

    public BitcodinApi(String apiKey) {

        this.apiKey = apiKey;
    }

    public String getKey() {
        return this.apiKey;
    }
    
    public Input createInput(String url) {
        
        RestClient rest;
        try {
            rest = new RestClient(new URI("http://portal.bitcodin.com/api/"));
        } catch (URISyntaxException e1) {
            return null;
        }

        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("bitcodin-api-version", "v1");
        headers.put("bitcodin-api-key", this.apiKey);
        
        try {
            Gson gson = new Gson();
            return gson.fromJson(rest.post(new URI("input/create"), headers, "{\"url\": \"" + url + "\"}"), Input.class);
        } catch (IOException e) {
            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

}
