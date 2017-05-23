package com.airbnb.android.react.maps.amap;

import android.content.Context;
import android.util.Log;

import com.airbnb.android.react.maps.common.AirMapFeature;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.busline.BusLineItem;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RideStep;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;
import com.facebook.react.bridge.ReadableMap;

import java.util.ArrayList;
import java.util.List;

public class AirAMapRoute extends AirAMapPolyline {

    private RouteSearch routeSearch;
    private PolylineOptions polylineOptions;
    private Polyline polyline;

    public static final String MODE_DRIVING = "driving";
    public static final String MODE_WALKING = "walking";
    public static final String MODE_RIDING = "riding";

    private String mode;
    private LatLng startPoint;
    private LatLng endPoint;
    private ArrayList<LatLng> coordinates;
    private int color;
    private float width;
    private boolean geodesic;
    private float zIndex;

    public AirAMapRoute(Context context) {
        super(context);
        routeSearch = new RouteSearch(getContext());
        routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
            @Override
            public void onBusRouteSearched(BusRouteResult result, int errorCode) {
                if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
                    if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                        final BusPath ridePath = result.getPaths().get(0);
                        coordinates = new ArrayList<>();
                        List<BusStep> ridePaths = ridePath.getSteps();
                        coordinates.add(startPoint);
                        for (int i = 0; i < ridePaths.size(); i++) {
                            BusStep rideStep = ridePaths.get(i);
                            for (BusLineItem item : rideStep.getBusLines()) {
                                for (LatLonPoint point : item.getDirectionsCoordinates()) {
                                    coordinates.add(new LatLng(point.getLatitude(), point.getLongitude()));
                                }
                            }
                        }
                        coordinates.add(endPoint);
                        polyline.setPoints(coordinates);
                    }
                }
            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
                if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
                    if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                        final DrivePath ridePath = result.getPaths().get(0);
                        coordinates = new ArrayList<>();
                        List<DriveStep> ridePaths = ridePath.getSteps();
                        coordinates.add(startPoint);
                        for (int i = 0; i < ridePaths.size(); i++) {
                            DriveStep rideStep = ridePaths.get(i);
                            for (LatLonPoint point : rideStep.getPolyline()) {
                                coordinates.add(new LatLng(point.getLatitude(), point.getLongitude()));
                            }
                        }
                        coordinates.add(endPoint);
                        Log.d("Map", "onDriveRouteSearched");
                        polyline.setPoints(coordinates);
                    }
                }
            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
                if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
                    if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                        final WalkPath ridePath = result.getPaths().get(0);
                        coordinates = new ArrayList<>();
                        List<WalkStep> ridePaths = ridePath.getSteps();
                        coordinates.add(startPoint);
                        for (int i = 0; i < ridePaths.size(); i++) {
                            WalkStep rideStep = ridePaths.get(i);
                            for (LatLonPoint point : rideStep.getPolyline()) {
                                coordinates.add(new LatLng(point.getLatitude(), point.getLongitude()));
                            }
                        }
                        coordinates.add(endPoint);
                        Log.d("Map", "onWalkRouteSearched");
                        polyline.setPoints(coordinates);
                    }
                }
            }

            @Override
            public void onRideRouteSearched(RideRouteResult result, int errorCode) {
                if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
                    if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                        final RidePath ridePath = result.getPaths().get(0);
                        coordinates = new ArrayList<>();
                        List<RideStep> ridePaths = ridePath.getSteps();
                        coordinates.add(startPoint);
                        for (int i = 0; i < ridePaths.size(); i++) {
                            RideStep rideStep = ridePaths.get(i);
                            for (LatLonPoint point : rideStep.getPolyline()) {
                                coordinates.add(new LatLng(point.getLatitude(), point.getLongitude()));
                            }
                        }
                        coordinates.add(endPoint);
                        Log.d("Map", "onRideRouteSearched");
                        polyline.setPoints(coordinates);
                    }
                }
            }
        });
    }

    public void setStartPoint(ReadableMap map) {
        this.startPoint = new LatLng(map.getDouble("latitude"), map.getDouble("longitude"));
        sync();
    }

    public void setEndPoint(ReadableMap map) {
        this.endPoint = new LatLng(map.getDouble("latitude"), map.getDouble("longitude"));
        sync();
    }

    public void setMode(String mode) {
        this.mode = mode;
        sync();
    }

    private void sync() {
        if (startPoint == null || endPoint == null) {
            return;
        }
        switch (this.mode) {
            case MODE_DRIVING:
                routeSearch.calculateDriveRouteAsyn(new RouteSearch.DriveRouteQuery(new RouteSearch.FromAndTo(
                        new LatLonPoint(startPoint.latitude, startPoint.longitude),
                        new LatLonPoint(endPoint.latitude, endPoint.longitude)
                ), 0, null, null, null));
                break;
            case MODE_WALKING:
                routeSearch.calculateWalkRouteAsyn(new RouteSearch.WalkRouteQuery(new RouteSearch.FromAndTo(
                        new LatLonPoint(startPoint.latitude, startPoint.longitude),
                        new LatLonPoint(endPoint.latitude, endPoint.longitude)
                )));
                break;
            case MODE_RIDING:
                routeSearch.calculateRideRouteAsyn(new RouteSearch.RideRouteQuery(new RouteSearch.FromAndTo(
                        new LatLonPoint(startPoint.latitude, startPoint.longitude),
                        new LatLonPoint(endPoint.latitude, endPoint.longitude)
                )));
                break;
        }
    }

    public void setColor(int color) {
        this.color = color;
        if (polyline != null) {
            polyline.setColor(color);
        }
    }

    public void setWidth(float width) {
        this.width = width;
        if (polyline != null) {
            polyline.setWidth(width);
        }
    }

    public void setZIndex(float zIndex) {
        this.zIndex = zIndex;
        if (polyline != null) {
            polyline.setZIndex(zIndex);
        }
    }

    public void setGeodesic(boolean geodesic) {
        this.geodesic = geodesic;
        if (polyline != null) {
            polyline.setGeodesic(geodesic);
        }
    }

    public PolylineOptions getPolylineOptions() {
        if (polylineOptions == null) {
            polylineOptions = createPolylineOptions();
        }
        return polylineOptions;
    }

    private PolylineOptions createPolylineOptions() {
        PolylineOptions options = new PolylineOptions();
        if (coordinates != null) {
            options.addAll(coordinates);
        }
        options.color(color);
        options.width(width);
        options.geodesic(geodesic);
        options.zIndex(zIndex);
        return options;
    }

    @Override
    public Object getFeature() {
        return polyline;
    }

    @Override
    public void addToMap(AMap map) {
        polyline = map.addPolyline(getPolylineOptions());
    }

    @Override
    public void removeFromMap(AMap map) {
        polyline.remove();
    }
}

