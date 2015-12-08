package com.acme.tvshows.android;

import java.util.List;
import com.acme.tvshows.android.service.Episode;
import com.acme.tvshows.android.service.ShowServiceException;
import com.acme.tvshows.android.service.TvShowClient;
import com.acme.tvshows.android.model.FavoriteShow;

import android.graphics.drawable.Drawable;
import android.util.Log;
import com.acme.tvshows.android.adapter.ItemViewAdapter;
import com.acme.tvshows.android.adapter.ItemViewCustomizer;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DrawableRes;

@EActivity(R.layout.activity_season)
public class SeasonActivity extends BaseActivity {
    private static final int EPISODE_REQUEST = 1;
    private boolean datasetChanged = false;
    private ArrayAdapter<Episode> adapter;
    @Bean TvShowClient client;
    @Extra int season;
    @Extra FavoriteShow show;
    @ViewById ListView lstEpisodes;
    @ViewById TextView title;
    @DrawableRes(android.R.drawable.checkbox_on_background)
    Drawable checkboxDrawable;

    @Override
    protected void onResume() {
        super.onResume();
        if (datasetChanged && adapter != null) {
            adapter.notifyDataSetChanged();
            datasetChanged = false;
        }
    }

    @AfterViews
    void initViews() {
        title.setText(String.format("%s - S%d", show.getShowName(), season));
        clearMessage();
        findEpisodes();
    }

    @ItemClick
    void lstEpisodes(Episode episode) {
        EpisodeActivity_.intent(this).show(show).season(season).episodeNumber(episode.getNumber()).episodeTitle(episode.getTitle()).startForResult(EPISODE_REQUEST);
    }

    @Background
    void findEpisodes() {
        try {
            List<Episode> episodes = client.getSeasonEpisodes(this, show.getStore(), show.getShowId(), season);
            showEpisodes(episodes);
        } catch(ShowServiceException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
    }

    @UiThread
    void showEpisodes(List<Episode> episodes) {
        adapter = new ItemViewAdapter<>(this, episodes, new ItemViewCustomizer<Episode>() {
            @Override
            public Drawable getImage(Episode item) {
                return show.isEpisodeSeen(season, item.getNumber()) ? checkboxDrawable : null;
            }
        });
        lstEpisodes.setAdapter(adapter);
    }

    @OnActivityResult(EPISODE_REQUEST)
    void onEpisodeResult(Intent data) {
        if (data != null && data.hasExtra("show")) {
            show = data.getExtras().getParcelable("show");
            datasetChanged = true;
            Intent intent = new Intent();
            intent.putExtra("show", show);
            setResult(RESULT_OK, intent);
        }
    }
}
