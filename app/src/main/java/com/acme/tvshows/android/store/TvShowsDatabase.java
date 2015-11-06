package com.acme.tvshows.android.store;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.acme.tvshows.android.model.Credentials;
import com.acme.tvshows.android.model.FavoriteShow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TvShowsDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tvshows";
    private static final int DATABASE_VERSION = 3;
    private final Map<Class<?>, DatabaseStore<?>> stores = new LinkedHashMap<>();

    public TvShowsDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        stores.put(FavoriteShow.class, new FavoriteShowStore());
        stores.put(Credentials.class, new CredentialsStore());
    }

    public void onCreate(SQLiteDatabase db) {
        for (DatabaseStore store : stores.values()) {
            store.onCreate(db);
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (DatabaseStore store : stores.values()) {
            store.onUpgrade(db, oldVersion, newVersion);
        }
    }

    public <T> void add(T item) throws StoreException {
        DatabaseStore<T> store = (DatabaseStore<T>) stores.get(item.getClass());
        SQLiteDatabase db = getWritableDatabase();
        try {
            store.add(db, item);
        } catch(SQLException e) {
            throw new StoreException("Could not insert " + item.getClass().getSimpleName(), e);
        } finally {
            db.close();
        }
    }

    public <T> List<T> getAll(Class<T> clazz) throws StoreException {
        DatabaseStore<T> store = (DatabaseStore<T>) stores.get(clazz);
        SQLiteDatabase db = getReadableDatabase();
        try {
            return store.getAll(db);
        } catch(SQLException e) {
            throw new StoreException("Could not retrieve " + clazz.getSimpleName(), e);
        } finally {
            db.close();
        }
    }

    public <T> void update(T item) throws StoreException {
        DatabaseStore<T> store = (DatabaseStore<T>) stores.get(item.getClass());
        SQLiteDatabase db = getWritableDatabase();
        try {
            store.update(db, item);
        } catch(SQLException e) {
            throw new StoreException("Could not update " + item.getClass().getSimpleName(), e);
        } finally {
            db.close();
        }
    }

    public <T> void delete(T item) throws StoreException {
        DatabaseStore<T> store = (DatabaseStore<T>) stores.get(item.getClass());
        SQLiteDatabase db = getWritableDatabase();
        try {
            store.delete(db, item);
        } catch(SQLException e) {
            throw new StoreException("Could not delete " + item.getClass().getSimpleName(), e);
        } finally {
            db.close();
        }
    }
}
