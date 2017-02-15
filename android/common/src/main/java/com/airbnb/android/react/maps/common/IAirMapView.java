package com.airbnb.android.react.maps.common;

import android.graphics.Bitmap;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by jiaming on 2/14/17.
 */

public interface IAirMapView {

    boolean initialized();

    void animateToRegion(SimpleBounds bounds, int duration);
    void animateToCoordinate(SimpleLatLng latLng, int duration);
    void fitToElements(boolean animated);
    void fitToSuppliedMarkers(List<String> markerIDs, boolean animated);
    void fitToCoordinates(List<SimpleLatLng> coordinatesArray, int left, int top, int right, int bottom, boolean animated);

    void snapshot(OnSnapshotReadyCallback callback);

    interface OnSnapshotReadyCallback {
        void onSnapshotReady(@Nullable Bitmap snapshot);
    }

}
