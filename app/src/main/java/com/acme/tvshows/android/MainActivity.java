package com.acme.tvshows.android;

import android.content.Intent;
import android.os.AsyncTask;
import java.util.List;

import com.acme.tvshows.android.store.FavoriteShow;
import com.acme.tvshows.android.store.DatabaseManager;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.TextView;
import android.app.Activity;
import android.widget.EditText;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.AdapterView;
import android.view.Menu;

public class MainActivity extends Activity {
    private ListView lstShows;
    private TextView txtMessages;
    private EditText txtShow;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtShow = (EditText) findViewById(R.id.txtShow);
        txtMessages = (TextView) findViewById(R.id.txtMessages);
        lstShows = (ListView) findViewById(R.id.lstShows);
        ImageButton btnSearchShow = (ImageButton) findViewById(R.id.btnSearchShow);
        btnSearchShow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("searchString", txtShow.getText().toString());
                startActivity(intent);
            }
        });
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
        txtShow.setText("");
        new RetrieveShowsTask().execute();
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    class RetrieveShowsTask extends AsyncTask<String,Integer,Boolean> {
        private List<FavoriteShow> shows;

        @Override
        protected Boolean doInBackground(String... params) {
            shows = DatabaseManager.getInstance().getAllShows(MainActivity.this);
            return true;
        }

        protected void onPreExecute() {
            txtMessages.setText("");
        }
        
        protected void onPostExecute(Boolean result) {
            if(result) {
                lstShows.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, shows));
            }
            findViewById(R.id.mainLoadingPanel).setVisibility(View.GONE);
        }
    }
}
