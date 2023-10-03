import axios, { AxiosInstance } from "axios";
import React, {
  PropsWithChildren,
  ReactElement,
  createContext,
  useMemo,
  useState,
} from "react";
import { useAuth } from "react-oidc-context";

export type AppContextProps = {
  doAlert: (severity: "success" | "error" | "failed", message: string) => void;
  severity: "success" | "error";
  message: string;
  alertOpen: boolean;
  title: ReactElement;
  setTitle: (title: ReactElement) => void;
  closeAlert: () => void;
  client: AxiosInstance | undefined;
};

const base = window._env_.BACKEND_BASE_URL;

export const AppContext = createContext<AppContextProps | null>(null);

const AppProvider: React.FC<PropsWithChildren> = ({ children }) => {
  const auth = useAuth();
  const [alertOpen, setAlertOpen] = useState<boolean>(false);
  const [severity, setSeverity] = useState<"success" | "error">("success");
  const [message, setMessage] = useState<string>("");
  const [title, setTitle] = useState<ReactElement>(<div />);

  const doAlert = (
    severity: "success" | "error" | "failed",
    message: string,
  ) => {
    message = severity === "failed" ? `Failed to load ${message}` : message;
    setMessage(message);
    setSeverity(severity === "success" ? "success" : "error");
    setAlertOpen(true);
  };

  const client = axios.create({
    baseURL: base,
    headers: { Authorization: `Bearer ${auth.user?.access_token}` },
  });

  function closeAlert() {
    setAlertOpen(false);
  }

  const foo = useMemo(
    () => ({
      doAlert,
      severity,
      message,
      alertOpen,
      setTitle,
      closeAlert,
      title,
      client,
    }),
    [client, severity, title, message, alertOpen],
  );
  return <AppContext.Provider value={foo}>{children}</AppContext.Provider>;
};

export default AppProvider;
