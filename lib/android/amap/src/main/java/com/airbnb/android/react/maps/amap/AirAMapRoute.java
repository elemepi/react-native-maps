package com.airbnb.android.react.maps.amap;

import android.content.Context;

import com.amap.api.maps.model.LatLng;
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
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.events.RCTEventEmitter;

import java.util.ArrayList;
import java.util.List;

public class AirAMapRoute extends AirAMapPolyline {

    private ThemedReactContext context;
    private RouteSearch routeSearch;

    public static final String MODE_DRIVING = "driving";
    public static final String MODE_WALKING = "walking";
    public static final String MODE_RIDING = "riding";

    private String mode;
    private LatLng startPoint;
    private LatLng endPoint;

    public AirAMapRoute(ThemedReactContext context) {
        super(context);
        this.context = context;
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
                        WritableMap data = new WritableNativeMap();
                        data.putDouble("distance", ridePath.getDistance());
                        data.putDouble("duration", ridePath.getDuration());
                        data.putDouble("busDistance", ridePath.getBusDistance());
                        data.putDouble("walkDistance", ridePath.getWalkDistance());
                        data.putDouble("cost", ridePath.getCost());
                        // TODO: steps not included
                        callCallback(data);
                    }
                }
            }

            @Override
            public void onDriveRouteSearched(DriveRouteResult result, int errorCode) {
                if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
                    if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                        final DrivePath ridePath = result.getPaths().get(0);
                        WritableArray steps = new WritableNativeArray();

                        coordinates = new ArrayList<>();
                        List<DriveStep> ridePaths = ridePath.getSteps();
                        coordinates.add(startPoint);
                        for (int i = 0; i < ridePaths.size(); i++) {
                            DriveStep rideStep = ridePaths.get(i);
                            WritableMap step = new WritableNativeMap();
                            step.putDouble("distance", rideStep.getDistance());
                            step.putDouble("duration", rideStep.getDuration());
                            step.putString("action", rideStep.getAction());
                            step.putString("assistantAction", rideStep.getAssistantAction());
                            step.putString("instruction", rideStep.getInstruction());
                            step.putString("orientation", rideStep.getOrientation());
                            step.putString("road", rideStep.getRoad());
                            step.putString("tollRoad", rideStep.getTollRoad());
                            step.putDouble("tollDistance", rideStep.getTollDistance());
                            step.putDouble("tolls", rideStep.getTolls());
                            steps.pushMap(step);

                            for (LatLonPoint point : rideStep.getPolyline()) {
                                coordinates.add(new LatLng(point.getLatitude(), point.getLongitude()));
                            }
                        }
                        coordinates.add(endPoint);
                        polyline.setPoints(coordinates);
                        WritableMap data = new WritableNativeMap();
                        data.putDouble("distance", ridePath.getDistance());
                        data.putDouble("duration", ridePath.getDuration());
                        data.putDouble("tolls", ridePath.getTolls());
                        data.putDouble("tollDistance", ridePath.getTollDistance());
                        data.putDouble("totalTrafficLights", ridePath.getTotalTrafficlights());
                        data.putArray("steps", steps);
                        callCallback(data);
                    }
                }
            }

            @Override
            public void onWalkRouteSearched(WalkRouteResult result, int errorCode) {
                if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
                    if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                        final WalkPath ridePath = result.getPaths().get(0);
                        WritableArray steps = new WritableNativeArray();

                        coordinates = new ArrayList<>();
                        List<WalkStep> ridePaths = ridePath.getSteps();
                        coordinates.add(startPoint);
                        for (int i = 0; i < ridePaths.size(); i++) {
                            WalkStep rideStep = ridePaths.get(i);
                            WritableMap step = new WritableNativeMap();
                            step.putDouble("distance", rideStep.getDistance());
                            step.putDouble("duration", rideStep.getDuration());
                            step.putString("action", rideStep.getAction());
                            step.putString("assistantAction", rideStep.getAssistantAction());
                            step.putString("instruction", rideStep.getInstruction());
                            step.putString("orientation", rideStep.getOrientation());
                            step.putString("road", rideStep.getRoad());
                            steps.pushMap(step);

                            for (LatLonPoint point : rideStep.getPolyline()) {
                                coordinates.add(new LatLng(point.getLatitude(), point.getLongitude()));
                            }
                        }
                        coordinates.add(endPoint);
                        polyline.setPoints(coordinates);

                        WritableMap data = new WritableNativeMap();
                        data.putDouble("distance", ridePath.getDistance());
                        data.putDouble("duration", ridePath.getDuration());
                        data.putArray("steps", steps);
                        callCallback(data);
                    }
                }
            }

            @Override
            public void onRideRouteSearched(RideRouteResult result, int errorCode) {
                if (errorCode == AMapException.CODE_AMAP_SUCCESS) {
                    if (result != null && result.getPaths() != null && result.getPaths().size() > 0) {
                        final RidePath ridePath = result.getPaths().get(0);
                        WritableArray steps = new WritableNativeArray();

                        coordinates = new ArrayList<>();
                        List<RideStep> ridePaths = ridePath.getSteps();
                        coordinates.add(startPoint);
                        for (int i = 0; i < ridePaths.size(); i++) {
                            RideStep rideStep = ridePaths.get(i);
                            WritableMap step = new WritableNativeMap();
                            step.putDouble("distance", rideStep.getDistance());
                            step.putDouble("duration", rideStep.getDuration());
                            step.putString("action", rideStep.getAction());
                            step.putString("assistantAction", rideStep.getAssistantAction());
                            step.putString("instruction", rideStep.getInstruction());
                            step.putString("orientation", rideStep.getOrientation());
                            step.putString("road", rideStep.getRoad());

                            steps.pushMap(step);
                            for (LatLonPoint point : rideStep.getPolyline()) {
                                coordinates.add(new LatLng(point.getLatitude(), point.getLongitude()));
                            }
                        }
                        coordinates.add(endPoint);
                        polyline.setPoints(coordinates);

                        WritableMap data = new WritableNativeMap();
                        data.putDouble("distance", ridePath.getDistance());
                        data.putDouble("duration", ridePath.getDuration());
                        data.putArray("steps", steps);
                        callCallback(data);
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

    private void callCallback(WritableMap map) {
        context.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), "onRouteLoaded", map);
    }

    private void sync() {
        if (startPoint == null || endPoint == null || mode == null) {
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

}

