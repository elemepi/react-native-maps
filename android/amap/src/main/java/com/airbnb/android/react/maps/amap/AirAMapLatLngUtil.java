package com.airbnb.android.react.maps.amap;

import com.airbnb.android.react.maps.common.SimpleBounds;
import com.airbnb.android.react.maps.common.SimpleLatLng;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;

/**
 * Created by jiaming on 2/14/17.
 */

public class AirAMapLatLngUtil {
    public static SimpleLatLng convert(LatLng latLng) {
        if (latLng == null) {
            return null;
        }
        return new SimpleLatLng(latLng.latitude, latLng.longitude);
    }
    public static SimpleBounds convert(LatLngBounds bounds) {
        if (bounds == null) {
            return null;
        }
        return new SimpleBounds(convert(bounds.northeast), convert(bounds.southwest));
    }
}
