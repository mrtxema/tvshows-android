package com.acme.tvshows.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.acme.tvshows.android.R;
import com.acme.tvshows.android.store.FavoriteShow;

import java.text.SimpleDateFormat;
import java.util.List;

public class ShowViewAdapter extends ArrayAdapter<FavoriteShow> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public ShowViewAdapter(Context context, List<FavoriteShow> objects) {
        super(context, R.layout.listitem_multiline, objects);
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rowView = inflater.inflate(R.layout.listitem_multiline, null, true);
        FavoriteShow item = getItem(position);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.title);
        txtTitle.setText(item.getShowName());

        if (item.hasNextEpisode()) {
            TextView txtEpisode = (TextView) rowView.findViewById(R.id.episode);
            txtEpisode.setText(String.format("%dx%d %s", item.getNextEpisodeSeason(), item.getNextEpisodeNumber(), item.getNextEpisodeTitle()));
        }

        if (item.getLastUpdate() != null) {
            TextView txtLastUpdate = (TextView) rowView.findViewById(R.id.last_update);
            txtLastUpdate.setText(DATE_FORMAT.format(item.getLastUpdate()));
        }

        return rowView;
    }

}
