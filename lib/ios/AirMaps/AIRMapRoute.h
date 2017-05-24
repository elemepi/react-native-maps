//
// Created by Leland Richardson on 12/27/15.
// Copyright (c) 2015 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <MapKit/MapKit.h>
#import <UIKit/UIKit.h>

#import <React/RCTComponent.h>
#import <React/RCTView.h>
#import "AIRMapCoordinate.h"
#import "AIRMap.h"
#import "AIRMapPolyline.h"
#import "RCTConvert+AirMap.h"


@interface AIRMapRoute: AIRMapPolyline 

@property (nonatomic, copy) NSString *mode;
@property (nonatomic, assign) CLLocationCoordinate2D startPoint;
@property (nonatomic, assign) CLLocationCoordinate2D endPoint;

@end
