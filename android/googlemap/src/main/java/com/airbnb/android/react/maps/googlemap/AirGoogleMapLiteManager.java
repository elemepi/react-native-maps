package com.airbnb.android.react.maps.googlemap;

import com.facebook.react.bridge.ReactApplicationContext;
import com.google.android.gms.maps.GoogleMapOptions;

public class AirGoogleMapLiteManager extends AirGoogleMapManager {

    private static final String REACT_CLASS = "AIRGoogleMapLite";

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public AirGoogleMapLiteManager(ReactApplicationContext context) {
        super(context);
        this.googleMapOptions = new GoogleMapOptions().liteMode(true);
    }

}
