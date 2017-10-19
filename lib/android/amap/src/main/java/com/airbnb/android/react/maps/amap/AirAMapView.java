package com.airbnb.android.react.maps.amap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.airbnb.android.react.maps.common.AirMapFeature;
import com.airbnb.android.react.maps.common.IAirMapView;
import com.airbnb.android.react.maps.common.LatLngBoundsUtils;
import com.airbnb.android.react.maps.common.RegionChangeEvent;
import com.airbnb.android.react.maps.common.SimpleBounds;
import com.airbnb.android.react.maps.common.SimpleLatLng;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.VisibleRegion;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirAMapView extends MapView implements AMap.InfoWindowAdapter, AMap.OnMarkerDragListener, IAirMapView {
    private ProgressBar mapLoadingProgressBar;
    private RelativeLayout mapLoadingLayout;
    private ImageView cacheImageView;
    private Boolean isMapLoaded = false;
    private Integer loadingBackgroundColor = null;
    private Integer loadingIndicatorColor = null;
    private final int baseMapPadding = 50;

    public AMap map;
    private LatLngBounds boundsToMove;
    private boolean showUserLocation = false;
    private boolean isMonitoringRegion = false;
    private boolean isTouchDown = false;
    private boolean handlePanDrag = false;
    private boolean moveOnMarkerPress = true;
    private boolean cacheEnabled = false;

    private static final String[] PERMISSIONS = new String[] {
            "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};

    private final List<AirMapFeature> features = new ArrayList<>();
    private final Map<Marker, AirAMapMarker> markerMap = new HashMap<>();
    private final Map<Polyline, AirAMapPolyline> polylineMap = new HashMap<>();
    private final Map<Polygon, AirAMapPolygon> polygonMap = new HashMap<>();
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetectorCompat gestureDetector;
    private final AirAMapManager manager;
    LifecycleEventListener lifecycleListener;
    private boolean paused = false;
    private boolean destroyed = false;
    private final ThemedReactContext context;
    private final EventDispatcher eventDispatcher;

    private static boolean contextHasBug(Context context) {
        return context == null ||
                context.getResources() == null ||
                context.getResources().getConfiguration() == null;
    }

    // We do this to fix this bug:
    // https://github.com/airbnb/react-native-maps/issues/271
    //
    // which conflicts with another bug regarding the passed in context:
    // https://github.com/airbnb/react-native-maps/issues/1147
    //
    // Doing this allows us to avoid both bugs.
    private static Context getNonBuggyContext(ThemedReactContext reactContext,
                                              ReactApplicationContext appContext) {
        Context superContext = reactContext;
        if (!contextHasBug(appContext.getCurrentActivity())) {
            superContext = appContext.getCurrentActivity();
        } else if (contextHasBug(superContext)) {
            // we have the bug! let's try to find a better context to use
            if (!contextHasBug(reactContext.getCurrentActivity())) {
                superContext = reactContext.getCurrentActivity();
            } else if (!contextHasBug(reactContext.getApplicationContext())) {
                superContext = reactContext.getApplicationContext();
            } else {
                // ¯\_(ツ)_/¯
            }
        }
        return superContext;
    }

    public AirAMapView(ThemedReactContext reactContext, ReactApplicationContext appContext, AirAMapManager manager, AMapOptions options) {
        super(getNonBuggyContext(reactContext, appContext), options);

        this.manager = manager;
        this.context = reactContext;

        super.onCreate(null);
        this.post(new Runnable() {
            @Override
            public void run() {
                onMapReady(getMap());
            }
        });

        final AirAMapView view = this;
        scaleDetector =
                new ScaleGestureDetector(reactContext, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        view.startMonitoringRegion();
                        return true; // stop recording this gesture. let mapview handle it.
                    }
        });

        gestureDetector =
                new GestureDetectorCompat(reactContext, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        view.startMonitoringRegion();
                        return false;
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                            float distanceY) {
                        if (handlePanDrag) {
                            onPanDrag(e2);
                        }
                        view.startMonitoringRegion();
                        return false;
                    }
                });

        this.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override public void onLayoutChange(View v, int left, int top, int right, int bottom,
                int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (!paused) {
                    AirAMapView.this.cacheView();
                }
            }
        });

        eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
    }

    public void onMapReady(final AMap map) {
        if (destroyed) {
            return;
        }
        this.map = map;
        this.map.setInfoWindowAdapter(this);
        this.map.setOnMarkerDragListener(this);

        manager.pushEvent(context, this, "onMapReady", new WritableNativeMap());

        final AirAMapView view = this;

        map.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                WritableMap event;
                AirAMapMarker airMapMarker = markerMap.get(marker);

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "marker-press");
                event.putString("id", airMapMarker.getIdentifier());
                manager.pushEvent(context, view, "onMarkerPress", event);

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "marker-press");
                event.putString("id", airMapMarker.getIdentifier());
                manager.pushEvent(context, markerMap.get(marker), "onPress", event);

                if (view.moveOnMarkerPress) {
                  return false;
                } else {
                  marker.showInfoWindow();
                  return true;
                }
            }
        });

        map.setOnPolylineClickListener(new AMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                WritableMap event = makeClickEventData(polyline.getPoints().get(0));
                event.putString("action", "polyline-press");
                manager.pushEvent(context, polylineMap.get(polyline), "onPress", event);
            }
        });

        map.setOnInfoWindowClickListener(new AMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                WritableMap event;

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "callout-press");
                manager.pushEvent(context, view, "onCalloutPress", event);

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "callout-press");
                AirAMapMarker markerView = markerMap.get(marker);
                manager.pushEvent(context, markerView, "onCalloutPress", event);

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "callout-press");
                AirAMapCallout infoWindow = markerView.getCalloutView();
                if (infoWindow != null) manager.pushEvent(context, infoWindow, "onPress", event);
            }
        });

        map.setOnMapClickListener(new AMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                for (Polygon polygon : polygonMap.keySet()) {
                    if (polygon.contains(point)) {
                        WritableMap event = makeClickEventData(polygon.getPoints().get(0));
                        event.putString("action", "polygon-press");
                        manager.pushEvent(context, polygonMap.get(polygon), "onPress", event);
                        return;
                    }
                }
                WritableMap event = makeClickEventData(point);
                event.putString("action", "press");
                manager.pushEvent(context, view, "onPress", event);
            }
        });

        map.setOnMapLongClickListener(new AMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                WritableMap event = makeClickEventData(point);
                event.putString("action", "long-press");
                manager.pushEvent(context, view, "onLongPress", makeClickEventData(point));
            }
        });

        map.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                LatLng center = position.target;
                lastBoundsEmitted = bounds;
                eventDispatcher.dispatchEvent(new RegionChangeEvent(getId(), AirAMapLatLngUtil.convert(bounds), AirAMapLatLngUtil.convert(center), isTouchDown));
                view.stopMonitoringRegion();
            }

            @Override
            public void onCameraChangeFinish(CameraPosition position) {
                LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                LatLng center = position.target;
                lastBoundsEmitted = bounds;
                eventDispatcher.dispatchEvent(new RegionChangeEvent(getId(), AirAMapLatLngUtil.convert(bounds), AirAMapLatLngUtil.convert(center), isTouchDown));
                view.stopMonitoringRegion();
            }
        });

        isMapLoaded = true;
        AirAMapView.this.cacheView();

        // We need to be sure to disable location-tracking when app enters background, in-case some
        // other module
        // has acquired a wake-lock and is controlling location-updates, otherwise, location-manager
        // will be left
        // updating location constantly, killing the battery, even though some other location-mgmt
        // module may
        // desire to shut-down location-services.
        lifecycleListener = new LifecycleEventListener() {
            @Override
            public void onHostResume() {
                if (hasPermissions()) {
                    //noinspection MissingPermission
                    map.setMyLocationEnabled(showUserLocation);
                }
                synchronized (AirAMapView.this) {
                    AirAMapView.this.onResume();
                    paused = false;
                }
            }

            @Override
            public void onHostPause() {
                if (hasPermissions()) {
                    //noinspection MissingPermission
                    map.setMyLocationEnabled(false);
                }
                synchronized (AirAMapView.this) {
                    AirAMapView.this.onPause();
                    paused = true;
                }
            }

            @Override
            public void onHostDestroy() {
                AirAMapView.this.doDestroy();
            }
        };

        context.addLifecycleEventListener(lifecycleListener);
    }

    public synchronized void doDestroy() {
        if (destroyed) {
            return;
        }
        destroyed = true;

        if (lifecycleListener != null && context != null) {
            context.removeLifecycleEventListener(lifecycleListener);
            lifecycleListener = null;
        }
        if(!paused) {
            onPause();
            paused = true;
        }
        if (!destroyed) {
            onDestroy();
            destroyed = true;
        }

    }

    private boolean hasPermissions() {
        return PermissionChecker.checkSelfPermission(getContext(), PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED ||
                PermissionChecker.checkSelfPermission(getContext(), PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    public void setRegion(ReadableMap region) {
        if (region == null) return;

        Double lng = region.getDouble("longitude");
        Double lat = region.getDouble("latitude");
        Double lngDelta = region.getDouble("longitudeDelta");
        Double latDelta = region.getDouble("latitudeDelta");
        LatLngBounds bounds = new LatLngBounds(
                new LatLng(lat - latDelta / 2, lng - lngDelta / 2), // southwest
                new LatLng(lat + latDelta / 2, lng + lngDelta / 2)  // northeast
        );
        if (super.getHeight() <= 0 || super.getWidth() <= 0) {
            // in this case, our map has not been laid out yet, so we save the bounds in a local
            // variable, and make a guess of zoomLevel 10. Not to worry, though: as soon as layout
            // occurs, we will move the camera to the saved bounds. Note that if we tried to move
            // to the bounds now, it would trigger an exception.
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 10));
            boundsToMove = bounds;
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
            boundsToMove = null;
        }
    }

    public void setShowsUserLocation(boolean showUserLocation) {
        this.showUserLocation = showUserLocation; // hold onto this for lifecycle handling
        if (hasPermissions()) {
            //noinspection MissingPermission
            //
            MyLocationStyle myLocationStyle;
            myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            map.setMyLocationStyle(myLocationStyle);
            map.setMyLocationEnabled(showUserLocation);
        }
    }

    public void setShowsMyLocationButton(boolean showMyLocationButton) {
        if (hasPermissions()) {
            map.getUiSettings().setMyLocationButtonEnabled(showMyLocationButton);
        }
    }

    public void setToolbarEnabled(boolean toolbarEnabled) {
        map.getUiSettings().setZoomControlsEnabled(toolbarEnabled);
    }

    public void setLogoPosition(String position) {
        switch (position) {
            case "left":
                map.getUiSettings().setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT);
                break;
            case "center":
                map.getUiSettings().setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_CENTER);
                break;
            case "right":
                map.getUiSettings().setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_RIGHT);
                break;
        }
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
        this.cacheView();
    }

    public void enableMapLoading(boolean loadingEnabled) {
        if (loadingEnabled && !this.isMapLoaded) {
            this.getMapLoadingLayoutView().setVisibility(View.VISIBLE);
        }
    }

    public void setMoveOnMarkerPress(boolean moveOnPress) {
        this.moveOnMarkerPress = moveOnPress;
    }

    public void setLoadingBackgroundColor(Integer loadingBackgroundColor) {
        this.loadingBackgroundColor = loadingBackgroundColor;

        if (this.mapLoadingLayout != null) {
            if (loadingBackgroundColor == null) {
                this.mapLoadingLayout.setBackgroundColor(Color.WHITE);
            } else {
                this.mapLoadingLayout.setBackgroundColor(this.loadingBackgroundColor);
            }
        }
    }

    public void setLoadingIndicatorColor(Integer loadingIndicatorColor) {
        this.loadingIndicatorColor = loadingIndicatorColor;
        if (this.mapLoadingProgressBar != null) {
            Integer color = loadingIndicatorColor;
            if (color == null) {
                color = Color.parseColor("#606060");
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ColorStateList progressTintList = ColorStateList.valueOf(loadingIndicatorColor);
                ColorStateList secondaryProgressTintList = ColorStateList.valueOf(loadingIndicatorColor);
                ColorStateList indeterminateTintList = ColorStateList.valueOf(loadingIndicatorColor);

                this.mapLoadingProgressBar.setProgressTintList(progressTintList);
                this.mapLoadingProgressBar.setSecondaryProgressTintList(secondaryProgressTintList);
                this.mapLoadingProgressBar.setIndeterminateTintList(indeterminateTintList);
            } else {
                PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                    mode = PorterDuff.Mode.MULTIPLY;
                }
                if (this.mapLoadingProgressBar.getIndeterminateDrawable() != null)
                    this.mapLoadingProgressBar.getIndeterminateDrawable().setColorFilter(color, mode);
                if (this.mapLoadingProgressBar.getProgressDrawable() != null)
                    this.mapLoadingProgressBar.getProgressDrawable().setColorFilter(color, mode);
            }
        }
    }

    public void setHandlePanDrag(boolean handlePanDrag) {
        this.handlePanDrag = handlePanDrag;
    }

    public void addFeature(View child, int index) {
        // Our desired API is to pass up annotations/overlays as children to the mapview component.
        // This is where we intercept them and do the appropriate underlying mapview action.
        if (child instanceof AirAMapMarker) {
            AirAMapMarker annotation = (AirAMapMarker) child;
            annotation.addToMap(map);
            features.add(index, annotation);
            Marker marker = (Marker) annotation.getFeature();
            markerMap.put(marker, annotation);
        } else if (child instanceof AirAMapRoute) {
            AirAMapRoute polylineView = (AirAMapRoute) child;
            polylineView.addToMap(map);
            features.add(index, polylineView);
            Polyline polyline = (Polyline) polylineView.getFeature();
            polylineMap.put(polyline, polylineView);
        } else if (child instanceof AirAMapPolyline) {
            AirAMapPolyline polylineView = (AirAMapPolyline) child;
            polylineView.addToMap(map);
            features.add(index, polylineView);
            Polyline polyline = (Polyline) polylineView.getFeature();
            polylineMap.put(polyline, polylineView);
        } else if (child instanceof AirAMapPolygon) {
            AirAMapPolygon polygonView = (AirAMapPolygon) child;
            polygonView.addToMap(map);
            features.add(index, polygonView);
            Polygon polygon = (Polygon) polygonView.getFeature();
            polygonMap.put(polygon, polygonView);
        } else if (child instanceof AirAMapCircle) {
            AirAMapCircle circleView = (AirAMapCircle) child;
            circleView.addToMap(map);
            features.add(index, circleView);
        } else if (child instanceof AirAMapUrlTile) {
            AirAMapUrlTile urlTileView = (AirAMapUrlTile) child;
            urlTileView.addToMap(map);
            features.add(index, urlTileView);
        } else {
            ViewGroup children = (ViewGroup) child;
            for (int i = 0; i < children.getChildCount(); i++) {
              addFeature(children.getChildAt(i), index);
            }
        }
    }

    public int getFeatureCount() {
        return features.size();
    }

    public View getFeatureAt(int index) {
        return features.get(index);
    }

    public void removeFeatureAt(int index) {
        AirMapFeature feature = features.remove(index);
        if (feature instanceof AirAMapMarker) {
            markerMap.remove(feature.getFeature());
        }
        feature.removeFromMap(map);
    }

    public WritableMap makeClickEventData(LatLng point) {
        WritableMap event = new WritableNativeMap();

        WritableMap coordinate = new WritableNativeMap();
        coordinate.putDouble("latitude", point.latitude);
        coordinate.putDouble("longitude", point.longitude);
        event.putMap("coordinate", coordinate);

        Projection projection = map.getProjection();
        Point screenPoint = projection.toScreenLocation(point);

        WritableMap position = new WritableNativeMap();
        position.putDouble("x", screenPoint.x);
        position.putDouble("y", screenPoint.y);
        event.putMap("position", position);

        return event;
    }

    public void updateExtraData(Object extraData) {
        // if boundsToMove is not null, we now have the MapView's width/height, so we can apply
        // a proper camera move
        if (boundsToMove != null) {
            HashMap<String, Float> data = (HashMap<String, Float>) extraData;
            float width = data.get("width");
            float height = data.get("height");
            map.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                            boundsToMove,
                            (int) width,
                            (int) height,
                            0
                    )
            );
            boundsToMove = null;
        }
    }

    public void animateToRegion(SimpleBounds bounds, int duration) {
        if (map != null) {
            startMonitoringRegion();
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(AirAMapLatLngUtil.convert(bounds), 0), duration, null);
        }
    }

    public void animateToCoordinate(SimpleLatLng coordinate, int duration) {
        if (map != null) {
            startMonitoringRegion();
            map.animateCamera(CameraUpdateFactory.newLatLng(AirAMapLatLngUtil.convert(coordinate)), duration, null);
        }
    }

    public void fitToElements(boolean animated) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        boolean addedPosition = false;

        for (AirMapFeature feature : features) {
            if (feature instanceof AirAMapMarker) {
                Marker marker = (Marker) feature.getFeature();
                builder.include(marker.getPosition());
                addedPosition = true;
            }
            // TODO(lmr): may want to include shapes / etc.
        }
        if (addedPosition) {
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, baseMapPadding);
            if (animated) {
                startMonitoringRegion();
                map.animateCamera(cu);
            } else {
                map.moveCamera(cu);
            }
        }
    }

    public void fitToSuppliedMarkers(List<String> markerIDs, boolean animated) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        boolean addedPosition = false;

        for (AirMapFeature feature : features) {
            if (feature instanceof AirAMapMarker) {
                String identifier = ((AirAMapMarker)feature).getIdentifier();
                Marker marker = (Marker)feature.getFeature();
                if (markerIDs.contains(identifier)) {
                    builder.include(marker.getPosition());
                    addedPosition = true;
                }
            }
        }

        if (addedPosition) {
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, baseMapPadding);
            if (animated) {
                startMonitoringRegion();
                map.animateCamera(cu);
            } else {
                map.moveCamera(cu);
            }
        }
    }

    public void fitToCoordinates(List<SimpleLatLng> coordinatesArray, int left, int top, int right, int bottom, boolean animated) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (SimpleLatLng latLng : coordinatesArray) {
            builder.include(AirAMapLatLngUtil.convert(latLng));
        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBoundsRect(
                bounds,
                baseMapPadding + left,
                baseMapPadding + right,
                baseMapPadding + top,
                baseMapPadding + bottom
        );

        if (animated) {
            startMonitoringRegion();
            map.animateCamera(cu);
        } else {
            map.moveCamera(cu);
        }
    }

    // InfoWindowAdapter interface

    @Override
    public View getInfoWindow(Marker marker) {
        AirAMapMarker markerView = markerMap.get(marker);
        return markerView.getCallout();
    }

    @Override
    public View getInfoContents(Marker marker) {
        AirAMapMarker markerView = markerMap.get(marker);
        return markerView.getInfoContents();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        scaleDetector.onTouchEvent(ev);
        gestureDetector.onTouchEvent(ev);

        int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                this.getParent().requestDisallowInterceptTouchEvent(
                        map != null && map.getUiSettings().isScrollGesturesEnabled());
                isTouchDown = true;
                break;
            case (MotionEvent.ACTION_MOVE):
                startMonitoringRegion();
                break;
            case (MotionEvent.ACTION_UP):
                // Clear this regardless, since isScrollGesturesEnabled() may have been updated
                this.getParent().requestDisallowInterceptTouchEvent(false);
                isTouchDown = false;
                break;
        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    // Timer Implementation

    public void startMonitoringRegion() {
        if (isMonitoringRegion) return;
        timerHandler.postDelayed(timerRunnable, 100);
        isMonitoringRegion = true;
    }

    public void stopMonitoringRegion() {
        if (!isMonitoringRegion) return;
        timerHandler.removeCallbacks(timerRunnable);
        isMonitoringRegion = false;
    }

    private LatLngBounds lastBoundsEmitted;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {

            Projection projection = map.getProjection();
            VisibleRegion region = (projection != null) ? projection.getVisibleRegion() : null;
            LatLngBounds bounds = (region != null) ? region.latLngBounds : null;

            if ((bounds != null) &&
                    (lastBoundsEmitted == null || LatLngBoundsUtils.BoundsAreDifferent(AirAMapLatLngUtil.convert(bounds), AirAMapLatLngUtil.convert(lastBoundsEmitted)))) {
                LatLng center = map.getCameraPosition().target;
                lastBoundsEmitted = bounds;
                if (bounds.northeast != null && bounds.southwest != null) {
                    eventDispatcher.dispatchEvent(new RegionChangeEvent(getId(), AirAMapLatLngUtil.convert(bounds), AirAMapLatLngUtil.convert(center), true));
                }
            }

            timerHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onMarkerDragStart(Marker marker) {
        WritableMap event = makeClickEventData(marker.getPosition());
        manager.pushEvent(context, this, "onMarkerDragStart", event);

        AirAMapMarker markerView = markerMap.get(marker);
        event = makeClickEventData(marker.getPosition());
        manager.pushEvent(context, markerView, "onDragStart", event);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        WritableMap event = makeClickEventData(marker.getPosition());
        manager.pushEvent(context, this, "onMarkerDrag", event);

        AirAMapMarker markerView = markerMap.get(marker);
        event = makeClickEventData(marker.getPosition());
        manager.pushEvent(context, markerView, "onDrag", event);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        WritableMap event = makeClickEventData(marker.getPosition());
        manager.pushEvent(context, this, "onMarkerDragEnd", event);

        AirAMapMarker markerView = markerMap.get(marker);
        event = makeClickEventData(marker.getPosition());
        manager.pushEvent(context, markerView, "onDragEnd", event);
    }

    private ProgressBar getMapLoadingProgressBar() {
        if (this.mapLoadingProgressBar == null) {
            this.mapLoadingProgressBar = new ProgressBar(getContext());
            this.mapLoadingProgressBar.setIndeterminate(true);
        }
        if (this.loadingIndicatorColor != null) {
            this.setLoadingIndicatorColor(this.loadingIndicatorColor);
        }
        return this.mapLoadingProgressBar;
    }

    private RelativeLayout getMapLoadingLayoutView() {
        if (this.mapLoadingLayout == null) {
            this.mapLoadingLayout = new RelativeLayout(getContext());
            this.mapLoadingLayout.setBackgroundColor(Color.LTGRAY);
            this.addView(this.mapLoadingLayout,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            this.mapLoadingLayout.addView(this.getMapLoadingProgressBar(), params);

            this.mapLoadingLayout.setVisibility(View.INVISIBLE);
        }
        this.setLoadingBackgroundColor(this.loadingBackgroundColor);
        return this.mapLoadingLayout;
    }

    private ImageView getCacheImageView() {
        if (this.cacheImageView == null) {
            this.cacheImageView = new ImageView(getContext());
            this.addView(this.cacheImageView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            this.cacheImageView.setVisibility(View.INVISIBLE);
        }
        return this.cacheImageView;
    }

    private void removeCacheImageView() {
        if (this.cacheImageView != null) {
            ((ViewGroup)this.cacheImageView.getParent()).removeView(this.cacheImageView);
            this.cacheImageView = null;
        }
    }

    private void removeMapLoadingProgressBar() {
        if (this.mapLoadingProgressBar != null) {
            ((ViewGroup)this.mapLoadingProgressBar.getParent()).removeView(this.mapLoadingProgressBar);
            this.mapLoadingProgressBar = null;
        }
    }

    private void removeMapLoadingLayoutView() {
        this.removeMapLoadingProgressBar();
        if (this.mapLoadingLayout != null) {
            ((ViewGroup)this.mapLoadingLayout.getParent()).removeView(this.mapLoadingLayout);
            this.mapLoadingLayout = null;
        }
    }

    private void cacheView() {
        if (this.cacheEnabled) {
            final ImageView cacheImageView = this.getCacheImageView();
            final RelativeLayout mapLoadingLayout = this.getMapLoadingLayoutView();
            cacheImageView.setVisibility(View.INVISIBLE);
            mapLoadingLayout.setVisibility(View.VISIBLE);
            if (this.isMapLoaded) {
                this.map.getMapScreenShot(new AMap.OnMapScreenShotListener() {
                    @Override
                    public void onMapScreenShot(Bitmap bitmap) {
                        cacheImageView.setImageBitmap(bitmap);
                        cacheImageView.setVisibility(View.VISIBLE);
                        mapLoadingLayout.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onMapScreenShot(Bitmap bitmap, int i) {

                    }
                });
            }
        }
        else {
            this.removeCacheImageView();
            if (this.isMapLoaded) {
                this.removeMapLoadingLayoutView();
            }
        }
    }

    public void onPanDrag(MotionEvent ev) {
        Point point = new Point((int) ev.getX(), (int) ev.getY());
        LatLng coords = this.map.getProjection().fromScreenLocation(point);
        WritableMap event = makeClickEventData(coords);
        manager.pushEvent(context, this, "onPanDrag", event);
    }

    @Override
    public boolean initialized() {
        return map != null;
    }

    @Override
    public void snapshot(final OnSnapshotReadyCallback callback) {
        map.getMapScreenShot(new AMap.OnMapScreenShotListener() {
            @Override
            public void onMapScreenShot(Bitmap bitmap) {
                callback.onSnapshotReady(bitmap);
            }

            @Override
            public void onMapScreenShot(Bitmap bitmap, int i) {

            }
        });
    }
}
