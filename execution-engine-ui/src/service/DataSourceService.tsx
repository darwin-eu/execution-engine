import { AxiosError, AxiosInstance } from "axios";

import { DataSource } from "../@types/data-source";

export const saveDataSource = async (
  dataSource: DataSource,
  client: AxiosInstance,
  isNew: boolean,
) => {
  if (isNew) {
    return client
      .post<void>("/data-sources", dataSource)
      .then(() => {})
      .catch((err) => {
        throw err;
      });
  } else {
    return client
      .put<void>("/data-sources", dataSource)
      .then(() => {})
      .catch((err) => {
        throw err;
      });
  }
};

export const fetchDataSources = async (
  client: AxiosInstance,
): Promise<DataSource[]> => {
  return client
    .get<DataSource[]>(`/data-sources`)
    .then((resp) => {
      return resp.data;
    })
    .catch((err: AxiosError | Error) => {
      throw err;
    });
};

export const deleteDataSource = async (id: number, client: AxiosInstance) => {
  return client
    .delete<DataSource[]>(`/data-sources/${id}`)
    .then((resp) => {
      return resp.data;
    })
    .catch((err) => {
      throw err;
    });
};
