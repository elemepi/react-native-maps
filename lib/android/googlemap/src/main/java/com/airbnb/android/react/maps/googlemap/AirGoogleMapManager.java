package com.airbnb.android.react.maps.googlemap;

import android.view.View;

import com.airbnb.android.react.maps.common.AirMapManager;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.util.Map;

import javax.annotation.Nullable;

public class AirGoogleMapManager extends AirMapManager<AirGoogleMapView> {

    private static final String REACT_CLASS = "AIRGoogleMap";

    private final Map<String, Integer> MAP_TYPES = MapBuilder.of(
            "standard", GoogleMap.MAP_TYPE_NORMAL,
            "satellite", GoogleMap.MAP_TYPE_SATELLITE,
            "hybrid", GoogleMap.MAP_TYPE_HYBRID,
            "terrain", GoogleMap.MAP_TYPE_TERRAIN,
            "none", GoogleMap.MAP_TYPE_NONE
    );

    private final ReactApplicationContext appContext;

    protected GoogleMapOptions googleMapOptions;

    public AirGoogleMapManager(ReactApplicationContext context) {
        this.appContext = context;
        this.googleMapOptions = new GoogleMapOptions();
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected AirGoogleMapView createViewInstance(ThemedReactContext context) {
        try {
            MapsInitializer.initialize(this.appContext);
        } catch (RuntimeException e) {
            e.printStackTrace();
            emitMapError(context, "Map initialize error", "map_init_error");
        }

        return new AirGoogleMapView(context, this.appContext, this, this.googleMapOptions);
    }

    @ReactProp(name = "region")
    public void setRegion(AirGoogleMapView view, ReadableMap region) {
        view.setRegion(region);
    }

    @ReactProp(name = "mapType")
    public void setMapType(AirGoogleMapView view, @Nullable String mapType) {
        Integer typeId = MAP_TYPES.get(mapType);
        if (typeId != null) {
            view.map.setMapType(typeId);
        }
    }
    
    @ReactProp(name = "customMapStyleString")
    public void setMapStyle(AirGoogleMapView view, @Nullable String customMapStyleString) {
        view.map.setMapStyle(new MapStyleOptions(customMapStyleString));
    }

    @ReactProp(name = "showsUserLocation", defaultBoolean = false)
    public void setShowsUserLocation(AirGoogleMapView view, boolean showUserLocation) {
        view.setShowsUserLocation(showUserLocation);
    }

    @ReactProp(name = "showsMyLocationButton", defaultBoolean = true)
    public void setShowsMyLocationButton(AirGoogleMapView view, boolean showMyLocationButton) {
        view.setShowsMyLocationButton(showMyLocationButton);
    }

    @ReactProp(name = "showsIndoorLevelPicker", defaultBoolean = false)
    public void setShowsIndoorLevelPicker(AirGoogleMapView view, boolean showsIndoorLevelPicker) {
        view.map.getUiSettings().setIndoorLevelPickerEnabled(showsIndoorLevelPicker);
    }

    @ReactProp(name = "toolbarEnabled", defaultBoolean = true)
    public void setToolbarEnabled(AirGoogleMapView view, boolean toolbarEnabled) {
        view.setToolbarEnabled(toolbarEnabled);
    }

    // This is a private prop to improve performance of panDrag by disabling it when the callback is not set
    @ReactProp(name = "handlePanDrag", defaultBoolean = false)
    public void setHandlePanDrag(AirGoogleMapView view, boolean handlePanDrag) {
        view.setHandlePanDrag(handlePanDrag);
    }

    @ReactProp(name = "showsTraffic", defaultBoolean = false)
    public void setShowTraffic(AirGoogleMapView view, boolean showTraffic) {
        view.map.setTrafficEnabled(showTraffic);
    }

    @ReactProp(name = "showsBuildings", defaultBoolean = false)
    public void setShowBuildings(AirGoogleMapView view, boolean showBuildings) {
        view.map.setBuildingsEnabled(showBuildings);
    }

    @ReactProp(name = "showsIndoors", defaultBoolean = false)
    public void setShowIndoors(AirGoogleMapView view, boolean showIndoors) {
        view.map.setIndoorEnabled(showIndoors);
    }

    @ReactProp(name = "showsCompass", defaultBoolean = false)
    public void setShowsCompass(AirGoogleMapView view, boolean showsCompass) {
        view.map.getUiSettings().setCompassEnabled(showsCompass);
    }

    @ReactProp(name = "scrollEnabled", defaultBoolean = false)
    public void setScrollEnabled(AirGoogleMapView view, boolean scrollEnabled) {
        view.map.getUiSettings().setScrollGesturesEnabled(scrollEnabled);
    }

    @ReactProp(name = "zoomEnabled", defaultBoolean = false)
    public void setZoomEnabled(AirGoogleMapView view, boolean zoomEnabled) {
        view.map.getUiSettings().setZoomGesturesEnabled(zoomEnabled);
    }

    @ReactProp(name = "rotateEnabled", defaultBoolean = false)
    public void setRotateEnabled(AirGoogleMapView view, boolean rotateEnabled) {
        view.map.getUiSettings().setRotateGesturesEnabled(rotateEnabled);
    }

    @ReactProp(name = "cacheEnabled", defaultBoolean = false)
    public void setCacheEnabled(AirGoogleMapView view, boolean cacheEnabled) {
        view.setCacheEnabled(cacheEnabled);
    }

    @ReactProp(name = "loadingEnabled", defaultBoolean = false)
    public void setLoadingEnabled(AirGoogleMapView view, boolean loadingEnabled) {
        view.enableMapLoading(loadingEnabled);
    }

    @ReactProp(name = "moveOnMarkerPress", defaultBoolean = true)
    public void setMoveOnMarkerPress(AirGoogleMapView view, boolean moveOnPress) {
        view.setMoveOnMarkerPress(moveOnPress);
    }

    @ReactProp(name = "loadingBackgroundColor", customType = "Color")
    public void setLoadingBackgroundColor(AirGoogleMapView view, @Nullable Integer loadingBackgroundColor) {
        view.setLoadingBackgroundColor(loadingBackgroundColor);
    }

    @ReactProp(name = "loadingIndicatorColor", customType = "Color")
    public void setLoadingIndicatorColor(AirGoogleMapView view, @Nullable Integer loadingIndicatorColor) {
        view.setLoadingIndicatorColor(loadingIndicatorColor);
    }

    @ReactProp(name = "pitchEnabled", defaultBoolean = false)
    public void setPitchEnabled(AirGoogleMapView view, boolean pitchEnabled) {
        view.map.getUiSettings().setTiltGesturesEnabled(pitchEnabled);
    }

    @Override
    public void addView(AirGoogleMapView parent, View child, int index) {
        parent.addFeature(child, index);
    }

    @Override
    public int getChildCount(AirGoogleMapView view) {
        return view.getFeatureCount();
    }

    @Override
    public View getChildAt(AirGoogleMapView view, int index) {
        return view.getFeatureAt(index);
    }

    @Override
    public void removeViewAt(AirGoogleMapView parent, int index) {
        parent.removeFeatureAt(index);
    }

    @Override
    public void updateExtraData(AirGoogleMapView view, Object extraData) {
        view.updateExtraData(extraData);
    }

}
