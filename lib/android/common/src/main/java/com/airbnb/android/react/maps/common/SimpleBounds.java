package com.airbnb.android.react.maps.common;

/**
 * Created by jiaming on 2/14/17.
 */

public class SimpleBounds {
    public SimpleLatLng northeast;
    public SimpleLatLng southwest;

    public SimpleBounds(SimpleLatLng northeast, SimpleLatLng southwest) {
        this.northeast = northeast;
        this.southwest = southwest;
    }

    public SimpleLatLng getCenter() {
        double lat = (southwest.latitude + northeast.latitude) / 2.0D;
        double lng1 = northeast.longitude;
        double lng2 = southwest.longitude;
        double lng;
        if(lng2 <= lng1) {
            lng = (lng1 + lng2) / 2.0D;
        } else {
            lng = (lng1 + 360.0D + lng2) / 2.0D;
        }

        return new SimpleLatLng(lat, lng);
    }
}
