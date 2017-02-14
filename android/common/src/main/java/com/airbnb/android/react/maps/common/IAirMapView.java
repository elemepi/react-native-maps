package com.airbnb.android.react.maps.common;

import android.graphics.Bitmap;

import javax.annotation.Nullable;

/**
 * Created by jiaming on 2/14/17.
 */

public interface IAirMapView {

    boolean initialized();
    void snapshot(OnSnapshotReadyCallback callback);

    interface OnSnapshotReadyCallback {
        void onSnapshotReady(@Nullable Bitmap snapshot);
    }

}
