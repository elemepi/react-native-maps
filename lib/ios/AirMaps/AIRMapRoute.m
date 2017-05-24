//
// Created by Leland Richardson on 12/27/15.
// Copyright (c) 2015 Facebook. All rights reserved.
//

#import "AIRMapRoute.h"
#import <React/UIView+React.h>


@implementation AIRMapRoute {

}

- (void)setMode:(NSString *)mode {
    _mode = mode;
    [self requestRoute];
}

- (void)setStartPoint:(CLLocationCoordinate2D)startPoint {
    _startPoint = startPoint;
    [self requestRoute];
}

- (void)setEndPoint:(CLLocationCoordinate2D)endPoint {
    _endPoint = endPoint;
    [self requestRoute];
}

- (void) update {
    [super update];
}

- (MKMapItem*) mapItem:(NSString*) name withLocation:(CLLocationCoordinate2D)point {
    MKPlacemark *placemark = [[MKPlacemark alloc] initWithCoordinate:point];
    MKMapItem *mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
    [mapItem setName:name];
    return mapItem;
}

- (void) requestRoute {
    if (!(_mode && _startPoint && _endPoint)) {
        return;
    }
    MKDirectionsRequest *routeReq = [[MKDirectionsRequest alloc] init];
    if ([_mode  isEqual: @"walking"]) {
        routeReq.transportType = MKDirectionsTransportTypeWalking;
    } else if ([_mode isEqual: @"driving"]) {
        routeReq.transportType = MKDirectionsTransportTypeAutomobile;
    } else {
        NSLog(@"Route type not supported: %@", _mode);
    }
    [routeReq setSource:[self mapItem:@"Start" withLocation:_startPoint]];
    [routeReq setDestination:[self mapItem:@"Destination" withLocation:_endPoint]];
    MKDirections *directions = [[MKDirections alloc] initWithRequest:routeReq];
    [directions calculateDirectionsWithCompletionHandler:^(MKDirectionsResponse * response, NSError *err) {
        if (err) {
            NSLog(@"requestRoute Error %@", err);
        } else {
            MKRoute *route = response.routes[0];
            self.polyline = route.polyline;
            self.renderer = [[MKPolylineRenderer alloc] initWithPolyline:self.polyline];
            [self update];
        }
    }];
}

@end
