package com.acme.tvshows.android.store;

import android.os.Parcelable;
import android.os.Parcel;

public class FavoriteShow implements Parcelable {
    private Integer id;
    private Integer nextEpisodeNumber;
    private Integer nextEpisodeSeason;
    private String nextEpisodeTitle;
    private final String showId;
    private final String showName;
    private final String store;
    
    public FavoriteShow(String store, String showId, String showName) {
        this.store = store;
        this.showId = showId;
        this.showName = showName;
    }
    
    public FavoriteShow(int id, String store, String showId, String showName, Integer nextEpisodeSeason, Integer nextEpisodeNumber, String nextEpisodeTitle) {
        this.id = id;
        this.store = store;
        this.showId = showId;
        this.showName = showName;
        this.nextEpisodeSeason = nextEpisodeSeason;
        this.nextEpisodeNumber = nextEpisodeNumber;
        this.nextEpisodeTitle = nextEpisodeTitle;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public boolean isSaved() {
        return (id != null);
    }
    
    public String getStore() {
        return store;
    }
    
    public String getShowId() {
        return showId;
    }
    
    public String getShowName() {
        return showName;
    }
    
    public Integer getNextEpisodeSeason() {
        return nextEpisodeSeason;
    }
    
    public Integer getNextEpisodeNumber() {
        return nextEpisodeNumber;
    }
    
    public String getNextEpisodeTitle() {
        return nextEpisodeTitle;
    }
    
    public boolean isEpisodeSeen(int season, int episodeNumber) {
        if ((nextEpisodeSeason != null) && (nextEpisodeNumber != null)) {
            return (nextEpisodeSeason > season) || ((nextEpisodeSeason == season) && (nextEpisodeNumber > episodeNumber));
        } else {
            return false;
        }
    }
    
    public boolean isSeasonSeen(int season) {
        return nextEpisodeSeason != null && nextEpisodeSeason > season;
    }
    
    public boolean hasNextEpisode() {
        return (nextEpisodeTitle != null);
    }
    
    public void setAllEpisodesSeen(boolean seen) {
        setNextEpisode(seen ? 999 : 0, seen ? 999 : 0, null);
    }
    
    public void setNextEpisode(Integer seasonNumber, Integer episodeNumber, String episodeTitle) {
        nextEpisodeSeason = seasonNumber;
        nextEpisodeNumber = episodeNumber;
        nextEpisodeTitle = episodeTitle;
    }
    
    public String toString() {
        return hasNextEpisode() ? String.format("%s - %dx%d %s", showName, nextEpisodeSeason, nextEpisodeNumber, nextEpisodeTitle) : showName;
    }
    
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        writeOptionalInteger(out, id);
        out.writeString(store);
        out.writeString(showId);
        out.writeString(showName);
        writeOptionalInteger(out, nextEpisodeSeason);
        writeOptionalInteger(out, nextEpisodeNumber);
        out.writeString(nextEpisodeTitle);
    }
    
    private void writeOptionalInteger(Parcel out, Integer i) {
        out.writeInt((i != null) ? i : -1);
    }
    
    private Integer readOptionalInteger(Parcel in) {
        int result = in.readInt();
        return (result != -1) ? result : null;
    }
    
    private FavoriteShow(Parcel in) {
        id = readOptionalInteger(in);
        store = in.readString();
        showId = in.readString();
        showName = in.readString();
        nextEpisodeSeason = readOptionalInteger(in);
        nextEpisodeNumber = readOptionalInteger(in);
        nextEpisodeTitle = in.readString();
    }

    public static final Parcelable.Creator<FavoriteShow> CREATOR = new Parcelable.Creator<FavoriteShow>() {
        public FavoriteShow createFromParcel(Parcel in) {
            return new FavoriteShow(in);
        }
        
        public FavoriteShow[] newArray(int size) {
            return new FavoriteShow[size];
        }
    };
}
