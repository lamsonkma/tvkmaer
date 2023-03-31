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
};

export const getAppInstalled = async (taskData: {delay: number}) => {
  const {delay} = taskData;
  const regex = /[^\w\s]/gi;
  await new Promise(async () => {
    for (let i = 0; BackgroundService.isRunning(); i++) {
      let applications =
        await NativeModules.UsageStatsModule.getInstalledApps();

      applications = applications.map((app: IApplication) => {
        app.name = app.name.replace(regex, '');
        return {
          ...app,
        };
      });
      const res = await apiInstance.post('/application', {
        applications,
        token: '879d57d7076efbc5',
      });
      await sleep(delay);
    }
  });
};

const sleep = (time: number) =>
  new Promise<void>(resolve => setTimeout(() => resolve(), time));

BackgroundService.on('expiration', () => {
  console.log('expiration');
});

const tasks = [
  {
    taskName: 'Task 1',
    taskTitle: 'Task 1 Title',
    taskDesc: 'Task 1',
    task: getAppInstalled,
    delay: 100000,
  },
  {
    taskName: 'Task 2',
    taskTitle: 'Task 2 Title',
    taskDesc: 'Task 2',
    task: getCurrentAppRunning,
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
  // NativeModules.UsageStatsModule.queryWeeklyUsageStats()
  //   .then(console.log)
  //   .catch(console.log);

  // NativeModules.AppUsageLimitModule.setUsageLimit(
  //   'com.tvkmaer',
  //   `${Date.now()}`,
  //   `${Date.now() + 1 * 10 * 1000}`,
  //   (err: any, result: any) => {
  //     console.log(err, result);
  //   },
  // );

  // useEffect(() => {
  //   let eventEmitter = new NativeEventEmitter();
  //   eventEmitter.addListener('usageLimitReached', event => {
  //     console.log('hello', event); // "someValue"
  //   });
  // }, []);

  // NativeModules.UsageStatsModule.queryWeeklyUsageStats().then(
  //   (res: {usageStats: {[x: string]: {appName: string}}}[]) => {
  //     const regex = /[^\w\s]/gi;

  //     Object.keys(res[0].usageStats)
  //       .map(key => res[0].usageStats[key].appName)
  //       .forEach((appName: string) => {
  //         if (appName.replace(regex, '') === 'Settings') {
  //           console.log('hello', appName.replace(regex, '')); // "someValue"
  //         }
  //       });
  //   },
  // );

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
