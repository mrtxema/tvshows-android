package com.acme.tvshows.android;

import android.content.Intent;
import android.os.AsyncTask;
import java.util.List;

import com.acme.tvshows.android.adapter.ShowViewAdapter;
import com.acme.tvshows.android.model.FavoriteShow;
import com.acme.tvshows.android.store.DatabaseManager;
import com.acme.tvshows.android.store.StoreException;

import android.util.Log;
import android.widget.ListView;
import android.view.View;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.AdapterView;
import android.view.Menu;

public class MainActivity extends BaseActivity {
    private ListView lstShows;
    private TextView txtMessages;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_main);

        txtMessages = (TextView) findViewById(R.id.txtMessages);
        lstShows = (ListView) findViewById(R.id.lstShows);
        lstShows.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FavoriteShow show = FavoriteShow.class.cast(adapterView.getItemAtPosition(i));
                Intent intent;
                if(show.hasNextEpisode()) {
                    intent = new Intent(MainActivity.this, EpisodeActivity.class);
                    intent.putExtra("show", show);
                    intent.putExtra("season", show.getNextEpisodeSeason());
                    intent.putExtra("episodeNumber", show.getNextEpisodeNumber());
                    intent.putExtra("episodeTitle", show.getNextEpisodeTitle());
                    intent.putExtra("direct", true);
                } else {
                    intent = new Intent(MainActivity.this, ShowActivity.class);
                    intent.putExtra("show", show);
                }
                startActivity(intent);
            }
        });
    }
    
    protected void onResume() {
        super.onResume();
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        new RetrieveShowsTask().execute();
    }
    
    class RetrieveShowsTask extends AsyncTask<String,Integer,Boolean> {
        private List<FavoriteShow> shows;
        private String errorMessage;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                shows = DatabaseManager.getInstance().getAllShows(MainActivity.this);
                return true;
            } catch (StoreException e) {
                Log.e("TvShowClient", e.getMessage(), e);
                errorMessage = e.getMessage();
            }
            return false;
        }

        protected void onPreExecute() {
            txtMessages.setText("");
        }
        
        protected void onPostExecute(Boolean result) {
            if (result) {
                lstShows.setAdapter(new ShowViewAdapter(MainActivity.this, shows));
                if (shows.isEmpty()) {
                    txtMessages.setText(getResources().getString(R.string.noresults));
                }
            } else {
                txtMessages.setText(errorMessage);
            }
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }
}
