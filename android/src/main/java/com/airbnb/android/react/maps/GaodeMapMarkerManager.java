package com.airbnb.android.react.maps;

import android.graphics.Color;
import android.view.View;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class GaodeMapMarkerManager extends ViewGroupManager<GaodeMapMarker> {

    private static final int SHOW_INFO_WINDOW = 1;
    private static final int HIDE_INFO_WINDOW = 2;

    public GaodeMapMarkerManager() {
    }

    @Override
    public String getName() {
        return "AIRGaodeMapMarker";
    }

    @Override
    public GaodeMapMarker createViewInstance(ThemedReactContext context) {
        return new GaodeMapMarker(context);
    }

    @ReactProp(name = "coordinate")
    public void setCoordinate(GaodeMapMarker view, ReadableMap map) {
        view.setCoordinate(map);
    }

    @ReactProp(name = "title")
    public void setTitle(GaodeMapMarker view, String title) {
        view.setTitle(title);
    }

    @ReactProp(name = "identifier")
    public void setIdentifier(GaodeMapMarker view, String identifier) {
        view.setIdentifier(identifier);
    }

    @ReactProp(name = "description")
    public void setDescription(GaodeMapMarker view, String description) {
        view.setSnippet(description);
    }

    // NOTE(lmr):
    // android uses normalized coordinate systems for this, and is provided through the
    // `anchor` property  and `calloutAnchor` instead.  Perhaps some work could be done
    // to normalize iOS and android to use just one of the systems.
//    @ReactProp(name = "centerOffset")
//    public void setCenterOffset(GaodeMapMarker view, ReadableMap map) {
//
//    }
//
//    @ReactProp(name = "calloutOffset")
//    public void setCalloutOffset(GaodeMapMarker view, ReadableMap map) {
//
//    }

    @ReactProp(name = "anchor")
    public void setAnchor(GaodeMapMarker view, ReadableMap map) {
        // should default to (0.5, 1) (bottom middle)
        double x = map != null && map.hasKey("x") ? map.getDouble("x") : 0.5;
        double y = map != null && map.hasKey("y") ? map.getDouble("y") : 1.0;
        view.setAnchor(x, y);
    }

    // TODO
    @ReactProp(name = "calloutAnchor")
    public void setCalloutAnchor(GaodeMapMarker view, ReadableMap map) {
        // should default to (0.5, 0) (top middle)
        double x = map != null && map.hasKey("x") ? map.getDouble("x") : 0.5;
        double y = map != null && map.hasKey("y") ? map.getDouble("y") : 0.0;
        view.setCalloutAnchor(x, y);
    }

    @ReactProp(name = "image")
    public void setImage(GaodeMapMarker view, @Nullable String source) {
        view.setImage(source);
    }
//    public void setImage(GaodeMapMarker view, ReadableMap image) {
//        view.setImage(image);
//    }

    @ReactProp(name = "pinColor", defaultInt = Color.RED, customType = "Color")
    public void setPinColor(GaodeMapMarker view, int pinColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(pinColor, hsv);
        // NOTE: android only supports a hue
        view.setMarkerHue(hsv[0]);
    }

    @ReactProp(name = "rotation", defaultFloat = 0.0f)
    public void setMarkerRotation(GaodeMapMarker view, float rotation) {
        view.setRotation(rotation);
    }

    @ReactProp(name = "flat", defaultBoolean = false)
    public void setFlat(GaodeMapMarker view, boolean flat) {
        view.setFlat(flat);
    }

    @ReactProp(name = "draggable", defaultBoolean = false)
    public void setDraggable(GaodeMapMarker view, boolean draggable) {
        view.setDraggable(draggable);
    }

    @Override
    @ReactProp(name = "zIndex", defaultFloat = 0.0f)
    public void setZIndex(GaodeMapMarker view, float zIndex) {
      super.setZIndex(view, zIndex);
      int integerZIndex = Math.round(zIndex);
      view.setZIndex(integerZIndex);
    }

    @Override
    @ReactProp(name = "opacity", defaultFloat = 1.0f)
    public void setOpacity(GaodeMapMarker view, float opacity) {
      super.setOpacity(view, opacity);
      view.setOpacity(opacity);
    }

    @Override
    public void addView(GaodeMapMarker parent, View child, int index) {
        // if an <Callout /> component is a child, then it is a callout view, NOT part of the
        // marker.
        if (child instanceof GaodeMapCallout) {
            parent.setCalloutView((GaodeMapCallout) child);
        } else {
            super.addView(parent, child, index);
            parent.update();
        }
    }

    @Override
    public void removeViewAt(GaodeMapMarker parent, int index) {
        super.removeViewAt(parent, index);
        parent.update();
    }

    @Override
    @Nullable
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                "showCallout", SHOW_INFO_WINDOW,
                "hideCallout", HIDE_INFO_WINDOW
        );
    }

    @Override
    public void receiveCommand(GaodeMapMarker view, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case SHOW_INFO_WINDOW:
                ((Marker) view.getFeature()).showInfoWindow();
                break;

            case HIDE_INFO_WINDOW:
                ((Marker) view.getFeature()).hideInfoWindow();
                break;
        }
    }

    @Override
    @Nullable
    public Map getExportedCustomDirectEventTypeConstants() {
        Map<String, Map<String, String>> map = MapBuilder.of(
                "onPress", MapBuilder.of("registrationName", "onPress"),
                "onCalloutPress", MapBuilder.of("registrationName", "onCalloutPress"),
                "onDragStart", MapBuilder.of("registrationName", "onDragStart"),
                "onDrag", MapBuilder.of("registrationName", "onDrag"),
                "onDragEnd", MapBuilder.of("registrationName", "onDragEnd")
        );

        map.putAll(MapBuilder.of(
                "onDragStart", MapBuilder.of("registrationName", "onDragStart"),
                "onDrag", MapBuilder.of("registrationName", "onDrag"),
                "onDragEnd", MapBuilder.of("registrationName", "onDragEnd")
        ));

        return map;
    }

    @Override
    public LayoutShadowNode createShadowNodeInstance() {
        // we use a custom shadow node that emits the width/height of the view
        // after layout with the updateExtraData method. Without this, we can't generate
        // a bitmap of the appropriate width/height of the rendered view.
        return new SizeReportingShadowNode();
    }

    @Override
    public void updateExtraData(GaodeMapMarker view, Object extraData) {
        // This method is called from the shadow node with the width/height of the rendered
        // marker view.
        HashMap<String, Float> data = (HashMap<String, Float>) extraData;
        float width = data.get("width");
        float height = data.get("height");
        view.update((int) width, (int) height);
    }
}
