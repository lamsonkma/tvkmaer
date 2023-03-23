/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useEffect} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  NativeModules,
  Alert,
  DeviceEventEmitter,
  TouchableOpacity,
} from 'react-native';
import BackgroundService from 'react-native-background-actions';
import {Colors} from 'react-native/Libraries/NewAppScreen';

import messaging from '@react-native-firebase/messaging';

import QRCode from 'react-native-qrcode-svg';

const sleep = (time: number) =>
  new Promise<void>(resolve => setTimeout(() => resolve(), time));

BackgroundService.on('expiration', () => {
  console.log('expiration');
});

const taskRandom = async taskData => {
  const {delay} = taskData;
  await new Promise(async () => {
    console.log(BackgroundService.isRunning(), delay);
    for (let i = 0; BackgroundService.isRunning(); i++) {
      NativeModules.CurrentAppModule.getCurrentAppInfo(
        (err: any, result: any) => {
          if (err) {
            console.log('err', err);
          } else {
            console.log(result);
          }
        },
      );

      await sleep(delay);
    }
  });
};

let playing = BackgroundService.isRunning();

const startService = async () => {
  playing = !playing;
  if (playing) {
    try {
      await BackgroundService.start(taskRandom, {
        taskDesc: 'Random task',
        taskName: 'RandomTaskName',
        taskTitle: 'Random Task Title',
        taskIcon: {
          name: 'ic_launcher',
          type: 'mipmap',
        },
        parameters: {
          delay: 2000,
        },
      });
    } catch (error) {
      console.log(error);
    }
  }
};

function App(): JSX.Element {
  // useEffect(() => {
  //   const unsubscribe = messaging().onMessage(async remoteMessage => {
  //     Alert.alert('A new FCM message arrived!', JSON.stringify(remoteMessage));
  //   });

  //   return unsubscribe;
  // }, []);

  // NativeModules.UsageStatsModule.queryWeeklyUsageStats()
  //   .then(console.log)
  //   .catch(console.log);

  NativeModules.AppUsageLimitModule.setUsageLimit(
    'com.tvkmaer',
    `${Date.now()}`,
    `${Date.now() + 1 * 60 * 1000}`,
    (err, result) => {
      console.log(err, result);
    },
  );

  useEffect(() => {
    startService();
  }, []);
  return (
    <SafeAreaView
      style={{
        backgroundColor: '#fffff',
      }}>
      <View
        // eslint-disable-next-line react-native/no-inline-styles
        style={{
          backgroundColor: '#fffff',
          flexDirection: 'row',
          height: '100%',
          alignItems: 'center',
          justifyContent: 'space-around',
        }}>
        <View>
          <Text style={styles.title}>To connect TV vs Smart Phone</Text>
          <Text style={styles.content}>
            1. Open the app on the TV and click on the "Connect" button
          </Text>
          <Text style={styles.content}>2. Tab on the "Add new device"</Text>
          <Text style={styles.content}>
            3. Open the camera on the app and scan the QR code on the TV
          </Text>
        </View>
        <View>
          <QRCode
            value="cWfptJUdQoa6Ju1hb_egM5:APA91bGHfRs_ZuzRE7ITkuChspq5UyBKGz7cHnMpTnB5r7cjnHCI4cf3g1OSjC4ohLVmuTE2POSxmd9Rf4J92TVvl-udNnNM4Z9HrlH-O4AKoAKlkE3s0mo0tLorokOlVbuRIYQAi48y"
            size={200}
          />
        </View>

        <View />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },

  title: {
    fontSize: 20,
    fontWeight: '500',
    color: '#000',
  },
  content: {
    fontSize: 16,
    fontWeight: '400',
    color: '#000',
  },
});

export default App;
