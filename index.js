import {Alert, AppRegistry, DeviceEventEmitter} from 'react-native';
import App from './App';
import {name as appName} from './app.json';
import messaging from '@react-native-firebase/messaging';
import notifee, {AndroidImportance} from '@notifee/react-native';

AppRegistry.registerComponent(appName, () => App);
