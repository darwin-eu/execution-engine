import { ThemeProvider, createTheme } from "@mui/material";
import { useContext } from "react";
import { AuthContextProps, useAuth } from "react-oidc-context";
import { BrowserRouter, Route, Routes } from "react-router-dom";

import AlertMessage from "./components/AlertMessage";
import { DrawerHeader } from "./components/DrawerHeader";
import SideBar from "./components/SideBar";
import { AppContext, AppContextProps } from "./context/AppContext";
import { About } from "./pages/about/About.tsx";
import { DataSourcesOverview } from "./pages/data-sources/Overview";
import Submissions from "./pages/execution/Submissions";
import { LibraryOverview } from "./pages/library/Overview.tsx";

const THEME = createTheme({
  components: {
    MuiTable: {
      styleOverrides: {
        root: {
          borderCollapse: "unset",
        },
      },
    },
  },
  shape: { borderRadius: 4 },
  typography: {
    fontFamily: `"Arial"`,
    fontSize: 14,
    fontWeightLight: 300,
    fontWeightRegular: 400,
    fontWeightMedium: 500,
  },
  palette: {
    primary: {
      main: "#004494",
    },
  },
});

const authEnabled = window._env_.AUTH_ENABLED === "true";

function App() {
  const { message, alertOpen, closeAlert, severity } = useContext(
    AppContext,
  ) as AppContextProps;

  const auth: AuthContextProps = useAuth();

  if (!authEnabled || auth.isAuthenticated) {
    return (
      <ThemeProvider theme={THEME}>
        <BrowserRouter>
          <div
            style={{
              minHeight: "100vh",
              display: "flex",
            }}
          >
            <div>
              <SideBar />
            </div>
            <div style={{ width: "95%" }}>
              <DrawerHeader />
              <Routes>
                <Route path="/data-sources" element={<DataSourcesOverview />} />
                <Route path="/code-execution" element={<Submissions />} />
                <Route path="/library" element={<LibraryOverview />} />
                <Route path="/about" element={<About />} />
                <Route path="/" element={<Submissions />} />
              </Routes>
              <AlertMessage
                message={message}
                onClose={closeAlert}
                open={alertOpen}
                severity={severity}
              />
            </div>
          </div>
        </BrowserRouter>
      </ThemeProvider>
    );
  } else if (auth.isLoading) {
    return <div />;
  } else {
    auth
      .signinRedirect()
      .then(() => {})
      .catch((err) => console.log(err));
  }
  return <div />;
}

export default App;
