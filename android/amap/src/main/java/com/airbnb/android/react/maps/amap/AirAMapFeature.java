package com.airbnb.android.react.maps.amap;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.facebook.react.views.view.ReactViewGroup;

public abstract class AirAMapFeature extends ReactViewGroup {
    public AirAMapFeature(Context context) {
        super(context);
    }

    public abstract void addToMap(AMap map);

    public abstract void removeFromMap(AMap map);

    public abstract Object getFeature();
}
