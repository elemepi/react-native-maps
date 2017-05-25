package com.airbnb.android.react.maps.amap;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import java.util.Map;

import javax.annotation.Nullable;

public class AirAMapRouteManager extends ViewGroupManager<AirAMapRoute> {
    private final DisplayMetrics metrics;

    public AirAMapRouteManager(ReactApplicationContext reactContext) {
        super();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            metrics = new DisplayMetrics();
            ((WindowManager) reactContext.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay()
                    .getRealMetrics(metrics);
        } else {
            metrics = reactContext.getResources().getDisplayMetrics();
        }
    }

    @Override
    public String getName() {
        return "AIRAMapRoute";
    }

    @Override
    public AirAMapRoute createViewInstance(ThemedReactContext context) {
        return new AirAMapRoute(context);
    }

    @ReactProp(name = "texture")
    public void setTexture(AirAMapRoute view, ReadableMap texture) {
        view.setTexture(texture);
    }

    @ReactProp(name = "mode")
    public void setMode(AirAMapRoute view, String mode) {
        view.setMode(mode);
    }

    @ReactProp(name = "startPoint")
    public void setStartPoint(AirAMapRoute view, ReadableMap coordinate) {
        view.setStartPoint(coordinate);
    }

    @ReactProp(name = "endPoint")
    public void setEndPoint(AirAMapRoute view, ReadableMap coordinate) {
        view.setEndPoint(coordinate);
    }

    @ReactProp(name = "strokeWidth", defaultFloat = 1f)
    public void setStrokeWidth(AirAMapRoute view, float widthInPoints) {
        float widthInScreenPx = metrics.density * widthInPoints; // done for parity with iOS
        view.setWidth(widthInScreenPx);
    }

    @ReactProp(name = "strokeColor", defaultInt = Color.RED, customType = "Color")
    public void setStrokeColor(AirAMapRoute view, int color) {
        view.setColor(color);
    }

    @ReactProp(name = "geodesic", defaultBoolean = false)
    public void setGeodesic(AirAMapRoute view, boolean geodesic) {
        view.setGeodesic(geodesic);
    }

    @ReactProp(name = "zIndex", defaultFloat = 1.0f)
    public void setZIndex(AirAMapRoute view, float zIndex) {
        view.setZIndex(zIndex);
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
            "onPress", MapBuilder.of("registrationName", "onPress"),
            "onRouteLoaded", MapBuilder.of("registrationName", "onRouteLoaded")
        );
    }
}
