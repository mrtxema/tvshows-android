package com.acme.tvshows.android.service;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.IOException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class TvShowClient {
    private static final String SERVER = "http://tvshowsapi.herokuapp.com";
    private final HttpClient httpClient;
    
    public TvShowClient() {
        httpClient = new DefaultHttpClient();
    }
    
    private String callUrl(String url) throws ShowServiceException {
        try {
            final HttpGet httpGet = new HttpGet(SERVER + url);
            httpGet.setHeader("content-type", "application/json");
            final HttpResponse resp = httpClient.execute(httpGet);
            return EntityUtils.toString(resp.getEntity());
        } catch(IOException e) {
            throw new ShowServiceException("Can't connect to endpoint: " + url, e);
        }
    }
    
    private JSONArray callUrlAsJson(String url) throws ShowServiceException, JSONException {
        String response = callUrl(url);
        if(response.charAt(0) == '{') {
            throw new ShowServiceException(new JSONObject(response).getString("message"));
        }
        if(response.charAt(0) != '[') {
            response = String.format("[%s]", response);
        }
        return new JSONArray(response);
    }
    
    public Set getAllStores() throws ShowServiceException {
        final String url = "/tvshows";
        final Set<String> result = new HashSet<>();
        try {
            JSONArray response = callUrlAsJson(url);
            for (int i = 0; i < response.length(); i++) {
                result.add(response.getString(i));
            }
        } catch (JSONException e) {
            throw new ShowServiceException("Can't parse response from url: " + url, e);
        }
        return result;
    }
    
    public List<Show> findShows(String store, String searchString) throws ShowServiceException {
        String url = null;
        final List<Show> result = new ArrayList<>();
        try {
            url = String.format("/tvshows/%s?q=%s", store, URLEncoder.encode(searchString, "utf-8"));
            JSONArray response = callUrlAsJson(url);
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                result.add(new Show(obj.getString("id"), obj.getString("name")));
            }
        } catch (UnsupportedEncodingException e) {
            throw new ShowServiceException("Can't build url", e);
        } catch (JSONException e) {
            throw new ShowServiceException("Can't parse response from url: " + url, e);
        }
        return result;
    }
    
    public List<Season> getShowSeasons(String store, String show) throws ShowServiceException {
        final String url = String.format("/tvshows/%s/%s", store, show);
        final List<Season> result = new ArrayList<>();
        try {
            JSONArray response = callUrlAsJson(url);
            for (int i = 0; i < response.length(); i++) {
                result.add(new Season(response.getInt(i)));
            }
        } catch (JSONException e) {
            throw new ShowServiceException("Can't parse response from url: " + url, e);
        }
        return result;
    }
    
    public List<Episode> getSeasonEpisodes(String store, String show, int season) throws ShowServiceException {
        final String url = String.format("/tvshows/%s/%s/%d", store, show, season);
        final List<Episode> result = new ArrayList<>();
        try {
            JSONArray response = callUrlAsJson(url);
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                result.add(new Episode(obj.getInt("number"), obj.getString("title")));
            }
        } catch (JSONException e) {
            throw new ShowServiceException("Can't parse response from url: " + url, e);
        }
        return result;
    }
    
    public List<Link> getEpisodeLinks(String store, String show, int season, int episode) throws ShowServiceException {
        final String url = String.format("/tvshows/%s/%s/%d/%d", store, show, season, episode);
        final List<Link> result = new ArrayList<>();
        try {
            JSONArray response = callUrlAsJson(url);
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                JSONObject languageObj = obj.getJSONObject("language");
                Language lang = new Language(languageObj.getString("code"), languageObj.getString("name"));
                result.add(new Link(obj.getString("id"), obj.getString("server"), lang));
            }
        } catch (JSONException e) {
            throw new ShowServiceException("Can't parse response from url: " + url, e);
        }
        return result;
    }
    
    public String getLinkUrl(String store, String show, int season, int episode, String link) throws ShowServiceException {
        final String url = String.format("/tvshows/%s/%s/%d/%d/%s", store, show, season, episode, link);
        try {
            return callUrlAsJson(url).getString(0);
        } catch(JSONException e) {
            throw new ShowServiceException("Can\'t parse response from url: " + url, e);
        }
    }
}
