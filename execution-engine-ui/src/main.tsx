import ReactDOM from "react-dom/client";
import { AuthProvider } from "react-oidc-context";

import App from "./App";
import AppContextProvider from "./context/AppContext";
import SocketContextProvider from "./context/SocketContext.tsx";
import "./index.css";

const oidcConfig = {
  authority: window._env_.OIDC_AUTHORITY,
  client_id: window._env_.OIDC_CLIENT_ID,
  redirect_uri: window._env_.OIDC_REDIRECT_URI,
  post_logout_redirect_uri: "https://darwin-eu.org",
};

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement,
);
root.render(
  <AuthProvider {...oidcConfig}>
    <SocketContextProvider>
      <AppContextProvider>
        <App />
      </AppContextProvider>
    </SocketContextProvider>
  </AuthProvider>,
);
