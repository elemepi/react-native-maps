package com.airbnb.android.react.maps;

import android.app.Activity;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MapsPackage implements ReactPackage {
    public MapsPackage(Activity activity) {
    } // backwards compatability

    public MapsPackage() {
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(new AirMapModule(reactContext));
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        AirMapCalloutManager calloutManager = new AirMapCalloutManager();
        AirMapMarkerManager annotationManager = new AirMapMarkerManager();
        AirMapPolylineManager polylineManager = new AirMapPolylineManager(reactContext);
        AirMapPolygonManager polygonManager = new AirMapPolygonManager(reactContext);
        AirMapCircleManager circleManager = new AirMapCircleManager(reactContext);
        AirMapManager mapManager = new AirMapManager(reactContext);
        AirMapLiteManager mapLiteManager = new AirMapLiteManager(reactContext);
        AirMapUrlTileManager tileManager = new AirMapUrlTileManager(reactContext);

        GaodeMapCalloutManager gaodeCalloutManager = new GaodeMapCalloutManager();
        GaodeMapMarkerManager gaodeAnnotationManager = new GaodeMapMarkerManager();
        GaodeMapPolylineManager gaodePolylineManager = new GaodeMapPolylineManager(reactContext);
        GaodeMapPolygonManager gaodePolygonManager = new GaodeMapPolygonManager(reactContext);
        GaodeMapCircleManager gaodeCircleManager = new GaodeMapCircleManager(reactContext);
        GaodeMapManager gaodeMapManager = new GaodeMapManager(reactContext);
        GaodeMapUrlTileManager gaodeTileManager = new GaodeMapUrlTileManager(reactContext);

        return Arrays.<ViewManager>asList(
                calloutManager,
                annotationManager,
                polylineManager,
                polygonManager,
                circleManager,
                mapManager,
                gaodeMapManager,
                mapLiteManager,
                tileManager,

                gaodeCalloutManager,
                gaodeAnnotationManager,
                gaodePolylineManager,
                gaodePolygonManager,
                gaodeCircleManager,
                gaodeMapManager,
                gaodeTileManager
                );
    }
}
