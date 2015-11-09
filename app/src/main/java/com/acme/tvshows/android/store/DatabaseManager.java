package com.acme.tvshows.android.store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.acme.tvshows.android.model.Credentials;
import com.acme.tvshows.android.model.FavoriteShow;

public class DatabaseManager {
    private List<FavoriteShow> showsCache;
    private Map<String, Credentials> credentialsCache;
    private static final DatabaseManager instance = new DatabaseManager();
    
    private DatabaseManager() {
    }
    
    public static DatabaseManager getInstance() {
        return instance;
    }
    
    private TvShowsDatabase getTvShowsDatabase(Context context) {
        return new TvShowsDatabase(context);
    }
    
    public List<FavoriteShow> getAllShows(Context context) {
        if (showsCache == null) {
            try {
                showsCache = getTvShowsDatabase(context).getAll(FavoriteShow.class);
            } catch(StoreException e) {
                Log.e("TvShows", "Error retrieving favorite shows", e);
            }
        }
        return showsCache;
    }
    
    public void saveShow(Context context, FavoriteShow show) {
        try {
            if (show.isSaved()) {
                getTvShowsDatabase(context).update(show);
            } else {
                getTvShowsDatabase(context).add(show);
            }
            showsCache = null;
        } catch(StoreException e) {
            Log.e("TvShows", "Error storing favorite show", e);
        }
    }
    
    public void deleteShow(Context context, FavoriteShow show) {
        try {
            getTvShowsDatabase(context).delete(show);
            showsCache = null;
        } catch(StoreException e) {
            Log.e("TvShows", "Error deleting favorite show", e);
        }
    }

    public Credentials getCredentials(Context context, String store) {
        if (credentialsCache == null) {
            try {
                Map<String, Credentials> credentialsMap = new HashMap<>();
                for (Credentials item : getTvShowsDatabase(context).getAll(Credentials.class)) {
                    credentialsMap.put(item.getStore(), item);
                }
                credentialsCache = credentialsMap;
            } catch(StoreException e) {
                Log.e("TvShows", "Error retrieving credentials", e);
            }
        }
        return credentialsCache.get(store);
    }

    public void saveCredentials(Context context, Credentials credentials) {
        try {
            getTvShowsDatabase(context).add(credentials);
            credentialsCache = null;
        } catch(StoreException e) {
            Log.e("TvShows", "Error storing credentials", e);
        }
    }
}
