package com.airbnb.android.react.maps.amap;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

public class AirAMapUrlTileManager extends ViewGroupManager<AirAMapUrlTile> {
    private DisplayMetrics metrics;

    public AirAMapUrlTileManager(ReactApplicationContext reactContext) {
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
        return "AIRAMapUrlTile";
    }

    @Override
    public AirAMapUrlTile createViewInstance(ThemedReactContext context) {
        return new AirAMapUrlTile(context);
    }

    @ReactProp(name = "urlTemplate")
    public void setUrlTemplate(AirAMapUrlTile view, String urlTemplate) {
        view.setUrlTemplate(urlTemplate);
    }

    @ReactProp(name = "zIndex", defaultFloat = -1.0f)
    public void setZIndex(AirAMapUrlTile view, float zIndex) {
        view.setZIndex(zIndex);
    }

}
