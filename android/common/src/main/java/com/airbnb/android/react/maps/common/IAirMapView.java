package com.airbnb.android.react.maps.common;

import android.graphics.Bitmap;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import javax.annotation.Nullable;

/**
 * Created by jiaming on 2/14/17.
 */

public interface IAirMapView {

    boolean initialized();

    void animateToRegion(SimpleBounds bounds, int duration);
    void animateToCoordinate(SimpleLatLng latLng, int duration);
    void fitToElements(boolean animated);
    void fitToSuppliedMarkers(ReadableArray markerIDsArray, boolean animated);
    void fitToCoordinates(ReadableArray coordinatesArray, ReadableMap edgePadding, boolean animated);

    void snapshot(OnSnapshotReadyCallback callback);

    interface OnSnapshotReadyCallback {
        void onSnapshotReady(@Nullable Bitmap snapshot);
    }

}
