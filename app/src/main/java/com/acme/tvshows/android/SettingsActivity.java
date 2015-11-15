package com.acme.tvshows.android;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.acme.tvshows.android.model.Credentials;
import com.acme.tvshows.android.service.ShowServiceException;
import com.acme.tvshows.android.service.Store;
import com.acme.tvshows.android.service.TvShowClient;
import com.acme.tvshows.android.store.DatabaseManager;
import com.acme.tvshows.android.store.StoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends BaseActivity {
    private static final int CREDENTIALS_REQUEST = 4;
    private ListView lstCredentials;
    private TextView txtMessages;
    private Map<String, Store> stores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_settings);

        txtMessages = (TextView) findViewById(R.id.txtMessages);
        lstCredentials = (ListView) findViewById(R.id.lstCredentials);
        lstCredentials.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (stores != null) {
                    Credentials credentials = Credentials.class.cast(adapterView.getItemAtPosition(i));
                    Store store = stores.get(credentials.getStore());
                    Intent intent = new Intent(SettingsActivity.this, CredentialsActivity.class);
                    intent.putExtra("store", store);
                    intent.putExtra("credentials", credentials);
                    startActivityForResult(intent, CREDENTIALS_REQUEST);
                }
            }
        });

        new RetrieveStoresTask().execute();
    }

    protected void onResume() {
        super.onResume();
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        new RetrieveCredentialsTask().execute();
    }

    /*
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREDENTIALS_REQUEST) {
            if (resultCode == RESULT_OK) {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                new RetrieveCredentialsTask().execute();
            }
        }
    }
    */

    class RetrieveCredentialsTask extends AsyncTask<String,Integer,Boolean> {
        private List<Credentials> credentialsList;
        private String errorMessage;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                credentialsList = DatabaseManager.getInstance().getAllCredentials(SettingsActivity.this);
                return true;
            } catch (StoreException e) {
                Log.e("TvShowClient", e.getMessage(), e);
                errorMessage = e.getMessage();
            }
            return false;
        }

        @Override
        protected void onPreExecute() {
            txtMessages.setText("");
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                lstCredentials.setAdapter(new ArrayAdapter<>(SettingsActivity.this, android.R.layout.simple_list_item_1, credentialsList));
                if (credentialsList.isEmpty()) {
                    txtMessages.setText(getResources().getString(R.string.noresults));
                }
            } else {
                txtMessages.setText(errorMessage);
            }
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
    }

    private class RetrieveStoresTask extends AsyncTask<String,Integer,Boolean> {
        private List<Store> storeList;
        private String errorMessage;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                storeList = TvShowClient.getInstance().getAllStores();
                return true;
            } catch(ShowServiceException e) {
                Log.e("TvShowClient", e.getMessage(), e);
                errorMessage = e.getMessage();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                stores = new HashMap<>();
                for (Store store : storeList) {
                    stores.put(store.getCode(), store);
                }
                if (storeList.isEmpty()) {
                    txtMessages.setText(getResources().getString(R.string.noresults));
                }
            } else {
                txtMessages.setText(errorMessage);
            }
        }
    }
}
