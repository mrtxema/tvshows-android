package com.acme.tvshows.android;

import android.os.AsyncTask;
import java.util.List;

import com.acme.tvshows.android.model.Credentials;
import com.acme.tvshows.android.service.Show;
import com.acme.tvshows.android.service.ShowServiceException;
import com.acme.tvshows.android.service.Store;
import com.acme.tvshows.android.service.TvShowClient;
import com.acme.tvshows.android.model.FavoriteShow;
import com.acme.tvshows.android.store.DatabaseManager;

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
    private static final String DEFAULT_STORE = "seriesyonkis";
    private static final int CREDENTIALS = 1;
    private TvShowClient client;
    private ListView lstShows;
    private TextView txtMessages;
    private EditText txtShow;
    private Spinner selectProvider;
    private Store selectedStore;

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

        selectProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Store store = Store.class.cast(parent.getItemAtPosition(position));
                if (!store.getLoginParameters().isEmpty()) {
                    new RetrieveCredentialsTask(store).execute();
                } else {
                    selectedStore = store;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ImageButton btnSearchShow = (ImageButton) findViewById(R.id.btnSearchShow);
        btnSearchShow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ((selectProvider.getCount() > 0) && (selectProvider.getSelectedItem() != null)) {
                    new FindShowsTask().execute(txtShow.getText().toString(), selectedStore.getCode());
                }
            }
        });

        lstShows.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Show basicShow = Show.class.cast(adapterView.getItemAtPosition(i));
                FavoriteShow show = new FavoriteShow(selectedStore.getCode(), basicShow.getId(), basicShow.getName());
                Intent intent = new Intent(SearchActivity.this, ShowActivity.class);
                intent.putExtra("show", show);
                startActivity(intent);
            }
        });

        new RetrieveStoresTask().execute();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREDENTIALS) {
            if (resultCode == RESULT_OK) {
                selectedStore = Store.class.cast(selectProvider.getSelectedItem());
            } else {
                setSelectedStore(selectedStore.getCode());
            }
        }
    }

    private void setSelectedStore(String storeCode) {
        for (int i=0; i<selectProvider.getCount(); i++) {
            if (Store.class.cast(selectProvider.getItemAtPosition(i)).getCode().equals(storeCode)) {
                selectProvider.setSelection(i);
                return;
            }
        }
        selectProvider.setSelection(0);
    }

    private class RetrieveStoresTask extends AsyncTask<String,Integer,Boolean> {
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

        protected void onPostExecute(Boolean result) {
            if (result) {
                ArrayAdapter<Store> adapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_spinner_item, stores);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                selectProvider.setAdapter(adapter);
                setSelectedStore(DEFAULT_STORE);
            } else {
                txtMessages.setText(errorMessage);
            }
        }
    }

    private class FindShowsTask extends AsyncTask<String,Integer,Boolean> {
        private String errorMessage;
        private List<Show> shows;
        
        protected void onPreExecute() {
            findViewById(R.id.searchLoadingPanel).setVisibility(View.VISIBLE);
            txtMessages.setText("");
        }
        
        protected Boolean doInBackground(String... params) {
            try {
                String searchString = params[0];
                String store = params[1];
                shows = client.findShows(SearchActivity.this, store, searchString);
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

    private class RetrieveCredentialsTask extends AsyncTask<String,Integer,Boolean> {
        private final Store store;
        private Credentials credentials;

        private RetrieveCredentialsTask(Store store) {
            this.store = store;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            credentials = DatabaseManager.getInstance().getCredentials(SearchActivity.this, store.getCode());
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (result && !store.getLoginParameters().isEmpty() && (credentials == null || !credentials.containsParameters(store.getLoginParameters()))) {
                Intent intent = new Intent(SearchActivity.this, CredentialsActivity.class);
                intent.putExtra("store", store);
                startActivityForResult(intent, CREDENTIALS);
            } else {
                selectedStore = store;
            }
        }
    }
}
