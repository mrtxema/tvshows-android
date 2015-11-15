package com.acme.tvshows.android;

import android.os.AsyncTask;
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

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class EpisodeActivity extends BaseActivity {
    private TvShowClient client;
    private int episodeNumber;
    private String episodeTitle;
    private ListView lstLinks;
    private int season;
    private FavoriteShow show;
    private TextView txtMessages;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_episode);
        client = TvShowClient.getInstance();
        show = getIntent().getExtras().getParcelable("show");
        season = getIntent().getExtras().getInt("season");
        episodeNumber = getIntent().getExtras().getInt("episodeNumber");
        episodeTitle = getIntent().getExtras().getString("episodeTitle");
        boolean directFromMain = getIntent().getBooleanExtra("direct", false);
        ((TextView) findViewById(R.id.title)).setText(show.getShowName());
        ((TextView) findViewById(R.id.subtitle)).setText(String.format("%dx%02d - %s", season, episodeNumber, episodeTitle));
        txtMessages = (TextView) findViewById(R.id.txtMessages);
        lstLinks = (ListView) findViewById(R.id.lstLinks);
        lstLinks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Link link = Link.class.cast(adapterView.getItemAtPosition(i));
                new RetrieveUrlTask().execute(link.getId());
            }
        });
        ImageButton btnUp = (ImageButton) findViewById(R.id.btnUp);
        if (directFromMain) {
            btnUp.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(EpisodeActivity.this, ShowActivity.class);
                    intent.putExtra("show", show);
                    startActivity(intent);
                }
            });
        } else {
            btnUp.setVisibility(View.GONE);
        }
        CheckBox btnMarkViewed = (CheckBox) findViewById(R.id.btnMarkViewed);
        btnMarkViewed.setChecked(show.isEpisodeSeen(season, episodeNumber));
        btnMarkViewed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                new ToggleEpisodeSeenTask().execute(checked);
            }
        });
        new FindLinksTask().execute();
    }
    
    class FindLinksTask extends AsyncTask<String,Integer,Boolean> {
        private String errorMessage;
        private List<Link> links;
        
        protected void onPreExecute() {
            txtMessages.setText("");
        }
        
        protected Boolean doInBackground(String... params) {
            try {
                links = client.getEpisodeLinks(EpisodeActivity.this, show.getStore(), show.getShowId(), season, episodeNumber);
                return true;
            } catch(ShowServiceException e) {
                Log.e("TvShowClient", e.getMessage(), e);
                errorMessage = e.getMessage();
            }
            return false;
        }
        
        protected void onPostExecute(Boolean result) {
            if (result) {
                lstLinks.setAdapter(new ArrayAdapter<>(EpisodeActivity.this, android.R.layout.simple_list_item_1, links));
            } else {
                txtMessages.setText(errorMessage);
            }
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }
    
    class RetrieveUrlTask extends AsyncTask<String,Integer,Boolean> {
        private String errorMessage;
        private String url;
        
        protected void onPreExecute() {
            txtMessages.setText("");
        }
        
        protected Boolean doInBackground(String... params) {
            try {
                String linkId = params[0];
                url = client.getLinkUrl(EpisodeActivity.this, show.getStore(), show.getShowId(), season, episodeNumber, linkId);
                return true;
            } catch(ShowServiceException e) {
                Log.e("TvShowClient", e.getMessage(), e);
                errorMessage = e.getMessage();
            }
            return false;
        }
        
        private void openWebPage(String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "video/mkv");
            Intent chooserIntent = Intent.createChooser(intent, getResources().getText(R.string.chooser_title));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooserIntent);
            }
        }
        
        protected void onPostExecute(Boolean result) {
            if(result) {
                openWebPage(url);
            } else {
                txtMessages.setText(errorMessage);
            }
        }
    }
    
    class ToggleEpisodeSeenTask extends AsyncTask<Boolean,Integer,Boolean> {
        private String errorMessage;
        
        protected void onPreExecute() {
            txtMessages.setText("");
        }
        
        protected Boolean doInBackground(Boolean... params) {
            boolean seen = params[0];
            try {
                if (seen && !show.isEpisodeSeen(season, episodeNumber)) {
                    setNextEpisode(show, season, episodeNumber);
                    DatabaseManager.getInstance().saveShow(EpisodeActivity.this, show);
                }
                if (!seen && show.isSaved() && show.isEpisodeSeen(season, episodeNumber)) {
                    show.setNextEpisode(season, episodeNumber, episodeTitle);
                    DatabaseManager.getInstance().saveShow(EpisodeActivity.this, show);
                }
                return true;
            } catch(ShowServiceException | StoreException e) {
                Log.e("TvShowClient", e.getMessage(), e);
                errorMessage = e.getMessage();
            }
            return false;
        }
        
        protected void onPostExecute(Boolean result) {
            if (result) {
                Intent intent = new Intent();
                intent.putExtra("show", show);
                setResult(RESULT_OK, intent);
            } else {
                txtMessages.setText(errorMessage);
            }
        }
        
        private boolean setNextEpisode(FavoriteShow show, int seasonNumber, int episodeNumber) throws ShowServiceException {
            for(Episode episode : client.getSeasonEpisodes(EpisodeActivity.this, show.getStore(), show.getShowId(), seasonNumber)) {
                if (episode.getNumber() == episodeNumber + 1) {
                    show.setNextEpisode(seasonNumber, episode.getNumber(), episode.getTitle());
                    return true;
                }
            }
            for (Season season : client.getShowSeasons(EpisodeActivity.this, show.getStore(), show.getShowId())) {
                if(season.getNumber() == seasonNumber + 1) {
                    List<Episode> episodes = client.getSeasonEpisodes(EpisodeActivity.this, show.getStore(), show.getShowId(), season.getNumber());
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
    }
}
