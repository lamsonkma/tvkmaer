import {StyleSheet, Text, View} from 'react-native';
import React, {useEffect, useState} from 'react';
import QRCode from 'react-native-qrcode-svg';
import {useAppDispatch, useAppSelector} from '../app/hook';
import {getAppByIdAction, selectAppById} from '../reducers/applicationSlice';
import {NativeModules} from 'react-native';
import DeviceInfo from 'react-native-device-info';
import {IApplication} from '../constants/interfaces';
export const HomeScreen = () => {
  const deviceInfo = DeviceInfo.getAndroidIdSync();
  const [apps, setApps] = useState<IApplication[]>([]);

  useEffect(() => {
    NativeModules.UsageStatsModule.getInstalledApps().then(
      (app: React.SetStateAction<IApplication[]>) => {
        setApps(app);
      },
    );
  }, []);

  const dispatch = useAppDispatch();

  return (
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
        <QRCode value={deviceInfo} size={200} />
      </View>

      <View />
    </View>
  );
};

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
