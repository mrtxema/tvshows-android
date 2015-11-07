package com.acme.tvshows.android;

import android.os.AsyncTask;
import java.util.List;
import com.acme.tvshows.android.service.Show;
import com.acme.tvshows.android.service.ShowServiceException;
import com.acme.tvshows.android.service.Store;
import com.acme.tvshows.android.service.TvShowClient;
import com.acme.tvshows.android.model.FavoriteShow;
import android.util.Log;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.View;
import android.app.Activity;
import android.widget.EditText;
import android.os.Bundle;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.AdapterView;

public class SearchActivity extends Activity {
    private static final String STORE = "seriesyonkis";
    private TvShowClient client;
    private ListView lstShows;
    private TextView txtMessages;
    private EditText txtShow;
    private Spinner selectProvider;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        txtShow = (EditText) findViewById(R.id.txtShow);
        selectProvider = (Spinner) findViewById(R.id.selectProvider);
        txtMessages = (TextView) findViewById(R.id.txtMessages);
        lstShows = (ListView) findViewById(R.id.lstShows);
        client = TvShowClient.getInstance();

        txtShow.setText(getIntent().getExtras().getString("searchString"));
        txtShow.setSelection(txtShow.getText().length());

        new RetrieveStoresTask().execute();

        ImageButton btnSearchShow = (ImageButton) findViewById(R.id.btnSearchShow);
        btnSearchShow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new FindShowsTask().execute(txtShow.getText().toString());
            }
        });

        lstShows.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Show basicShow = Show.class.cast(adapterView.getItemAtPosition(i));
                FavoriteShow show = new FavoriteShow(STORE, basicShow.getId(), basicShow.getName());
                Intent intent = new Intent(SearchActivity.this, ShowActivity.class);
                intent.putExtra("show", show);
                startActivity(intent);
            }
        });

        new FindShowsTask().execute(txtShow.getText().toString());
    }

    class RetrieveStoresTask extends AsyncTask<String,Integer,Boolean> {
        private String errorMessage;
        private List<Store> stores;

        protected Boolean doInBackground(String... params) {
            try {
                stores = client.getAllStores();
                if (stores.isEmpty()) {
                    errorMessage = getResources().getString(R.string.noresults);
                }
                return !stores.isEmpty();
            } catch(ShowServiceException e) {
                Log.e("TvShowClient", e.getMessage(), e);
                errorMessage = e.getMessage();
            }
            return false;
        }

        private int findStorePosition(List<Store> stores, String storeCode) {
            int index = 0;
            for (Store store : stores) {
                if (store.getCode().equals(storeCode)) {
                    return index;
                }
                index++;
            }
            return 0;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                ArrayAdapter<Store> adapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_spinner_item, stores);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                selectProvider.setAdapter(adapter);
                selectProvider.setSelection(findStorePosition(stores, STORE));
            } else {
                txtMessages.setText(errorMessage);
            }
        }
    }

    class FindShowsTask extends AsyncTask<String,Integer,Boolean> {
        private String errorMessage;
        private List<Show> shows;
        
        protected void onPreExecute() {
            txtMessages.setText("");
        }
        
        protected Boolean doInBackground(String... params) {
            try {
                String searchString = params[0];
                shows = client.findShows(SearchActivity.this, STORE, searchString);
                if (shows.isEmpty()) {
                    errorMessage = getResources().getString(R.string.noresults);
                }
                return !shows.isEmpty();
            } catch(ShowServiceException e) {
                Log.e("TvShowClient", e.getMessage(), e);
                errorMessage = e.getMessage();
            }
            return false;
        }
        
        protected void onPostExecute(Boolean result) {
            if(result) {
                lstShows.setAdapter(new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_list_item_1, shows));
            } else {
                txtMessages.setText(errorMessage);
            }
            findViewById(R.id.searchLoadingPanel).setVisibility(View.GONE);
        }
    }
}
