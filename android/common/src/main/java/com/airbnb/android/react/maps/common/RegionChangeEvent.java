package com.airbnb.android.react.maps.common;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

public class RegionChangeEvent extends Event<RegionChangeEvent> {
    private final SimpleLatLng center;
    private final SimpleBounds bounds;
    private final boolean continuous;

    public RegionChangeEvent(int id, SimpleBounds bounds, SimpleLatLng center, boolean continuous) {
        super(id);
        this.center = center;
        this.bounds = bounds;
        this.continuous = continuous;
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
        region.putDouble("latitude", center.latitude);
        region.putDouble("longitude", center.longitude);
        region.putDouble("latitudeDelta", bounds.northeast.latitude - bounds.southwest.latitude);
        region.putDouble("longitudeDelta", bounds.northeast.longitude - bounds.southwest.longitude);
        event.putMap("region", region);

        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), event);
    }
}
