package com.acme.tvshows.android.store;

import android.os.Parcelable;
import android.os.Parcel;

import java.util.Date;

public class FavoriteShow implements Parcelable {
    private Integer id;
    private Integer nextEpisodeNumber;
    private Integer nextEpisodeSeason;
    private String nextEpisodeTitle;
    private Date lastUpdate;
    private final String showId;
    private final String showName;
    private final String store;

    public FavoriteShow(String store, String showId, String showName) {
        this.store = store;
        this.showId = showId;
        this.showName = showName;
    }
    
    public FavoriteShow(int id, String store, String showId, String showName, Integer nextEpisodeSeason, Integer nextEpisodeNumber, String nextEpisodeTitle, Date lastUpdate) {
        this.id = id;
        this.store = store;
        this.showId = showId;
        this.showName = showName;
        this.nextEpisodeSeason = nextEpisodeSeason;
        this.nextEpisodeNumber = nextEpisodeNumber;
        this.nextEpisodeTitle = nextEpisodeTitle;
        this.lastUpdate = lastUpdate;
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

    public Date getLastUpdate() {
        return lastUpdate;
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
        lastUpdate = new Date();
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
        writeOptionalDate(out, lastUpdate);
    }
    
    private void writeOptionalInteger(Parcel out, Integer i) {
        out.writeInt((i != null) ? i : -1);
    }
    
    private Integer readOptionalInteger(Parcel in) {
        int result = in.readInt();
        return (result != -1) ? result : null;
    }

    private void writeOptionalDate(Parcel out, Date d) {
        out.writeLong((d != null) ? d.getTime() : -1);
    }

    private Date readOptionalDate(Parcel in) {
        long result = in.readLong();
        return (result != -1) ? new Date(result) : null;
    }

    private FavoriteShow(Parcel in) {
        id = readOptionalInteger(in);
        store = in.readString();
        showId = in.readString();
        showName = in.readString();
        nextEpisodeSeason = readOptionalInteger(in);
        nextEpisodeNumber = readOptionalInteger(in);
        nextEpisodeTitle = in.readString();
        lastUpdate = readOptionalDate(in);
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
