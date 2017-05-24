import React from 'react';
import {
  StyleSheet,
  View,
  Text,
  Dimensions,
  TouchableOpacity,
} from 'react-native';

import MapView from 'react-native-maps';

const { width, height } = Dimensions.get('window');

const ASPECT_RATIO = width / height;
const LATITUDE = 31.23295;
const LONGITUDE = 121.3822;
const LATITUDE_DELTA = 0.0922;
const LONGITUDE_DELTA = LATITUDE_DELTA * ASPECT_RATIO;
const SPACE = 0.01;

class Overlays extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      region: {
        latitude: LATITUDE,
        longitude: LONGITUDE,
        latitudeDelta: LATITUDE_DELTA,
        longitudeDelta: LONGITUDE_DELTA,
      },
      startPoint: {
        latitude: LATITUDE + SPACE,
        longitude: LONGITUDE - SPACE,
      },
      endPoint: {
        latitude: LATITUDE - (2 * SPACE),
        longitude: LONGITUDE + (2 * SPACE),
      },
      mode: 'driving',
    };
  }

  render() {
    const { region, startPoint, endPoint, mode } = this.state;
    return (
      <View style={styles.container}>
        <MapView
          provider={this.props.provider}
          style={styles.map}
          initialRegion={region}
        >
          <MapView.Route
            startPoint={startPoint}
            endPoint={endPoint}
            mode={mode}
            texture={require('../custtexture.png')}
            strokeColor="rgba(0,0,200,0.5)"
            strokeWidth={16}
            lineDashPattern={[5, 2, 3, 2]}
          />
        </MapView>
        <View style={styles.buttonContainer}>
          <TouchableOpacity
            onPress={() => this.changeMode('driving')}
            style={[styles.bubble, styles.button]}
          >
            <Text>Driving</Text>
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => this.changeMode('walking')}
            style={[styles.bubble, styles.button]}
          >
            <Text>Walking</Text>
          </TouchableOpacity>
          <TouchableOpacity
            onPress={() => this.changeMode('riding')}
            style={[styles.bubble, styles.button]}
          >
            <Text>Riding</Text>
          </TouchableOpacity>
        </View>
      </View>
    );
  }

  changeMode(mode) {
    this.setState({
      mode,
    });
  }
}

Overlays.propTypes = {
  provider: MapView.ProviderPropType,
};

const styles = StyleSheet.create({
  container: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
  map: {
    ...StyleSheet.absoluteFillObject,
  },
  bubble: {
    flex: 1,
    backgroundColor: 'rgba(255,255,255,0.7)',
    paddingHorizontal: 18,
    paddingVertical: 12,
    borderRadius: 20,
  },
  latlng: {
    width: 200,
    alignItems: 'stretch',
  },
  button: {
    width: 80,
    paddingHorizontal: 12,
    alignItems: 'center',
    marginHorizontal: 10,
  },
  buttonContainer: {
    flexDirection: 'row',
    marginVertical: 20,
    backgroundColor: 'transparent',
  },
});

module.exports = Overlays;
