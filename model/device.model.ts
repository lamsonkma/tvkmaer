import {db, Dao} from '../helper/dao';

class DeviceModel {
  db: Dao;
  constructor() {
    this.db = db;
  }
}

export const deviceModel = new DeviceModel();
