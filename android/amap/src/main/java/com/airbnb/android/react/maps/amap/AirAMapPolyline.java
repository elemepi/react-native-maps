package com.airbnb.android.react.maps.amap;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.airbnb.android.react.maps.common.AirMapFeature;
import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.util.ArrayList;
import java.util.List;

public class AirAMapPolyline extends AirMapFeature<AMap> {

    private PolylineOptions polylineOptions;
    private Polyline polyline;

    private List<LatLng> coordinates;
    private int color;
    private float width;
    private boolean geodesic;
    private float zIndex;

    protected String texture;
    protected Bitmap textureBitmap;

    public AirAMapPolyline(Context context) {
        super(context);
    }

    public void setCoordinates(ReadableArray coordinates) {
        this.coordinates = new ArrayList<>(coordinates.size());
        for (int i = 0; i < coordinates.size(); i++) {
            ReadableMap coordinate = coordinates.getMap(i);
            this.coordinates.add(i,
                    new LatLng(coordinate.getDouble("latitude"), coordinate.getDouble("longitude")));
        }
        if (polyline != null) {
            polyline.setPoints(this.coordinates);
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
        if (this.textureBitmap != null) {
            options.setCustomTexture(BitmapDescriptorFactory.fromBitmap(this.textureBitmap));
        }
        options.color(color);
        options.width(width);
        options.geodesic(geodesic);
        options.zIndex(zIndex);
        return options;
    }

    public void setTexture(ReadableMap map) {
        String newTexture = map.getString("uri");
        if (this.texture != null && this.textureBitmap != null && this.texture.equals(newTexture)) {
            return;
        }
        texture = newTexture;
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(texture)).build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        final DataSource<CloseableReference<CloseableImage>>
                dataSource = imagePipeline.fetchDecodedImage(imageRequest, this);

        dataSource.subscribe(new BaseBitmapDataSubscriber() {

            @Override
            public void onNewResultImpl(@Nullable Bitmap bitmap) {
                if (dataSource.isFinished() && bitmap != null){
                    setTextureBitmap(bitmap);
                    dataSource.close();
                }
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                if (dataSource != null) {
                    dataSource.close();
                }
            }
        }, CallerThreadExecutor.getInstance());
    }

    private void setTextureBitmap(Bitmap bitmap) {
        this.textureBitmap = bitmap;
        if (polyline != null) {
            polyline.setCustomTexture(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
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
