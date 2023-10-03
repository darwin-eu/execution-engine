import { AxiosError, AxiosInstance } from "axios";

import { Analysis, Submission } from "../@types/data-source";

export const upload = (
  file: File,
  submission: Submission,
  client: AxiosInstance,
): Promise<void> => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append(
    "submission",
    new Blob([JSON.stringify(submission)], {
      type: "application/json",
    }),
  );
  return client.post("/submissions", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

export const rerun = async (
  id: number,
  submission: Submission,
  isLib: boolean,
  client: AxiosInstance,
): Promise<void> => {
  const endpoint = isLib
    ? `/submissions/library/items/${id}`
    : `/submissions/${id}/rerun`;
  return client
    .post(endpoint, submission)
    .then(() => {})
    .catch((err: AxiosError | Error) => {
      throw err;
    });
};

export const cancel = async (
  id: number,
  client: AxiosInstance,
): Promise<void> => {
  return client
    .post(`/submissions/${id}/cancel`)
    .then(() => {})
    .catch((err) => {
      throw err;
    });
};

export const fetchSubmissionLogs = async (
  id: number,
  client: AxiosInstance,
): Promise<Analysis> => {
  return client
    .get<Analysis>(`/submissions/${id}/logs`)
    .then((resp) => {
      return resp.data;
    })
    .catch((err) => {
      throw err;
    });
};

export const fetchSubmissions = async (
  client: AxiosInstance,
): Promise<Analysis[]> => {
  return client
    .get<Analysis[]>(`/submissions`)
    .then((resp) => {
      return resp.data;
    })
    .catch((err) => {
      throw err;
    });
};

export const downloadSubmissionResults = async (
  id: number,
  client: AxiosInstance,
) => {
  const fileName = `${id}.zip`;
  return client
    .get(`/submissions/${id}/results`, {
      responseType: "arraybuffer",
      headers: {
        Accept: "application/zip",
      },
    })
    .then((response) => {
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", fileName); //or any other extension
      document.body.appendChild(link);
      link.click();
    })
    .catch((err: Error | AxiosError) => {
      throw err;
    });
};

export const postComment = async (
  id: number,
  comment: string,
  client: AxiosInstance,
) => {
  return client
    .post(`/submissions/${id}/comments`, { comment })
    .then(() => {})
    .catch((err) => {
      throw err;
    });
};
