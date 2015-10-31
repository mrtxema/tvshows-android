package com.acme.tvshows.android;

import android.os.AsyncTask;
import java.util.List;
import com.acme.tvshows.android.service.Season;
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
import com.acme.tvshows.android.store.DatabaseManager;
import android.app.Activity;
import android.widget.CheckBox;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.AdapterView;

public class ShowActivity extends Activity {
    private static final int SEASON = 0x2;
    private ArrayAdapter<Season> adapter;
    private CheckBox btnAddShow;
    private TvShowClient client;
    private boolean datasetChanged;
    private ListView lstSeasons;
    private FavoriteShow show;
    private TextView txtMessages;
    
    public ShowActivity() {
        datasetChanged = false;
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        client = TvShowClient.getInstance();
        show = getIntent().getExtras().getParcelable("show");
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(show.getShowName());
        txtMessages = (TextView) findViewById(R.id.txtMessages);
        lstSeasons = (ListView) findViewById(R.id.lstSeasons);
        btnAddShow = (CheckBox) findViewById(R.id.btnAddShow);
        btnAddShow.setChecked(show.isSaved());
        btnAddShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                new ToggleFavoriteShowTask().execute(checked);
            }
        });
        lstSeasons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Season season = Season.class.cast(adapterView.getItemAtPosition(i));
                Intent intent = new Intent(ShowActivity.this, SeasonActivity.class);
                intent.putExtra("show", show);
                intent.putExtra("season", season.getNumber());
                startActivityForResult(intent, SEASON);
            }
        });
        new FindSeasonsTask().execute();
    }
    
    protected void onResume() {
        super.onResume();
        if (datasetChanged && adapter != null) {
            adapter.notifyDataSetChanged();
            datasetChanged = false;
        }
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEASON && data != null) {
            FavoriteShow resultShow = data.getExtras().getParcelable("show");
            if (resultShow != null) {
                show = resultShow;
                btnAddShow.setChecked(show.isSaved());
                datasetChanged = true;
            }
        }
    }
    
    class FindSeasonsTask extends AsyncTask<String,Integer,Boolean> {
        private String errorMessage;
        private List<Season> seasons;
        
        protected void onPreExecute() {
            txtMessages.setText("");
        }
        
        protected Boolean doInBackground(String[] params) {
            try {
                seasons = client.getShowSeasons(show.getStore(), show.getShowId());
                return true;
            } catch(ShowServiceException e) {
                Log.e("TvShowClient", e.getMessage(), e);
                errorMessage = e.getMessage();
            }
            return false;
        }
        
        protected void onPostExecute(Boolean result) {
            if(result) {
                adapter = new ItemViewAdapter<>(ShowActivity.this, seasons, new ItemViewCustomizer<Season>() {
                    @Override
                    public Integer getImageId(Season season) {
                        return show.isSeasonSeen(season.getNumber()) ? android.R.drawable.checkbox_on_background : null;
                    }
                });
                lstSeasons.setAdapter(adapter);
            } else {
                txtMessages.setText(errorMessage);
            }
            findViewById(R.id.showLoadingPanel).setVisibility(View.GONE);
        }
    }
    
    class ToggleFavoriteShowTask extends AsyncTask<Boolean,Integer,Boolean> {

        protected void onPreExecute() {
            txtMessages.setText("");
        }
        
        protected Boolean doInBackground(Boolean... params) {
            boolean add = params[0];
            if (add && !show.isSaved()) {
                DatabaseManager.getInstance().storeShow(ShowActivity.this, show);
            }
            if (!add && show.isSaved()) {
                DatabaseManager.getInstance().deleteShow(ShowActivity.this, show);
            }
            return true;
        }
    }
}
