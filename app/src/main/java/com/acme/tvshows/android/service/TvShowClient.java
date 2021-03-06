package com.acme.tvshows.android.service;

import android.content.Context;

import com.acme.tvshows.android.model.Credentials;
import com.acme.tvshows.android.store.DatabaseManager;
import com.acme.tvshows.android.store.StoreException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;

@EBean(scope = EBean.Scope.Singleton)
public class TvShowClient {
    private static final String SERVER = "http://tvshowsapi.herokuapp.com";
    private static final String BASE_PATH = "/tvshows/v2";
    private static final int SESSION_EXPIRED_ERROR = 6;
    private static final long TOKEN_EXPIRATION_TIME = 1740000;

    @Bean RestApiClient restApiClient;
    @Bean DatabaseManager database;
    private final ExpirableCache<String,String> tokens = new ExpirableCache<>(TOKEN_EXPIRATION_TIME);
    private Map<String, Store> stores;

    private JSONArray callApiUrl(String store, String urlPath) throws JSONException, ShowServiceException {
        try {
            return restApiClient.callUrlAsJson(SERVER + BASE_PATH + urlPath);
        } catch (RestApiException e) {
            JSONObject error = e.getError();
            if ((store != null) && (error.getInt("code") == SESSION_EXPIRED_ERROR)) {
                tokens.remove(store);
            }
            throw new ShowServiceException(error.getString("message"));
        } catch (IOException e) {
            throw new ShowServiceException("Can't connect to endpoint: " + urlPath, e);
        }
    }

    public List<Store> getAllStores() throws ShowServiceException {
        return new ArrayList<>(getStoreMap().values());
    }

    private synchronized Map<String,Store> getStoreMap() throws ShowServiceException {
        if (stores == null) {
            final String url = "";
            final Map<String, Store> result = new LinkedHashMap<>();
            try {
                JSONArray response = callApiUrl(null, url);
                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    String code = obj.getString("code");
                    result.put(code, new Store(code, restApiClient.asStringList(obj.getJSONArray("loginParameters"))));
                }
            } catch (JSONException e) {
                throw new ShowServiceException("Can't parse response from url: " + url, e);
            }
            stores = result;
        }
        return stores;
    }

    private String getToken(Context context, String store) throws ShowServiceException {
        String token = tokens.get(store);
        if (token == null) {
            token = login(store, getLoginCredentials(context, store));
            tokens.put(store, token);
        }
        return token;
    }

    private Map<String, String> getLoginCredentials(Context context, String storeCode) throws ShowServiceException {
        Store store = getStoreMap().get(storeCode);
        if (store == null) {
            throw new ShowServiceException("Unknown store: " + storeCode);
        }
        List<String> loginParameters = store.getLoginParameters();
        if (loginParameters.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Credentials credentials = null;
            try {
                credentials = database.getCredentials(context, storeCode);
                if (credentials == null || !credentials.containsParameters(loginParameters)) {
                    throw new ShowServiceException("Missing credentials for store " + storeCode);
                }
            } catch (StoreException e) {
                throw new ShowServiceException("Can't retrieve credentials for store " + storeCode, e);
            }
            return credentials.getParameters();
        }
    }

    private String login(String store, Map<String,String> parameters) throws ShowServiceException {
        final String url = String.format("/%s/login%s", store, restApiClient.buildQueryString(parameters));
        try {
            return callApiUrl(store, url).getString(0);
        } catch(JSONException e) {
            throw new ShowServiceException("Can\'t parse response from url: " + url, e);
        }
    }

    public List<Show> findShows(Context context, String store, String searchString) throws ShowServiceException {
        String url = null;
        final List<Show> result = new ArrayList<>();
        try {
            url = String.format("/%s/searchshow?token=%s&q=%s", store, getToken(context, store), URLEncoder.encode(searchString, "utf-8"));
            JSONArray response = callApiUrl(store, url);
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
    
    public List<Season> getShowSeasons(Context context, String store, String show) throws ShowServiceException {
        final String url = String.format("/%s/show/%s?token=%s", store, show, getToken(context, store));
        final List<Season> result = new ArrayList<>();
        try {
            JSONArray response = callApiUrl(store, url);
            for (int i = 0; i < response.length(); i++) {
                result.add(new Season(response.getInt(i)));
            }
        } catch (JSONException e) {
            throw new ShowServiceException("Can't parse response from url: " + url, e);
        }
        return result;
    }
    
    public List<Episode> getSeasonEpisodes(Context context, String store, String show, int season) throws ShowServiceException {
        final String url = String.format("/%s/show/%s/%d?token=%s", store, show, season, getToken(context, store));
        final List<Episode> result = new ArrayList<>();
        try {
            JSONArray response = callApiUrl(store, url);
            for (int i = 0; i < response.length(); i++) {
                JSONObject obj = response.getJSONObject(i);
                result.add(new Episode(obj.getInt("number"), obj.getString("title")));
            }
        } catch (JSONException e) {
            throw new ShowServiceException("Can't parse response from url: " + url, e);
        }
        return result;
    }
    
    public List<Link> getEpisodeLinks(Context context, String store, String show, int season, int episode) throws ShowServiceException {
        final String url = String.format("/%s/show/%s/%d/%d?token=%s", store, show, season, episode, getToken(context, store));
        final List<Link> result = new ArrayList<>();
        try {
            JSONArray response = callApiUrl(store, url);
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
    
    public String getLinkUrl(Context context, String store, String show, int season, int episode, String link) throws ShowServiceException {
        final String url = String.format("/%s/show/%s/%d/%d/%s?token=%s", store, show, season, episode, link, getToken(context, store));
        try {
            return callApiUrl(store, url).getString(0);
        } catch(JSONException e) {
            throw new ShowServiceException("Can\'t parse response from url: " + url, e);
        }
    }
}
