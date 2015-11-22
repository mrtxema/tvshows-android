package com.acme.tvshows.android;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.acme.tvshows.android.model.Credentials;
import com.acme.tvshows.android.service.ShowServiceException;
import com.acme.tvshows.android.service.Store;
import com.acme.tvshows.android.service.TvShowClient;
import com.acme.tvshows.android.store.DatabaseManager;
import com.acme.tvshows.android.store.StoreException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EActivity(R.layout.activity_settings)
public class SettingsActivity extends BaseActivity {
    private static final int CREDENTIALS_REQUEST = 4;
    private Map<String, Store> stores;
    @Bean TvShowClient client;
    @Bean DatabaseManager database;
    @ViewById ListView lstCredentials;

    @Override
    protected void onResume() {
        super.onResume();
        setLoadingPanelVisibility(View.VISIBLE);
        clearMessage();
        retrieveCredentials();
    }

    @AfterViews
    void initViews() {
        retrieveStores();
    }

    @ItemClick
    void lstCredentials(Credentials credentials) {
        if (stores != null) {
            Store store = stores.get(credentials.getStore());
            CredentialsActivity_.intent(this).store(store).credentials(credentials).startForResult(CREDENTIALS_REQUEST);
        }
    }

    @Background
    void retrieveCredentials() {
        try {
            List<Credentials> credentialsList = database.getAllCredentials(this);
            showCredentials(credentialsList);
        } catch (StoreException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
    }

    @UiThread
    void showCredentials(List<Credentials> credentialsList) {
        lstCredentials.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, credentialsList));
        if (credentialsList.isEmpty()) {
            setMessage(getResources().getString(R.string.noresults));
        }
    }

    @Background
    void retrieveStores() {
        try {
            List<Store> storeList = client.getAllStores();
            showStores(storeList);
        } catch(ShowServiceException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
    }

    @UiThread
    void showStores(List<Store> storeList) {
        stores = new HashMap<>();
        for (Store store : storeList) {
            stores.put(store.getCode(), store);
        }
        if (storeList.isEmpty()) {
            setMessage(getResources().getString(R.string.noresults));
        }
    }

    /*
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREDENTIALS_REQUEST) {
            if (resultCode == RESULT_OK) {
                setLoadingPanelVisibility(View.VISIBLE);
                clearMessage();
                retrieveCredentials();
            }
        }
    }
    */
}
