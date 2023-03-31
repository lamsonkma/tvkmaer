import {db, Dao} from '../helper/dao';

class RuleModel {
  db: Dao;

  constructor() {
    this.db = db;
  }

  async getRules() {
    const [results] = await this.db.excute('select * from rule');
    return results;
  }

  async addRule(name: string, status: number, startTime: Date, endTime: Date) {
    return this.db.excute(
      'insert into rule (name, status, startTime, endTime) values (?, ?, ?, ?)',
      [name, status, startTime, endTime],
    );
  }

  async updateRule(
    id: number,
    name: string,
    status: number,
    startTime: Date,
    endTime: Date,
  ) {
    return this.db.excute(
      'update rule set name = ?, status = ?, startTime = ?, endTime = ? where id = ?',
      [name, status, startTime, endTime, id],
    );
  }

  async deleteRule(id: number) {
    return this.db.excute('delete from rule where id = ?', [id]);
  }

  getAllRulesByApplicationId(applicationId: number) {
    return this.db.excute(
      'select * from rule INNER JOIN application ON rule.applicationId = application.id where application.id = ?',
      [applicationId],
    );
  }
}

export const ruleModel = new RuleModel();
