package com.airbnb.android.react.maps.amap;

import android.app.Activity;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AirAMapPackage implements ReactPackage {
    public AirAMapPackage(Activity activity) {
    } // backwards compatability

    public AirAMapPackage() {
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
        AirAMapCalloutManager gaodeCalloutManager = new AirAMapCalloutManager();
        AirAMapMarkerManager gaodeAnnotationManager = new AirAMapMarkerManager();
        AirAMapPolylineManager gaodePolylineManager = new AirAMapPolylineManager(reactContext);
        AirAMapRouteManager gaodeRouteManager = new AirAMapRouteManager(reactContext);
        AirAMapPolygonManager gaodePolygonManager = new AirAMapPolygonManager(reactContext);
        AirAMapCircleManager gaodeCircleManager = new AirAMapCircleManager(reactContext);
        AirAMapManager gaodeMapManager = new AirAMapManager(reactContext);
        AirAMapUrlTileManager gaodeTileManager = new AirAMapUrlTileManager(reactContext);

        return Arrays.<ViewManager>asList(
                gaodeCalloutManager,
                gaodeAnnotationManager,
                gaodePolylineManager,
                gaodeRouteManager,
                gaodePolygonManager,
                gaodeCircleManager,
                gaodeMapManager,
                gaodeTileManager
                );
    }
}
