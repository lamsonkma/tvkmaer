import {db} from '../helper/dao';

class ApplicationModel {
  async getApplications() {
    const [results] = await db.excute('select * from application');
    return results;
  }

  async getApplicationByName(name: string): Promise<any> {
    const [results] = await db.excute(
      'select * from application where name = ?',
      [name],
    );
    return results;
  }

  async addApplication(name: string): Promise<any> {
    const [results] = await this.getApplicationByName(name);
    if (results.length > 0) {
      return results;
    }
    return db.excute('insert into application (name) values (?)', [name]);
  }

  async deleteApplication(id: number) {
    return db.excute('delete from application where id = ?', [id]);
  }

  async updateApplication(id: number, name: string) {
    return db.excute('update application set name = ? where id = ?', [
      name,
      id,
    ]);
  }
}

export const applicationModel = new ApplicationModel();
