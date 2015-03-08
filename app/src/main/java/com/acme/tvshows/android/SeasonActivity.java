package com.acme.tvshows.android;

import android.os.AsyncTask;
import java.util.List;
import com.acme.tvshows.android.service.Episode;
import com.acme.tvshows.android.service.ShowServiceException;
import com.acme.tvshows.android.service.TvShowClient;
import com.acme.tvshows.android.store.FavoriteShow;
import android.util.Log;
import com.acme.tvshows.android.adapter.ItemViewAdapter;
import com.acme.tvshows.android.adapter.ItemViewCustomizer;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;

public class SeasonActivity extends Activity {
    private static final int EPISODE = 1;
    private ArrayAdapter<Episode> adapter;
    private TvShowClient client;
    private boolean datasetChanged;
    private ListView lstEpisodes;
    private int season;
    private FavoriteShow show;
    private TextView txtMessages;
    
    public SeasonActivity() {
        datasetChanged = false;
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_season);
        client = new TvShowClient();
        show = getIntent().getExtras().getParcelable("show");
        season = getIntent().getExtras().getInt("season");
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(String.format("%s - T%d", show.getShowName(), season));
        txtMessages = (TextView) findViewById(R.id.txtMessages);
        lstEpisodes = (ListView) findViewById(R.id.lstEpisodes);
        lstEpisodes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Episode episode = Episode.class.cast(adapterView.getItemAtPosition(i));
                Intent intent = new Intent(SeasonActivity.this, EpisodeActivity.class);
                intent.putExtra("show", show);
                intent.putExtra("season", season);
                intent.putExtra("episodeNumber", episode.getNumber());
                intent.putExtra("episodeTitle", episode.getTitle());
                startActivityForResult(intent, EPISODE);
            }
        });
        new FindEpisodesTask().execute();
    }
    
    protected void onResume() {
        super.onResume();
        if (datasetChanged && adapter != null) {
            adapter.notifyDataSetChanged();
            datasetChanged = false;
        }
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EPISODE && data != null) {
            FavoriteShow resultShow = data.getExtras().getParcelable("show");
            if (resultShow != null) {
                show = resultShow;
                datasetChanged = true;
                Intent intent = new Intent();
                intent.putExtra("show", show);
                setResult(-1, intent);
            }
        }
    }
    
    class FindEpisodesTask extends AsyncTask<String,Integer,Boolean> {
        private List<Episode> episodes;
        private String errorMessage;
        
        protected void onPreExecute() {
            txtMessages.setText("");
        }
        
        protected Boolean doInBackground(String[] params) {
            try {
                episodes = client.getSeasonEpisodes(show.getStore(), show.getShowId(), season);
                return true;
            } catch(ShowServiceException e) {
                Log.e("TvShowClient", e.getMessage(), e);
                errorMessage = e.getMessage();
            }
            return false;
        }
        
        protected void onPostExecute(Boolean result) {
            if(result) {
                adapter = new ItemViewAdapter<>(SeasonActivity.this, episodes, new ItemViewCustomizer<Episode>() {
                    @Override
                    public Integer getImageId(Episode item) {
                        return show.isEpisodeSeen(season, item.getNumber()) ? android.R.drawable.checkbox_on_background : null;
                    }
                });
                lstEpisodes.setAdapter(adapter);
            } else {
                txtMessages.setText(errorMessage);
            }
            findViewById(R.id.seasonLoadingPanel).setVisibility(View.GONE);
        }
    }
}
