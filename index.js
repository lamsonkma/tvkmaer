/**
 * @format
 */

import {AppRegistry} from 'react-native';
import App from './App';
import {name as appName} from './app.json';
import messaging from '@react-native-firebase/messaging';
import notifee, {AndroidImportance} from '@notifee/react-native';

messaging().setBackgroundMessageHandler(async remoteMessage => {
  const data = remoteMessage.data;
  console.log('Message handled in the background!', remoteMessage);

  return true;
});

AppRegistry.registerComponent(appName, () => App);
