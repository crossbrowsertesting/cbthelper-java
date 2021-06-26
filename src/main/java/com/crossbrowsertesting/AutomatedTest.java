package com.crossbrowsertesting;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import com.mashape.unirest.request.GetRequest;
import org.apache.commons.codec.binary.Base64;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import com.google.gson.JsonObject;


/**
 *
 * @author CBT
 */
public class AutomatedTest {
    
    String testId;
    
    public AutomatedTest(String testId) {
        this.testId = testId;
    }
    
    public JsonNode setScore(String score) throws UnirestException { //won't return payload, but do return status for error checking?
        HttpResponse<JsonNode> response = Unirest.put(Builders.api + this.testId)
                .basicAuth(Builders.username, Builders.authkey)
                .field("action","set_score")
                .field("score", score)
                .asJson();
        return response.getBody();
    }
    
    public JsonNode setDescription(String description) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.put(Builders.api + this.testId)
                .basicAuth(Builders.username, Builders.authkey)
                .field("action","set_description")
                .field("description", description)
                .asJson();
        return response.getBody();
    }
    
    public JsonNode stop(boolean score) throws UnirestException {//overloaded method
        HttpResponse<JsonNode> response = Unirest.put(Builders.api + this.testId)
                .basicAuth(Builders.username, Builders.authkey)
                .field("action","set_score")
                .field("score", score)
                .asJson();
        HttpResponse<JsonNode> deletion_response = Unirest.delete(Builders.api + this.testId)
                .basicAuth(Builders.username, Builders.authkey)
                .asJson();
        return response.getBody();
    }
    
    public void stop() throws UnirestException {//overloaded method also add error checking
        HttpResponse<JsonNode> deletion_response = Unirest.delete(Builders.api + this.testId)
                .basicAuth(Builders.username, Builders.authkey)
                .asJson();
    }
    
    public Snapshot takeSnapshot() throws UnirestException {//overloaded method
        HttpResponse<JsonNode> response = Unirest.post(Builders.api + this.testId + "/snapshots")
                .basicAuth(Builders.username, Builders.authkey).
                asJson();
        String hash = (String) response.getBody().getObject().get("hash");
        Snapshot snap = new Snapshot(hash, this.testId);
        return snap;
    }
    
    public Snapshot takeSnapshot(String description) throws UnirestException {//overloaded method
        HttpResponse<JsonNode> response = Unirest.post(Builders.api + this.testId + "/snapshots")
                .basicAuth(Builders.username, Builders.authkey).
                asJson();
        String hash = (String) response.getBody().getObject().get("hash");
        Snapshot snap = new Snapshot(hash, this.testId);
        snap.setDescription(description);
        return snap;
    }
    
    public Snapshot[] getSnapshots() throws UnirestException {
        Snapshot[] snaps;
        HttpResponse<String> response = doGet("/snapshots").asString();
        JSONArray results = new JSONArray(response.getBody());
        String hash;
        snaps = new Snapshot[results.length()];
        for(int i=0; i<results.length();i++) {
            hash = results.getJSONObject(i).getString("hash");
            snaps[i] = new Snapshot(hash, this.testId);
        }
        return snaps;
    }

    public Video startRecordingVideo() throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.post(Builders.api + this.testId + "/videos")
                .basicAuth(Builders.username, Builders.authkey).
                asJson();
        String hash = (String) response.getBody().getObject().get("hash");
        Video video = new Video(hash, this.testId);
        return video;
    }
    
    public Video[] getVideos() throws UnirestException {
        Video[] videos;
        HttpResponse<String> response = doGet("/videos").asString();
        JSONArray results = new JSONArray(response.getBody());
        String hash;
        videos = new Video[results.length()];
        for(int i=0; i<results.length();i++) {
            hash = results.getJSONObject(i).getString("hash");
            videos[i] = new Video(hash, this.testId);
        }
        return videos;
    }

    public void saveAllSnapshots(String directory, boolean useDescription) throws UnirestException, Exception {
        //FileUtils.copyURLToFile(URL, File);
        String prefix = "image";
        String filename;
        int count = 0;
        
        //verify directory here
        
        Snapshot[] snaps = this.getSnapshots();
        for (Snapshot snap : snaps) {
            if(useDescription && !(snap.info.getObject().getString("description").equals(""))) {
                filename = snap.info.getObject().getString("description") + ".png";
            } else {
                filename = prefix + Integer.toString(count) + ".png";
            }
            snap.saveLocally(directory+filename);
            count++;
        }
    }
    
    public void saveAllVideos(String directory, boolean useDescription) throws UnirestException, Exception {
        String prefix = "video";
        String filename;
        int count = 0;
        
        //verify directory heres
        
        Video[] videos = this.getVideos();
        for (Video vid : videos) {
            if(useDescription && !(vid.info.getObject().getString("description").equals(""))) {
                filename = vid.info.getObject().getString("description") + ".mp4";
            } else {
                filename = prefix + Integer.toString(count) + ".mp4";
            }
            vid.saveLocally(directory+filename);
            count++;
        }
    }

    public String getWebUrl() throws UnirestException {
        return doGet("").asJson()
                        .getBody()
                        .getObject()
                        .getString("show_result_web_url");
    }

    private GetRequest doGet(String relativeUrl) {
        return Unirest.get(Builders.api + this.testId + relativeUrl)
                      .basicAuth(Builders.username, Builders.authkey);
    }
}
