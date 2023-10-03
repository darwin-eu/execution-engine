import { AxiosError, AxiosInstance } from "axios";

import { LibraryItem } from "../@types/data-source";

export const upload = (
  file: File,
  item: LibraryItem,
  client: AxiosInstance,
): Promise<void> => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append(
    "item",
    new Blob([JSON.stringify(item)], {
      type: "application/json",
    }),
  );
  return client.post("/library/items", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

export const fetchLibraryItems = async (
  client: AxiosInstance,
): Promise<LibraryItem[]> => {
  return client
    .get<LibraryItem[]>(`/library/items`)
    .then((resp) => {
      return resp.data;
    })
    .catch((err: AxiosError | Error) => {
      throw err;
    });
};

export const deleteLibraryItem = async (
  id: number,
  client: AxiosInstance,
): Promise<void> => {
  return client
    .delete<void>(`/library/items/${id}`)
    .then((resp) => {
      return resp.data;
    })
    .catch((err: AxiosError | Error) => {
      throw err;
    });
};
