/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useEffect} from 'react';
import {NativeModules, NativeEventEmitter} from 'react-native';
import BackgroundService from 'react-native-background-actions';
import {Provider} from 'react-redux';
import {apiInstance} from './app/axiosClient';
import {store} from './app/store';
import {IApplication} from './constants/interfaces';
import {HomeScreen} from './screen/HomeScreen';
export const getCurrentAppRunning = async (taskData: {delay: number}) => {
  const {delay} = taskData;
  for (let i = 0; BackgroundService.isRunning(); i++) {
    NativeModules.CurrentAppModule.getCurrentAppInfo(
      async (err: any, result: any) => {
        if (err) {
          console.log('err', err);
        } else {
          console.log('App running', result);
          const rule = await apiInstance.get(
            `/rule/application/879d57d7076efbc5/${result.packageName}`,
          );
          const {data} = rule;
          for (const el of data) {
            if (result.packageName !== 'com.tvkmaer') {
              if (
                Date.now() < Number(el.endTime) &&
                Date.now() > Number(el.startTime)
              ) {
                NativeModules.AppUsageLimitModule.setUsageLimit(
                  el.application.package,
                  el.startTime,
                  el.endTime,
                  (error: any, _res: any) => {
                    if (error) {
                      console.log(error);
                    } else {
                      console.log('set limit success', el.application.package);
                    }
                  },
                );
              } else if (Date.now() < Number(el.startTime)) {
                NativeModules.AppKillModule.killApp();
              }
            }
          }
        }
      },
    );
    await sleep(delay);
  }
};

export const getAppInstalled = async (taskData: {delay: number}) => {
  const {delay} = taskData;
  const regex = /[^\w\s]/gi;
  for (let i = 0; BackgroundService.isRunning(); i++) {
    let applications = await NativeModules.UsageStatsModule.getInstalledApps();
    applications = applications.map((app: IApplication) => {
      app.name = app.name.replace(regex, '');
      return {
        ...app,
      };
    });
    await apiInstance.post('/application', {
      applications,
      token: '879d57d7076efbc5',
    });
    await sleep(delay);
  }
};

export const updateAppUsage = async (taskData: {delay: number}) => {
  const {delay} = taskData;
  for (let i = 0; BackgroundService.isRunning(); i++) {
    const usgae = await NativeModules.UsageStatsModule.queryWeeklyUsageStats();
    const data = usgae.map(
      (item: {
        dayOfWeek: any;
        usageStats: {[x: string]: {totalTimeInForeground: string}};
      }) => {
        return {
          dayOfWeek: item.dayOfWeek,
          applications: Object.keys(item.usageStats).map((key: any) => {
            return {
              packageName: key,
              totalTimeInForeground: parseInt(
                item.usageStats[key].totalTimeInForeground,
                10,
              ),
            };
          }),
          token: '879d57d7076efbc5',
        };
      },
    );
    await apiInstance.post('/usage', {
      data,
    });
    await sleep(delay);
  }
};

const sleep = (time: number) =>
  new Promise<void>(resolve => setTimeout(() => resolve(), time));

BackgroundService.on('expiration', () => {
  console.log('expiration');
});

const tasks: any = [
  {
    taskName: 'Task 1',
    taskTitle: 'Task 1 Title',
    taskDesc: 'Task 1',
    task: getAppInstalled,
    delay: 10000,
  },
  {
    taskName: 'Task 2',
    taskTitle: 'Task 2 Title',
    taskDesc: 'Task 2',
    task: getCurrentAppRunning,
    delay: 1000,
  },
  {
    taskName: 'Task 3',
    taskTitle: 'Task 3 Title',
    taskDesc: 'Task 3',
    task: updateAppUsage,
    delay: 2000,
  },
];

let playing = BackgroundService.isRunning();

const startService = async () => {
  playing = !playing;
  if (playing) {
    try {
      for (let i = 0; i < tasks.length; i++) {
        await BackgroundService.start(tasks[i].task, {
          taskDesc: tasks[i].taskDesc,
          taskName: tasks[i].taskName,
          taskTitle: tasks[i].taskTitle,
          taskIcon: {
            name: 'ic_launcher',
            type: 'mipmap',
          },
          parameters: {
            delay: tasks[i].delay,
          },
        });
      }
    } catch (error) {
      console.log(error);
    }
  }
};

function App(): JSX.Element {
  useEffect(() => {
    let eventEmitter = new NativeEventEmitter();
    eventEmitter.addListener('usageLimitReached', event => {
      console.log('usageLimitReached', event);
      NativeModules.AppKillModule.killApp();
    });
  }, []);

  // Đóng ứng dụng
  // useEffect(() => {
  //   const unsubscribe = messaging().onMessage(async remoteMessage => {
  //     Alert.alert('A new FCM message arrived!', JSON.stringify(remoteMessage));
  //   });

  //   return unsubscribe;
  // }, []);

  // messaging().setBackgroundMessageHandler(async remoteMessage => {
  //   const data = remoteMessage.data;
  //   console.log('Message handled in the background!', data);
  // });

  useEffect(() => {
    startService();
  }, []);
  return (
    <Provider store={store}>
      <HomeScreen />
    </Provider>
  );
}

export default App;
