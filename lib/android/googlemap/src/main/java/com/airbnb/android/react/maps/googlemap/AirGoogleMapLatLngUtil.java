package com.airbnb.android.react.maps.googlemap;

import com.airbnb.android.react.maps.common.SimpleBounds;
import com.airbnb.android.react.maps.common.SimpleLatLng;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by jiaming on 2/14/17.
 */

public class AirGoogleMapLatLngUtil {
    public static LatLng convert(SimpleLatLng latLng) {
        if (latLng == null) {
            return null;
        }
        return new LatLng(latLng.latitude, latLng.longitude);
    }
    public static LatLngBounds convert(SimpleBounds bounds) {
        if (bounds == null) {
            return null;
        }
        return new LatLngBounds(convert(bounds.northeast), convert(bounds.southwest));
    }
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
