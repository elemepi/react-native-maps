package com.airbnb.android.react.maps.amap;

import android.os.RemoteException;
import android.view.View;

import com.airbnb.android.react.maps.common.AirMapManager;
import com.airbnb.android.react.maps.common.SizeReportingShadowNode;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.Map;

import javax.annotation.Nullable;

public class AirAMapManager extends AirMapManager<AirAMapView> {

    private static final String REACT_CLASS = "AIRAMap";

    private static final Map<String, Integer> MAP_TYPES = MapBuilder.of(
            "standard", AMap.MAP_TYPE_NORMAL,
            "satellite", AMap.MAP_TYPE_SATELLITE,
            "night", AMap.MAP_TYPE_NIGHT,
            "navigation", AMap.MAP_TYPE_NAVI
    );

    private final ReactApplicationContext appContext;

    protected AMapOptions mapOptions;

    public AirAMapManager(ReactApplicationContext context) {
        this.appContext = context;
        this.mapOptions = new AMapOptions();
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected AirAMapView createViewInstance(ThemedReactContext context) {
        try {
            MapsInitializer.initialize(this.appContext);
        } catch (RuntimeException e) {
            e.printStackTrace();
            emitMapError(context, "Map initialize error", "map_init_error");
        } catch (RemoteException e) {
            e.printStackTrace();
            emitMapError(context, "Map initialize error", "map_init_error");
        }

        return new AirAMapView(context, this.appContext.getCurrentActivity(), this, this.mapOptions);
    }

    @ReactProp(name = "region")
    public void setRegion(AirAMapView view, ReadableMap region) {
        view.setRegion(region);
    }

    @ReactProp(name = "mapType")
    public void setMapType(AirAMapView view, @Nullable String mapType) {
        Integer typeId = MAP_TYPES.get(mapType);
        if (typeId != null) {
            view.map.setMapType(typeId);
        }
    }
    
    @ReactProp(name = "customMapStyleString")
    public void setMapStyle(AirAMapView view, @Nullable String customMapStyleString) {
        view.map.setCustomMapStylePath(customMapStyleString);
    }

    @ReactProp(name = "showsUserLocation", defaultBoolean = false)
    public void setShowsUserLocation(AirAMapView view, boolean showUserLocation) {
        view.setShowsUserLocation(showUserLocation);
    }

    @ReactProp(name = "showsMyLocationButton", defaultBoolean = true)
    public void setShowsMyLocationButton(AirAMapView view, boolean showMyLocationButton) {
        view.setShowsMyLocationButton(showMyLocationButton);
    }

    @ReactProp(name = "toolbarEnabled", defaultBoolean = true)
    public void setToolbarEnabled(AirAMapView view, boolean toolbarEnabled) {
        view.setToolbarEnabled(toolbarEnabled);
    }

    // This is a private prop to improve performance of panDrag by disabling it when the callback is not set
    @ReactProp(name = "handlePanDrag", defaultBoolean = false)
    public void setHandlePanDrag(AirAMapView view, boolean handlePanDrag) {
        view.setHandlePanDrag(handlePanDrag);
    }

    @ReactProp(name = "showsTraffic", defaultBoolean = false)
    public void setShowTraffic(AirAMapView view, boolean showTraffic) {
        view.map.setTrafficEnabled(showTraffic);
    }

    @ReactProp(name = "showsBuildings", defaultBoolean = false)
    public void setShowBuildings(AirAMapView view, boolean showBuildings) {
        view.map.showBuildings(showBuildings);
    }

    @ReactProp(name = "showsIndoors", defaultBoolean = false)
    public void setShowIndoors(AirAMapView view, boolean showIndoors) {
        view.map.showIndoorMap(showIndoors);
    }

    @ReactProp(name = "showsCompass", defaultBoolean = false)
    public void setShowsCompass(AirAMapView view, boolean showsCompass) {
        view.map.getUiSettings().setCompassEnabled(showsCompass);
    }

    @ReactProp(name = "scrollEnabled", defaultBoolean = false)
    public void setScrollEnabled(AirAMapView view, boolean scrollEnabled) {
        view.map.getUiSettings().setScrollGesturesEnabled(scrollEnabled);
    }

    @ReactProp(name = "zoomEnabled", defaultBoolean = false)
    public void setZoomEnabled(AirAMapView view, boolean zoomEnabled) {
        view.map.getUiSettings().setZoomGesturesEnabled(zoomEnabled);
    }

    @ReactProp(name = "rotateEnabled", defaultBoolean = false)
    public void setRotateEnabled(AirAMapView view, boolean rotateEnabled) {
        view.map.getUiSettings().setRotateGesturesEnabled(rotateEnabled);
    }

    @ReactProp(name = "cacheEnabled", defaultBoolean = false)
    public void setCacheEnabled(AirAMapView view, boolean cacheEnabled) {
        view.setCacheEnabled(cacheEnabled);
    }

    @ReactProp(name = "loadingEnabled", defaultBoolean = false)
    public void setLoadingEnabled(AirAMapView view, boolean loadingEnabled) {
        view.enableMapLoading(loadingEnabled);
    }

    @ReactProp(name = "moveOnMarkerPress", defaultBoolean = true)
    public void setMoveOnMarkerPress(AirAMapView view, boolean moveOnPress) {
        view.setMoveOnMarkerPress(moveOnPress);
    }

    @ReactProp(name = "loadingBackgroundColor", customType = "Color")
    public void setLoadingBackgroundColor(AirAMapView view, @Nullable Integer loadingBackgroundColor) {
        view.setLoadingBackgroundColor(loadingBackgroundColor);
    }

    @ReactProp(name = "loadingIndicatorColor", customType = "Color")
    public void setLoadingIndicatorColor(AirAMapView view, @Nullable Integer loadingIndicatorColor) {
        view.setLoadingIndicatorColor(loadingIndicatorColor);
    }

    @ReactProp(name = "pitchEnabled", defaultBoolean = false)
    public void setPitchEnabled(AirAMapView view, boolean pitchEnabled) {
        view.map.getUiSettings().setTiltGesturesEnabled(pitchEnabled);
    }

    @Override
    public void addView(AirAMapView parent, View child, int index) {
        parent.addFeature(child, index);
    }

    @Override
    public int getChildCount(AirAMapView view) {
        return view.getFeatureCount();
    }

    @Override
    public View getChildAt(AirAMapView view, int index) {
        return view.getFeatureAt(index);
    }

    @Override
    public void removeViewAt(AirAMapView parent, int index) {
        parent.removeFeatureAt(index);
    }

    @Override
    public void updateExtraData(AirAMapView view, Object extraData) {
        view.updateExtraData(extraData);
    }

}
