package com.acme.tvshows.android.store;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import java.util.List;
import java.util.ArrayList;

public class FavoriteShowStore extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "tvshows";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SHOWS = "shows";
    private static final String KEY_ID = "id";
    private static final String KEY_NEXT_EPISODE_NUMBER = "next_episode_number";
    private static final String KEY_NEXT_EPISODE_SEASON = "next_episode_season";
    private static final String KEY_NEXT_EPISODE_TITLE = "next_episode_title";
    private static final String KEY_SHOW_ID = "show_id";
    private static final String KEY_SHOW_NAME = "show_name";
    private static final String KEY_STORE = "store";

    public FavoriteShowStore(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_SHOWS_TABLE = "CREATE TABLE " + TABLE_SHOWS + " (" +
                KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_STORE + " TEXT, " +
                KEY_SHOW_ID + " TEXT, " +
                KEY_SHOW_NAME + " TEXT, " +
                KEY_NEXT_EPISODE_SEASON + " INTEGER, " +
                KEY_NEXT_EPISODE_NUMBER + " INTEGER, " +
                KEY_NEXT_EPISODE_TITLE + " TEXT)";
        db.execSQL(CREATE_SHOWS_TABLE);
    }
    
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOWS);
        onCreate(db);
    }
    
    private ContentValues buildContentValues(FavoriteShow show) {
        ContentValues values = new ContentValues();
        values.put(KEY_STORE, show.getStore());
        values.put(KEY_SHOW_ID, show.getShowId());
        values.put(KEY_SHOW_NAME, show.getShowName());
        values.put(KEY_NEXT_EPISODE_SEASON, show.getNextEpisodeSeason());
        values.put(KEY_NEXT_EPISODE_NUMBER, show.getNextEpisodeNumber());
        values.put(KEY_NEXT_EPISODE_TITLE, show.getNextEpisodeTitle());
        return values;
    }
    
    private String[] idValues(FavoriteShow show) {
        return new String[] {String.valueOf(show.getId())};
    }
    
    private Integer getCursorInteger(Cursor cursor, String field) {
        final int columnIndex = cursor.getColumnIndex(field);
        return cursor.isNull(columnIndex) ? null : cursor.getInt(columnIndex);
    }
    
    public void addShow(FavoriteShow show) throws StoreException {
        final SQLiteDatabase db = getWritableDatabase();
        try {
            final long id = db.insertOrThrow(TABLE_SHOWS, null, buildContentValues(show));
            show.setId((int) id);
        } catch(SQLException e) {
            throw new StoreException("Could not insert show", e);
        } finally {
            db.close();
        }
    }
    
    public List<FavoriteShow> getAllShows() throws StoreException {
        final SQLiteDatabase db = getWritableDatabase();
        final List<FavoriteShow> showList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_SHOWS,
                    new String[] {
                            KEY_ID, KEY_STORE, KEY_SHOW_ID, KEY_SHOW_NAME,
                            KEY_NEXT_EPISODE_SEASON, KEY_NEXT_EPISODE_NUMBER, KEY_NEXT_EPISODE_TITLE
                    },
                    null, null, null, null,
                    KEY_SHOW_NAME);
            if(cursor.moveToFirst()) {
                do {
                    FavoriteShow show = new FavoriteShow(
                            getCursorInteger(cursor, KEY_ID),
                            cursor.getString(cursor.getColumnIndex(KEY_STORE)),
                            cursor.getString(cursor.getColumnIndex(KEY_SHOW_ID)),
                            cursor.getString(cursor.getColumnIndex(KEY_SHOW_NAME)),
                            getCursorInteger(cursor, KEY_NEXT_EPISODE_SEASON),
                            getCursorInteger(cursor, KEY_NEXT_EPISODE_NUMBER),
                            cursor.getString(cursor.getColumnIndex(KEY_NEXT_EPISODE_TITLE))
                    );
                    showList.add(show);
                } while (cursor.moveToNext());
            }
        } catch(SQLException e) {
            throw new StoreException("Could not retrieve shows", e);
        } finally {
            if(cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return showList;
    }
    
    public void updateShow(FavoriteShow show) throws StoreException {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.update("shows", buildContentValues(show), "id=?", idValues(show));
        } catch(SQLException e) {
            throw new StoreException("Could not update show", e);
        } finally {
            db.close();
        }
    }
    
    public void deleteShow(FavoriteShow show) throws StoreException {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete("shows", "id=?", idValues(show));
            show.setId(null);
        } catch(SQLException e) {
            throw new StoreException("Could not delete show", e);
        } finally {
            db.close();
        }
    }
}
