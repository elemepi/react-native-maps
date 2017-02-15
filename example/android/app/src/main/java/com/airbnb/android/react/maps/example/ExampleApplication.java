package com.airbnb.android.react.maps.example;

import android.app.Application;

import com.airbnb.android.react.maps.amap.AirAMapPackage;
import com.airbnb.android.react.maps.common.AirMapsPackage;
import com.airbnb.android.react.maps.googlemap.AirGoogleMapPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import java.util.Arrays;
import java.util.List;

public class ExampleApplication extends Application implements ReactApplication {
  private final ReactNativeHost reactNativeHost = new ReactNativeHost(this) {
    @Override protected boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override protected List<ReactPackage> getPackages() {
      return Arrays.asList(
              new MainReactPackage(),
              new AirMapsPackage(),
              new AirAMapPackage());
    }
  };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return reactNativeHost;
  }
}