import SQLite, {SQLError} from 'react-native-sqlite-storage';
export class Dao {
  db: SQLite.SQLiteDatabase;
  constructor() {
    this.db = SQLite.openDatabase(
      {name: 'tv.db', location: 'Shared', createFromLocation: 1},
      async () => {
        await db.excute(
          'create table if not exists device (id integer primary key autoincrement, name text unique, application_id integer, foreign key(application_id) references application(id))',
        );
        await db.excute(
          'create table if not exists application (id integer primary key autoincrement, name text)',
        );

        await db.excute(
          'create table if not exists rule (id integer primary key autoincrement, name text, status integer, startTime Date, endTime Date, applicationId integer FOREIGN KEY(applicationId) REFERENCES application(id))',
        );
      },
      (e: SQLError) => {
        console.log('error', e);
      },
    );
  }

  async excute(sql: string, params?: any[]) {
    return this.db.executeSql(sql, params);
  }

  async close() {
    return this.db.close();
  }
}

export const db = new Dao();
