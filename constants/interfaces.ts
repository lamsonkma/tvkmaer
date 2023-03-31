export interface IRule {
  id?: number;
  name: string;
  status: number;
  startTime: Date;
  endTime: Date;
}

export interface IApplication {
  id?: number;
  name: string;
  package?: string;
}

export interface IDevice {
  id?: number;
  name?: string;
  image?: string;
  token: string;
}
