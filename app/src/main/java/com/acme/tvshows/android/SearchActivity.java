package com.acme.tvshows.android;

import java.util.Collections;
import java.util.List;

import com.acme.tvshows.android.model.Credentials;
import com.acme.tvshows.android.service.Show;
import com.acme.tvshows.android.service.ShowServiceException;
import com.acme.tvshows.android.service.Store;
import com.acme.tvshows.android.service.TvShowClient;
import com.acme.tvshows.android.model.FavoriteShow;
import com.acme.tvshows.android.store.DatabaseManager;
import com.acme.tvshows.android.store.StoreException;

import android.util.Log;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.View;
import android.widget.EditText;
import android.content.Intent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ItemSelect;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_search)
public class SearchActivity extends BaseActivity {
    private static final int CREDENTIALS_REQUEST = 3;
    private Store selectedStore;
    @Bean TvShowClient client;
    @Bean DatabaseManager database;
    @ViewById ListView lstShows;
    @ViewById EditText txtShow;
    @ViewById Spinner selectProvider;

    @AfterViews
    void initViews() {
        clearMessage();
        retrieveStores();
    }

    @Background
    void retrieveStores() {
        try {
            showStores(client.getAllStores());
        } catch(ShowServiceException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
    }

    @UiThread
    void showStores(List<Store> stores) {
        ArrayAdapter<Store> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stores);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectProvider.setAdapter(adapter);
        if (stores.isEmpty()) {
            setMessage(getResources().getString(R.string.noresults));
        } else {
            setSelectedStore(stores.get(0).getCode());
        }
    }

    @ItemSelect
    void selectProvider(boolean selected, Store store) {
        if (selected) {
            if (!store.getLoginParameters().isEmpty()) {
                retrieveCredentials(store);
            } else {
                selectedStore = store;
            }
        }
    }

    @Background
    void retrieveCredentials(Store store) {
        try {
            Credentials credentials = database.getCredentials(this, store.getCode());
            showCredentials(store, credentials);
        } catch (StoreException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
    }

    @UiThread
    void showCredentials(Store store, Credentials credentials) {
        if (!store.getLoginParameters().isEmpty() && (credentials == null || !credentials.containsParameters(store.getLoginParameters()))) {
            CredentialsActivity_.intent(this).store(store).startForResult(CREDENTIALS_REQUEST);
        } else {
            selectedStore = store;
        }
    }

    @Click
    void btnSearchShow() {
        if ((selectProvider.getCount() > 0) && (selectProvider.getSelectedItem() != null)) {
            setLoadingPanelVisibility(View.VISIBLE);
            clearMessage();
            lstShows.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Collections.emptyList()));
            findShows(txtShow.getText().toString(), selectedStore.getCode());
        }
    }

    @Background
    void findShows(String searchString, String store) {
        try {
            List<Show> shows = client.findShows(this, store, searchString);
            showShows(shows);
        } catch(ShowServiceException e) {
            Log.e("TvShowClient", e.getMessage(), e);
            setMessage(e.getMessage());
        }
        setLoadingPanelVisibility(View.GONE);
    }

    @UiThread
    void showShows(List<Show> shows) {
        lstShows.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, shows));
        if (shows.isEmpty()) {
            setMessage(getResources().getString(R.string.noresults));
        }
    }

    @ItemClick
    void lstShows(Show basicShow) {
        FavoriteShow show = new FavoriteShow(selectedStore.getCode(), basicShow.getId(), basicShow.getName());
        ShowActivity_.intent(this).show(show).start();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREDENTIALS_REQUEST) {
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
}
