package com.airbnb.android.react.maps;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

public class RegionChangeEvent extends Event<RegionChangeEvent> {
    static class SimpleLatLng {
        double lat;
        double lng;

        SimpleLatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }
    private final SimpleLatLng center;
    private final SimpleLatLng northeast;
    private final SimpleLatLng southwest;
    private final boolean continuous;

    public RegionChangeEvent(int id, SimpleLatLng center, SimpleLatLng northeast, SimpleLatLng southwest, boolean continuous) {
        super(id);
        this.center = center;
        this.northeast = northeast;
        this.southwest = southwest;
        this.continuous = continuous;
    }

    public RegionChangeEvent(int id,
                             com.google.android.gms.maps.model.LatLngBounds bounds,
                             com.google.android.gms.maps.model.LatLng center,
                             boolean continuous) {
        this(id, new SimpleLatLng(center.latitude, center.longitude),
                new SimpleLatLng(bounds.northeast.latitude, bounds.northeast.longitude),
                new SimpleLatLng(bounds.southwest.latitude, bounds.southwest.longitude),
                continuous);
    }

    public RegionChangeEvent(int id,
                             com.amap.api.maps.model.LatLngBounds bounds,
                             com.amap.api.maps.model.LatLng center,
                             boolean continuous) {
        this(id, new SimpleLatLng(center.latitude, center.longitude),
                new SimpleLatLng(bounds.northeast.latitude, bounds.northeast.longitude),
                new SimpleLatLng(bounds.southwest.latitude, bounds.southwest.longitude),
                continuous);
    }

    @Override
    public String getEventName() {
        return "topChange";
    }

    @Override
    public boolean canCoalesce() {
        return false;
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {

        WritableMap event = new WritableNativeMap();
        event.putBoolean("continuous", continuous);

        WritableMap region = new WritableNativeMap();
        region.putDouble("latitude", center.lat);
        region.putDouble("longitude", center.lng);
        region.putDouble("latitudeDelta", northeast.lat - southwest.lat);
        region.putDouble("longitudeDelta", northeast.lng - southwest.lng);
        event.putMap("region", region);

        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), event);
    }
}
