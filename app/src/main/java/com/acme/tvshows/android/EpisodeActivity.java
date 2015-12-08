package com.acme.tvshows.android;

import java.util.List;

import com.acme.tvshows.android.service.Link;
import com.acme.tvshows.android.service.ShowServiceException;
import com.acme.tvshows.android.service.TvShowClient;
import com.acme.tvshows.android.model.FavoriteShow;

import android.util.Log;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.net.Uri;
import android.content.Intent;
import com.acme.tvshows.android.service.Episode;
import com.acme.tvshows.android.service.Season;
import com.acme.tvshows.android.store.DatabaseManager;
import com.acme.tvshows.android.store.StoreException;

import android.widget.ImageButton;
import android.widget.CheckBox;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.LayoutRes;
import org.androidannotations.annotations.res.StringRes;

@EActivity(R.layout.activity_episode)
public class EpisodeActivity extends BaseActivity {
    @Bean DatabaseManager database;
    @Bean TvShowClient client;
    @StringRes(R.string.chooser_title)
    String chooserTitle;

    @Extra int episodeNumber;
    @Extra String episodeTitle;
    @Extra int season;
    @Extra FavoriteShow show;
    @Extra boolean direct = false;
    @ViewById ListView lstLinks;
    @ViewById TextView title;
    @ViewById TextView subtitle;
    @ViewById ImageButton btnUp;
    @ViewById CheckBox btnMarkViewed;

    @AfterViews
    void initViews() {
        title.setText(show.getShowName());
        subtitle.setText(String.format("%dx%02d - %s", season, episodeNumber, episodeTitle));
        btnMarkViewed.setChecked(show.isEpisodeSeen(season, episodeNumber));
        if (!direct) {
            btnUp.setVisibility(View.GONE);
        }
        clearMessage();
        findLinks();
    }

    @ItemClick
    void lstLinks(Link link) {
        clearMessage();
        retrieveUrl(link.getId());
    }

    @Click
    void btnUp() {
        ShowActivity_.intent(this).show(show).start();
    }

    @CheckedChange
    void btnMarkViewed(boolean checked) {
        clearMessage();
        toggleEpisodeSeen(checked);
    }

    @Background
    void findLinks() {
        try {
            List<Link> links = client.getEpisodeLinks(this, show.getStore(), show.getShowId(), season, episodeNumber);
            showLinks(links);
        } catch(ShowServiceException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
    }

    @UiThread
    void showLinks(List<Link> links) {
        lstLinks.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, links));
    }

    @Background
    void retrieveUrl(String linkId) {
        try {
            String url = client.getLinkUrl(this, show.getStore(), show.getShowId(), season, episodeNumber, linkId);
            openWebPage(url);
        } catch(ShowServiceException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
    }

    @UiThread
    void openWebPage(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "video/mkv");
        Intent chooserIntent = Intent.createChooser(intent, chooserTitle);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooserIntent);
        }
    }

    @Background
    void toggleEpisodeSeen(boolean seen) {
        try {
            if (seen && !show.isEpisodeSeen(season, episodeNumber)) {
                setNextEpisode(show, season, episodeNumber);
                database.saveShow(this, show);
            } else if (!seen && show.isSaved() && show.isEpisodeSeen(season, episodeNumber)) {
                show.setNextEpisode(season, episodeNumber, episodeTitle);
                database.saveShow(this, show);
            }
            updateResult();
        } catch(ShowServiceException | StoreException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
    }

    private boolean setNextEpisode(FavoriteShow show, int seasonNumber, int episodeNumber) throws ShowServiceException {
        for (Episode episode : client.getSeasonEpisodes(this, show.getStore(), show.getShowId(), seasonNumber)) {
            if (episode.getNumber() == episodeNumber + 1) {
                show.setNextEpisode(seasonNumber, episode.getNumber(), episode.getTitle());
                return true;
            }
        }
        for (Season season : client.getShowSeasons(this, show.getStore(), show.getShowId())) {
            if(season.getNumber() == seasonNumber + 1) {
                List<Episode> episodes = client.getSeasonEpisodes(this, show.getStore(), show.getShowId(), season.getNumber());
                if (!episodes.isEmpty()) {
                    Episode episode = episodes.get(0);
                    show.setNextEpisode(season.getNumber(), episode.getNumber(), episode.getTitle());
                    return true;
                }
            }
        }
        show.setAllEpisodesSeen(true);
        return false;
    }

    private void updateResult() {
        Intent intent = new Intent();
        intent.putExtra("show", show);
        setResult(RESULT_OK, intent);
    }
}
