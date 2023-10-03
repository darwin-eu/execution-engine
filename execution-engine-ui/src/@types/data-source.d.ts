export type DataSource = {
  id: number;
  name?: string;
  description?: string;
  type?: string;
  cdmVersion?: string;
  dbCatalog?: string;
  dbServer?: string;
  dbPort?: string;
  dbName?: string;
  username?: string;
  password?: string;
  connectionString?: string;
  cdmSchema?: string;
  targetSchema?: string;
  resultSchema?: string;
  cohortTargetTable?: string;
};

export type Submission = {
  studyId: string;
  studyTitle: string;
  datasourceId: string;
  entrypoint: string;
  engine: string;
  params?: ConfigParams[];
};

export type ConfigParams = {
  label: string;
  description: string;
  type: string;
  key: string;
  value: string | boolean | string[] | undefined;
};

export type Analysis = {
  id: number;
  studyTitle: string;
  studyId: string;
  analysis: string;
  origin: string;
  dataSource: string;
  status: string;
  author: string;
  created: Date;
  finished: Date;
  logs: Log[];
  comment: string;
  datasourceId: string;
  entrypoint: string;
  engine: string;
  params?: ConfigParams[];
};

export type Log = {
  date: number;
  line: string;
};

export type LibraryItem = {
  id: number;
  name: string;
  description: string;
  created: Date;
  entrypoint?: string;
  engine?: string;
  params?: ConfigParams[];
};

declare global {
  interface Window {
    _env_: {
      BACKEND_BASE_URL: string;
      AUTH_ENABLED: "true" | "false";
      OIDC_AUTHORITY: string;
      OIDC_CLIENT_ID: string;
      OIDC_REDIRECT_URI: string;
    };
  }
}
