import SQLite, {SQLError} from 'react-native-sqlite-storage';
export class Dao {
  db: SQLite.SQLiteDatabase;
  constructor() {
    this.db = SQLite.openDatabase(
      {name: 'test.db', location: 'default'},
      () => {
        console.log('success');
      },
      (e: SQLError) => {
        console.log('error', e);
      },
    );
  }

  async excute(sql: string, params: any[]) {
    return this.db.executeSql(sql, params);
  }

  async close() {
    return this.db.close();
  }
}
