/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useEffect} from 'react';
import type {PropsWithChildren} from 'react';
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
  Image,
} from 'react-native';

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

import messaging from '@react-native-firebase/messaging';
import notifee, {EventType} from '@notifee/react-native';
import QRCode from 'react-native-qrcode-svg';
type SectionProps = PropsWithChildren<{
  title: string;
}>;

function Section({children, title}: SectionProps): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  return (
    <View style={styles.sectionContainer}>
      <Text
        style={[
          styles.sectionTitle,
          {
            color: isDarkMode ? Colors.white : Colors.black,
          },
        ]}>
        {title}
      </Text>
      <Text
        style={[
          styles.sectionDescription,
          {
            color: isDarkMode ? Colors.light : Colors.dark,
          },
        ]}>
        {children}
      </Text>
    </View>
  );
}

function App(): JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';
  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  useEffect(() => {
    const unsubscribe = messaging().onMessage(async remoteMessage => {
      Alert.alert('A new FCM message arrived!', JSON.stringify(remoteMessage));
    });

    return unsubscribe;
  }, []);

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <View
        // eslint-disable-next-line react-native/no-inline-styles
        style={{
          backgroundColor: isDarkMode ? Colors.black : Colors.white,
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
            3.Open the camera on the app and scan the QR code on the TV
          </Text>
        </View>
        <View>
          <QRCode value="lamson" size={200} />
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
