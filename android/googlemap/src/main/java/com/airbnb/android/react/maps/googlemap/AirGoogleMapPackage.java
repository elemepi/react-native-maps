package com.airbnb.android.react.maps.googlemap;

import android.app.Activity;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AirGoogleMapPackage implements ReactPackage {
    public AirGoogleMapPackage(Activity activity) {
    } // backwards compatability

    public AirGoogleMapPackage() {
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        AirGoogleMapCalloutManager calloutManager = new AirGoogleMapCalloutManager();
        AirGoogleMapMarkerManager annotationManager = new AirGoogleMapMarkerManager();
        AirGoogleMapPolylineManager polylineManager = new AirGoogleMapPolylineManager(reactContext);
        AirGoogleMapPolygonManager polygonManager = new AirGoogleMapPolygonManager(reactContext);
        AirGoogleMapCircleManager circleManager = new AirGoogleMapCircleManager(reactContext);
        AirGoogleMapManager mapManager = new AirGoogleMapManager(reactContext);
        AirGoogleMapLiteManager mapLiteManager = new AirGoogleMapLiteManager(reactContext);
        AirGoogleMapUrlTileManager tileManager = new AirGoogleMapUrlTileManager(reactContext);

        return Arrays.<ViewManager>asList(
                calloutManager,
                annotationManager,
                polylineManager,
                polygonManager,
                circleManager,
                mapManager,
                mapLiteManager,
                tileManager
                );
    }
}
