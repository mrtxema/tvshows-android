package com.acme.tvshows.android;

import java.util.List;
import com.acme.tvshows.android.service.Season;
import com.acme.tvshows.android.service.ShowServiceException;
import com.acme.tvshows.android.service.TvShowClient;
import com.acme.tvshows.android.model.FavoriteShow;
import android.util.Log;
import com.acme.tvshows.android.adapter.ItemViewAdapter;
import com.acme.tvshows.android.adapter.ItemViewCustomizer;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import com.acme.tvshows.android.store.DatabaseManager;
import com.acme.tvshows.android.store.StoreException;

import android.widget.CheckBox;
import android.content.Intent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_show)
public class ShowActivity extends BaseActivity {
    private static final int SEASON_REQUEST = 2;
    private boolean datasetChanged = false;
    private ArrayAdapter<Season> adapter;
    @Bean TvShowClient client;
    @Bean DatabaseManager database;
    @Extra FavoriteShow show;
    @ViewById CheckBox btnAddShow;
    @ViewById ListView lstSeasons;
    @ViewById TextView title;

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
        title.setText(show.getShowName());
        btnAddShow.setChecked(show.isSaved());
        clearMessage();
        findSeasons();
    }

    @ItemClick
    void lstSeasons(Season season) {
        SeasonActivity_.intent(this).show(show).season(season.getNumber()).startForResult(SEASON_REQUEST);
    }

    @CheckedChange
    void btnAddShow(boolean checked) {
        clearMessage();
        toggleFavoriteShow(checked);
    }

    @Background
    void findSeasons() {
        try {
            List<Season> seasons = client.getShowSeasons(this, show.getStore(), show.getShowId());
            showSeasons(seasons);
        } catch(ShowServiceException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
    }

    @UiThread
    void showSeasons(List<Season> seasons) {
        adapter = new ItemViewAdapter<>(this, seasons, new ItemViewCustomizer<Season>() {
            @Override
            public Integer getImageId(Season season) {
                return show.isSeasonSeen(season.getNumber()) ? android.R.drawable.checkbox_on_background : null;
            }
        });
        lstSeasons.setAdapter(adapter);
    }

    @Background
    void toggleFavoriteShow(boolean add) {
        try {
            if (add && !show.isSaved()) {
                database.saveShow(this, show);
            } else if (!add && show.isSaved()) {
                database.deleteShow(this, show);
            }
        } catch (StoreException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEASON_REQUEST && data != null) {
            FavoriteShow resultShow = data.getExtras().getParcelable("show");
            if (resultShow != null) {
                show = resultShow;
                btnAddShow.setChecked(show.isSaved());
                datasetChanged = true;
            }
        }
    }
}
