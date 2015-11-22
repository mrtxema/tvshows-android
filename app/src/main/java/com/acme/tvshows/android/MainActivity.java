package com.acme.tvshows.android;

import java.util.List;

import com.acme.tvshows.android.adapter.ShowViewAdapter;
import com.acme.tvshows.android.model.FavoriteShow;
import com.acme.tvshows.android.store.DatabaseManager;
import com.acme.tvshows.android.store.StoreException;

import android.util.Log;
import android.widget.ListView;
import android.view.View;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity {
    @Bean DatabaseManager database;
    @ViewById ListView lstShows;

    @ItemClick
    void lstShows(FavoriteShow show) {
        if (show.hasNextEpisode()) {
            EpisodeActivity_.intent(this).show(show).season(show.getNextEpisodeSeason())
                    .episodeNumber(show.getNextEpisodeNumber()).episodeTitle(show.getNextEpisodeTitle()).direct(true).start();
        } else {
            ShowActivity_.intent(this).show(show).start();
        }
    }

    protected void onResume() {
        super.onResume();
        setLoadingPanelVisibility(View.VISIBLE);
        clearMessage();
        retrieveShows();
    }

    @Background
    void retrieveShows() {
        try {
            List<FavoriteShow> shows = database.getAllShows(this);
            showShows(shows);
        } catch (StoreException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
    }

    @UiThread
    void showShows(List<FavoriteShow> shows) {
        lstShows.setAdapter(new ShowViewAdapter(this, shows));
        if (shows.isEmpty()) {
            setMessage(getResources().getString(R.string.noresults));
        }
    }
}
