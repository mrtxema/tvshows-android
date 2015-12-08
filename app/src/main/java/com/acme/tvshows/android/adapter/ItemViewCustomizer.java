package com.acme.tvshows.android.adapter;

import android.graphics.drawable.Drawable;

public interface ItemViewCustomizer<T> {
    
    Drawable getImage(T object);
}
