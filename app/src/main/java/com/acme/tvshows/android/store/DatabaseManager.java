package com.acme.tvshows.android.store;

import java.util.List;
import android.content.Context;
import android.util.Log;

public class DatabaseManager {
    private List<FavoriteShow> shows;
    private static final DatabaseManager instance = new DatabaseManager();
    
    private DatabaseManager() {
    }
    
    public static DatabaseManager getInstance() {
        return instance;
    }
    
    private FavoriteShowStore getFavoriteShowStore(Context context) {
        return new FavoriteShowStore(context);
    }
    
    public List<FavoriteShow> getAllShows(Context context) {
        if(shows == null) {
            try {
                shows = getFavoriteShowStore(context).getAllShows();
            } catch(StoreException e) {
                Log.e("TvShows", "Error retrieving favorite shows", e);
            }
        }
        return shows;
    }
    
    public void storeShow(Context context, FavoriteShow show) {
        try {
            if (show.isSaved()) {
                getFavoriteShowStore(context).updateShow(show);
            } else {
                getFavoriteShowStore(context).addShow(show);
            }
            shows = null;
        } catch(StoreException e) {
            Log.e("TvShows", "Error storing favorite show", e);
        }
    }
    
    public void deleteShow(Context context, FavoriteShow show) {
        try {
            getFavoriteShowStore(context).deleteShow(show);
            shows = null;
        } catch(StoreException e) {
            Log.e("TvShows", "Error deleting favorite show", e);
        }
    }
}
