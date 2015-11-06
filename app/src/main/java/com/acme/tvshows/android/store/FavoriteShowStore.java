package com.acme.tvshows.android.store;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;

import com.acme.tvshows.android.model.FavoriteShow;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class FavoriteShowStore implements DatabaseStore<FavoriteShow> {
    private static final String TABLE_NAME = "shows";
    private static final String FIELD_ID = "id";
    private static final String FIELD_NEXT_EPISODE_NUMBER = "next_episode_number";
    private static final String FIELD_NEXT_EPISODE_SEASON = "next_episode_season";
    private static final String FIELD_NEXT_EPISODE_TITLE = "next_episode_title";
    private static final String FIELD_SHOW_ID = "show_id";
    private static final String FIELD_SHOW_NAME = "show_name";
    private static final String FIELD_STORE = "store";
    private static final String FIELD_LAST_UPDATE = "last_update";

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_SHOWS_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                FIELD_ID + " INTEGER PRIMARY KEY, " +
                FIELD_STORE + " TEXT, " +
                FIELD_SHOW_ID + " TEXT, " +
                FIELD_SHOW_NAME + " TEXT, " +
                FIELD_NEXT_EPISODE_SEASON + " INTEGER, " +
                FIELD_NEXT_EPISODE_NUMBER + " INTEGER, " +
                FIELD_NEXT_EPISODE_TITLE + " TEXT, " +
                FIELD_LAST_UPDATE + " INTEGER)";
        db.execSQL(CREATE_SHOWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + FIELD_LAST_UPDATE + " INTEGER");
                break;
            case 2:
                break;
            default:
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
                onCreate(db);
        }
    }
    
    private ContentValues buildContentValues(FavoriteShow show) {
        ContentValues values = new ContentValues();
        values.put(FIELD_STORE, show.getStore());
        values.put(FIELD_SHOW_ID, show.getShowId());
        values.put(FIELD_SHOW_NAME, show.getShowName());
        values.put(FIELD_NEXT_EPISODE_SEASON, show.getNextEpisodeSeason());
        values.put(FIELD_NEXT_EPISODE_NUMBER, show.getNextEpisodeNumber());
        values.put(FIELD_NEXT_EPISODE_TITLE, show.getNextEpisodeTitle());
        values.put(FIELD_LAST_UPDATE, show.getLastUpdate() == null ? null : show.getLastUpdate().getTime());
        return values;
    }
    
    private String[] idValues(FavoriteShow show) {
        return new String[] {String.valueOf(show.getId())};
    }

    private String[] getAllFields() {
        return new String[] {
                FIELD_ID, FIELD_STORE, FIELD_SHOW_ID, FIELD_SHOW_NAME,
                FIELD_NEXT_EPISODE_SEASON, FIELD_NEXT_EPISODE_NUMBER, FIELD_NEXT_EPISODE_TITLE,
                FIELD_LAST_UPDATE
        };
    }

    private Integer getCursorInteger(Cursor cursor, String field) {
        final int columnIndex = cursor.getColumnIndex(field);
        return cursor.isNull(columnIndex) ? null : cursor.getInt(columnIndex);
    }

    private Date getCursorDate(Cursor cursor, String field) {
        final int columnIndex = cursor.getColumnIndex(field);
        return cursor.isNull(columnIndex) ? null : new Date(cursor.getLong(columnIndex));
    }

    private FavoriteShow buildItem(Cursor cursor) {
        return new FavoriteShow(
                getCursorInteger(cursor, FIELD_ID),
                cursor.getString(cursor.getColumnIndex(FIELD_STORE)),
                cursor.getString(cursor.getColumnIndex(FIELD_SHOW_ID)),
                cursor.getString(cursor.getColumnIndex(FIELD_SHOW_NAME)),
                getCursorInteger(cursor, FIELD_NEXT_EPISODE_SEASON),
                getCursorInteger(cursor, FIELD_NEXT_EPISODE_NUMBER),
                cursor.getString(cursor.getColumnIndex(FIELD_NEXT_EPISODE_TITLE)),
                getCursorDate(cursor, FIELD_LAST_UPDATE)
        );
    }

    @Override
    public void add(SQLiteDatabase db, FavoriteShow show) {
        final long id = db.insertOrThrow(TABLE_NAME, null, buildContentValues(show));
        show.setId((int) id);
    }

    @Override
    public FavoriteShow find(SQLiteDatabase db, FavoriteShow item) {
        Cursor cursor = db.query(TABLE_NAME, getAllFields(), FIELD_ID + "=?", idValues(item), null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return buildItem(cursor);
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    public List<FavoriteShow> getAll(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_NAME, getAllFields(), null, null, null, null, FIELD_LAST_UPDATE + " DESC, " + FIELD_SHOW_NAME);
        try {
            final List<FavoriteShow> showList = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    showList.add(buildItem(cursor));
                } while (cursor.moveToNext());
            }
            return showList;
        } finally {
            cursor.close();
        }
    }

    @Override
    public void update(SQLiteDatabase db, FavoriteShow show) {
        db.update(TABLE_NAME, buildContentValues(show), FIELD_ID + "=?", idValues(show));
    }

    @Override
    public void delete(SQLiteDatabase db, FavoriteShow show) {
        db.delete(TABLE_NAME, FIELD_ID + "=?", idValues(show));
        show.setId(null);
    }
}
