package com.airbnb.android.react.maps.googlemap;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
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
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public class AirGoogleMapView extends MapView implements GoogleMap.InfoWindowAdapter,
        GoogleMap.OnMarkerDragListener, OnMapReadyCallback, IAirMapView {
    public GoogleMap map;
    private ProgressBar mapLoadingProgressBar;
    private RelativeLayout mapLoadingLayout;
    private ImageView cacheImageView;
    private Boolean isMapLoaded = false;
    private Integer loadingBackgroundColor = null;
    private Integer loadingIndicatorColor = null;
    private final int baseMapPadding = 50;

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
    private final Map<Marker, AirGoogleMapMarker> markerMap = new HashMap<>();
    private final Map<Polyline, AirGoogleMapPolyline> polylineMap = new HashMap<>();
    private final Map<Polygon, AirGoogleMapPolygon> polygonMap = new HashMap<>();
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetectorCompat gestureDetector;
    private final AirGoogleMapManager manager;
    private boolean paused = false;
    private final ThemedReactContext context;
    private final EventDispatcher eventDispatcher;

    public AirGoogleMapView(ThemedReactContext reactContext, Context appContext, AirGoogleMapManager manager,
                            GoogleMapOptions googleMapOptions) {
        super(appContext, googleMapOptions);

        this.manager = manager;
        this.context = reactContext;

        super.onCreate(null);
        super.onResume();
        super.getMapAsync(this);

        final AirGoogleMapView view = this;
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
                    AirGoogleMapView.this.cacheView();
                }
            }
        });

        eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        this.map = map;
        this.map.setInfoWindowAdapter(this);
        this.map.setOnMarkerDragListener(this);

        manager.pushEvent(this, "onMapReady", new WritableNativeMap());

        final AirGoogleMapView view = this;

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                WritableMap event;
                AirGoogleMapMarker airMapMarker = markerMap.get(marker);

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "marker-press");
                event.putString("id", airMapMarker.getIdentifier());
                manager.pushEvent(view, "onMarkerPress", event);

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "marker-press");
                event.putString("id", airMapMarker.getIdentifier());
                manager.pushEvent(markerMap.get(marker), "onPress", event);

                // Return false to open the callout info window and center on the marker
                // https://developers.google.com/android/reference/com/google/android/gms/maps/GoogleMap.OnMarkerClickListener
                if (view.moveOnMarkerPress) {
                  return false;
                } else {
                  marker.showInfoWindow();
                  return true;
                }
            }
        });

        map.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                WritableMap event = makeClickEventData(polygon.getPoints().get(0));
                event.putString("action", "polygon-press");
                manager.pushEvent(polygonMap.get(polygon), "onPress", event);
            }
        });

        map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                WritableMap event = makeClickEventData(polyline.getPoints().get(0));
                event.putString("action", "polyline-press");
                manager.pushEvent(polylineMap.get(polyline), "onPress", event);
            }
        });

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                WritableMap event;

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "callout-press");
                manager.pushEvent(view, "onCalloutPress", event);

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "callout-press");
                AirGoogleMapMarker markerView = markerMap.get(marker);
                manager.pushEvent(markerView, "onCalloutPress", event);

                event = makeClickEventData(marker.getPosition());
                event.putString("action", "callout-press");
                AirGoogleMapCallout infoWindow = markerView.getCalloutView();
                if (infoWindow != null) manager.pushEvent(infoWindow, "onPress", event);
            }
        });

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                WritableMap event = makeClickEventData(point);
                event.putString("action", "press");
                manager.pushEvent(view, "onPress", event);
            }
        });

        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                WritableMap event = makeClickEventData(point);
                event.putString("action", "long-press");
                manager.pushEvent(view, "onLongPress", makeClickEventData(point));
            }
        });

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
                LatLng center = position.target;
                lastBoundsEmitted = bounds;
                eventDispatcher.dispatchEvent(new RegionChangeEvent(getId(), AirGoogleMapLatLngUtil.convert(bounds), AirGoogleMapLatLngUtil.convert(center), isTouchDown));
                view.stopMonitoringRegion();
            }
        });

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override public void onMapLoaded() {
                isMapLoaded = true;
                AirGoogleMapView.this.cacheView();
            }
        });

        // We need to be sure to disable location-tracking when app enters background, in-case some
        // other module
        // has acquired a wake-lock and is controlling location-updates, otherwise, location-manager
        // will be left
        // updating location constantly, killing the battery, even though some other location-mgmt
        // module may
        // desire to shut-down location-services.
      LifecycleEventListener lifecycleListener = new LifecycleEventListener() {
        @Override
        public void onHostResume() {
          if (hasPermissions()) {
            //noinspection MissingPermission
            map.setMyLocationEnabled(showUserLocation);
          }
          synchronized (AirGoogleMapView.this) {
            AirGoogleMapView.this.onResume();
            paused = false;
          }
        }

        @Override
        public void onHostPause() {
          if (hasPermissions()) {
            //noinspection MissingPermission
            map.setMyLocationEnabled(false);
          }
          paused = true;
        }

        @Override
        public void onHostDestroy() {
        }
      };

        context.addLifecycleEventListener(lifecycleListener);
    }

    private boolean hasPermissions() {
        return checkSelfPermission(getContext(), PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(getContext(), PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
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
            map.setMyLocationEnabled(showUserLocation);
        }
    }

    public void setShowsMyLocationButton(boolean showMyLocationButton) {
        if (hasPermissions()) {
            map.getUiSettings().setMyLocationButtonEnabled(showMyLocationButton);
        }
    }

    public void setToolbarEnabled(boolean toolbarEnabled) {
        if (hasPermissions()) {
            map.getUiSettings().setMapToolbarEnabled(toolbarEnabled);
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
        if (child instanceof AirGoogleMapMarker) {
            AirGoogleMapMarker annotation = (AirGoogleMapMarker) child;
            annotation.addToMap(map);
            features.add(index, annotation);
            Marker marker = (Marker) annotation.getFeature();
            markerMap.put(marker, annotation);
        } else if (child instanceof AirGoogleMapPolyline) {
            AirGoogleMapPolyline polylineView = (AirGoogleMapPolyline) child;
            polylineView.addToMap(map);
            features.add(index, polylineView);
            Polyline polyline = (Polyline) polylineView.getFeature();
            polylineMap.put(polyline, polylineView);
        } else if (child instanceof AirGoogleMapPolygon) {
            AirGoogleMapPolygon polygonView = (AirGoogleMapPolygon) child;
            polygonView.addToMap(map);
            features.add(index, polygonView);
            Polygon polygon = (Polygon) polygonView.getFeature();
            polygonMap.put(polygon, polygonView);
        } else if (child instanceof AirGoogleMapCircle) {
            AirGoogleMapCircle circleView = (AirGoogleMapCircle) child;
            circleView.addToMap(map);
            features.add(index, circleView);
        } else if (child instanceof AirGoogleMapUrlTile) {
            AirGoogleMapUrlTile urlTileView = (AirGoogleMapUrlTile) child;
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
        if (feature instanceof AirGoogleMapMarker) {
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
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(AirGoogleMapLatLngUtil.convert(bounds), 0), duration, null);
        }
    }

    public void animateToCoordinate(SimpleLatLng coordinate, int duration) {
        if (map != null) {
            startMonitoringRegion();
            map.animateCamera(CameraUpdateFactory.newLatLng(AirGoogleMapLatLngUtil.convert(coordinate)), duration, null);
        }
    }

    public void fitToElements(boolean animated) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        boolean addedPosition = false;

        for (AirMapFeature feature : features) {
            if (feature instanceof AirGoogleMapMarker) {
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
            if (feature instanceof AirGoogleMapMarker) {
                String identifier = ((AirGoogleMapMarker)feature).getIdentifier();
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
            builder.include(AirGoogleMapLatLngUtil.convert(latLng));
        }

        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, baseMapPadding);

        map.setPadding(left, top, right, bottom);

        if (animated) {
            startMonitoringRegion();
            map.animateCamera(cu);
        } else {
            map.moveCamera(cu);
        }
        map.setPadding(0, 0, 0, 0); // Without this, the Google logo is moved up by the value of edgePadding.bottom
    }

    // InfoWindowAdapter interface

    @Override
    public View getInfoWindow(Marker marker) {
        AirGoogleMapMarker markerView = markerMap.get(marker);
        return markerView.getCallout();
    }

    @Override
    public View getInfoContents(Marker marker) {
        AirGoogleMapMarker markerView = markerMap.get(marker);
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

            LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            if (lastBoundsEmitted == null ||
                    LatLngBoundsUtils.BoundsAreDifferent(AirGoogleMapLatLngUtil.convert(bounds), AirGoogleMapLatLngUtil.convert(lastBoundsEmitted))) {
                LatLng center = map.getCameraPosition().target;
                lastBoundsEmitted = bounds;
                eventDispatcher.dispatchEvent(new RegionChangeEvent(getId(), AirGoogleMapLatLngUtil.convert(bounds), AirGoogleMapLatLngUtil.convert(center), true));
            }

            timerHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onMarkerDragStart(Marker marker) {
        WritableMap event = makeClickEventData(marker.getPosition());
        manager.pushEvent(this, "onMarkerDragStart", event);

        AirGoogleMapMarker markerView = markerMap.get(marker);
        event = makeClickEventData(marker.getPosition());
        manager.pushEvent(markerView, "onDragStart", event);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        WritableMap event = makeClickEventData(marker.getPosition());
        manager.pushEvent(this, "onMarkerDrag", event);

        AirGoogleMapMarker markerView = markerMap.get(marker);
        event = makeClickEventData(marker.getPosition());
        manager.pushEvent(markerView, "onDrag", event);
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        WritableMap event = makeClickEventData(marker.getPosition());
        manager.pushEvent(this, "onMarkerDragEnd", event);

        AirGoogleMapMarker markerView = markerMap.get(marker);
        event = makeClickEventData(marker.getPosition());
        manager.pushEvent(markerView, "onDragEnd", event);
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
                this.map.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override public void onSnapshotReady(Bitmap bitmap) {
                        cacheImageView.setImageBitmap(bitmap);
                        cacheImageView.setVisibility(View.VISIBLE);
                        mapLoadingLayout.setVisibility(View.INVISIBLE);
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
        manager.pushEvent(this, "onPanDrag", event);
    }

    @Override
    public boolean initialized() {
        return map != null;
    }

    @Override
    public void snapshot(final IAirMapView.OnSnapshotReadyCallback callback) {
        map.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                callback.onSnapshotReady(bitmap);
            }
        });
    }
}