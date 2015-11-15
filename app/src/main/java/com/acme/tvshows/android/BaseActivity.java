package com.acme.tvshows.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

public abstract class BaseActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState, int layoutResId) {
        super.onCreate(savedInstanceState);
        boolean customTitle = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(layoutResId);
        if (customTitle) {
            getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
        }

        ImageButton btnHome = (ImageButton) findViewById(R.id.btnTitleHome);
        btnHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityByClass(MainActivity.class, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        });

        ImageButton btnSearchShow = (ImageButton) findViewById(R.id.btnTitleSearch);
        btnSearchShow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityByClass(SearchActivity.class);
            }
        });

        ImageButton btnSettings = (ImageButton) findViewById(R.id.btnTitleSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityByClass(SettingsActivity.class);
            }
        });
    }

    protected void startActivityByClass(Class<? extends Activity> target, Integer flags) {
        if (!target.isInstance(this)) {
            Intent intent = new Intent(this, target);
            if (flags != null) {
                intent.setFlags(flags);
            }
            startActivity(intent);
        }
    }

    protected void startActivityByClass(Class<? extends Activity> target) {
        startActivityByClass(target, null);
    }
}
