package com.airbnb.android.react.maps.googlemap;

import android.content.Context;

import com.facebook.react.views.view.ReactViewGroup;
import com.google.android.gms.maps.GoogleMap;

public abstract class AirGoogleMapFeature extends ReactViewGroup {
    public AirGoogleMapFeature(Context context) {
        super(context);
    }

    public abstract void addToMap(GoogleMap map);

    public abstract void removeFromMap(GoogleMap map);

    public abstract Object getFeature();
}
