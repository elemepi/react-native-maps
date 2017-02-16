import { PropTypes } from 'react';
import {
  requireNativeComponent,
  NativeModules,
  Platform,
} from 'react-native';
import {
  PROVIDER_GOOGLE,
  PROVIDER_AMAP,
} from './ProviderConstants';

export const SUPPORTED = 'SUPPORTED';
export const NOT_SUPPORTED = 'NOT_SUPPORTED';

export function getAirMapName(provider) {
  if (provider === PROVIDER_AMAP) return 'AIRAMap';
  if (provider === PROVIDER_GOOGLE) return 'AIRGoogleMap';
  if (Platform.OS === 'android') return 'AIRGoogleMap';
  return 'AIRMap';
}

function getAirComponentName(provider, component) {
  return `${getAirMapName(provider)}${component}`;
}

export const contextTypes = {
  provider: PropTypes.string,
};

export const createNotSupportedComponent = message => () => {
  console.error(message); // eslint-disable-line no-console
  return null;
};

export const isProviderInstalled = provider => !!NativeModules.UIManager[getAirMapName(provider)];

export default function decorateMapComponent(Component, { componentType, providers }) {
  const components = {};

  Component.contextTypes = contextTypes; // eslint-disable-line no-param-reassign

  // eslint-disable-next-line no-param-reassign
  Component.prototype.getAirComponent = function getAirComponent() {
    const provider = this.context.provider;
    if (components[provider]) return components[provider];

    const providerInfo = providers[provider];
    const platformSupport = providerInfo[Platform.OS];
    const componentName = getAirComponentName(provider, componentType);
    if (platformSupport === NOT_SUPPORTED) {
      components[provider] = createNotSupportedComponent(`react-native-maps: ${componentName} is not supported on ${Platform.OS}`); // eslint-disable-line max-len
    } else if (platformSupport === SUPPORTED) {
      if (isProviderInstalled(provider)) {
        components[provider] = requireNativeComponent(componentName, Component);
      } else {
        components[provider] = createNotSupportedComponent(`react-native-maps: ${componentName} is not supported on ${Platform.OS}`); // eslint-disable-line max-len
      }
    }

    return components[provider];
  };

  Component.prototype.getUIManagerCommand = function getUIManagerCommand(name) {  // eslint-disable-line no-param-reassign,max-len
    return NativeModules.UIManager[getAirComponentName(this.context.provider, componentType)]
      .Commands[name];
  };

  Component.prototype.getMapManagerCommand = function getMapManagerCommand(name) { // eslint-disable-line no-param-reassign,max-len
    const airComponentName = `${getAirComponentName(this.context.provider, componentType)}Manager`;
    return NativeModules[airComponentName][name];
  };

  return Component;
}
