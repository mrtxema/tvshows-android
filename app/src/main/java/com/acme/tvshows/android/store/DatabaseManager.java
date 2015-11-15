package com.acme.tvshows.android.store;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.acme.tvshows.android.CredentialsActivity;
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
    
    public List<FavoriteShow> getAllShows(Context context) throws StoreException {
        if (showsCache == null) {
            showsCache = getTvShowsDatabase(context).getAll(FavoriteShow.class);
        }
        return showsCache;
    }
    
    public void saveShow(Context context, FavoriteShow show) throws StoreException {
        if (show.isSaved()) {
            getTvShowsDatabase(context).update(show);
        } else {
            getTvShowsDatabase(context).add(show);
        }
        showsCache = null;
    }
    
    public void deleteShow(Context context, FavoriteShow show) throws StoreException {
        getTvShowsDatabase(context).delete(show);
        showsCache = null;
    }

    private void ensureCredentials(Context context) throws StoreException {
        if (credentialsCache == null) {
            Map<String, Credentials> credentialsMap = new LinkedHashMap<>();
            for (Credentials item : getTvShowsDatabase(context).getAll(Credentials.class)) {
                credentialsMap.put(item.getStore(), item);
            }
            credentialsCache = credentialsMap;
        }
    }

    public List<Credentials> getAllCredentials(Context context) throws StoreException {
        ensureCredentials(context);
        return new ArrayList<>(credentialsCache.values());
    }

    public Credentials getCredentials(Context context, String store) throws StoreException {
        ensureCredentials(context);
        return credentialsCache.get(store);
    }

    public void saveCredentials(Context context, Credentials credentials) throws StoreException {
        getTvShowsDatabase(context).add(credentials);
        credentialsCache = null;
    }

    public void deleteCredentials(Context context, Credentials credentials) throws StoreException {
        getTvShowsDatabase(context).delete(credentials);
        credentialsCache = null;
    }
}
