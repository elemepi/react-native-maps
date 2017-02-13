package com.airbnb.android.react.maps;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.amap.api.maps.model.LatLng;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

public class GaodeMapCircleManager extends ViewGroupManager<GaodeMapCircle> {
    private final DisplayMetrics metrics;

    public GaodeMapCircleManager(ReactApplicationContext reactContext) {
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
        return "AIRGaodeMapCircle";
    }

    @Override
    public GaodeMapCircle createViewInstance(ThemedReactContext context) {
        return new GaodeMapCircle(context);
    }

    @ReactProp(name = "center")
    public void setCenter(GaodeMapCircle view, ReadableMap center) {
        view.setCenter(new LatLng(center.getDouble("latitude"), center.getDouble("longitude")));
    }

    @ReactProp(name = "radius", defaultDouble = 0)
    public void setRadius(GaodeMapCircle view, double radius) {
        view.setRadius(radius);
    }

    @ReactProp(name = "strokeWidth", defaultFloat = 1f)
    public void setStrokeWidth(GaodeMapCircle view, float widthInPoints) {
        float widthInScreenPx = metrics.density * widthInPoints; // done for parity with iOS
        view.setStrokeWidth(widthInScreenPx);
    }

    @ReactProp(name = "fillColor", defaultInt = Color.RED, customType = "Color")
    public void setFillColor(GaodeMapCircle view, int color) {
        view.setFillColor(color);
    }

    @ReactProp(name = "strokeColor", defaultInt = Color.RED, customType = "Color")
    public void setStrokeColor(GaodeMapCircle view, int color) {
        view.setStrokeColor(color);
    }

    @ReactProp(name = "zIndex", defaultFloat = 1.0f)
    public void setZIndex(GaodeMapCircle view, float zIndex) {
        view.setZIndex(zIndex);
    }

}
