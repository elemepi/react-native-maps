package com.airbnb.android.react.maps.common;

import android.content.Context;

import com.facebook.react.views.view.ReactViewGroup;

public abstract class AirMapFeature<T> extends ReactViewGroup {
    public AirMapFeature(Context context) {
        super(context);
    }

    public abstract void addToMap(T map);

    public abstract void removeFromMap(T map);

    public abstract Object getFeature();
}
