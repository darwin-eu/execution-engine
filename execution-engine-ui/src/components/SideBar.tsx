import AccountBalanceIcon from "@mui/icons-material/AccountBalance";
import CodeIcon from "@mui/icons-material/Code";
import InfoIcon from "@mui/icons-material/Info";
import KeyboardDoubleArrowLeftIcon from "@mui/icons-material/KeyboardDoubleArrowLeft";
import KeyboardDoubleArrowRightIcon from "@mui/icons-material/KeyboardDoubleArrowRight";
import StorageIcon from "@mui/icons-material/Storage";
import { ListItemButton, ListItemIcon } from "@mui/material";
import MuiAppBar from "@mui/material/AppBar";
import { AppBarProps as MuiAppBarProps } from "@mui/material/AppBar/AppBar";
import Box from "@mui/material/Box";
import Divider from "@mui/material/Divider";
import MuiDrawer from "@mui/material/Drawer";
import IconButton from "@mui/material/IconButton";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import ListItemText from "@mui/material/ListItemText";
import Toolbar from "@mui/material/Toolbar";
import { CSSObject, Theme, styled } from "@mui/material/styles";
import { ReactElement, useContext, useState } from "react";
import { Link } from "react-router-dom";

import darwinLogo from "../assets/darwin-eu-logo.png";
import { AppContext, AppContextProps } from "../context/AppContext";
import { DrawerHeader } from "./DrawerHeader";

const drawerWidth = 240;

const openedMixin = (theme: Theme): CSSObject => ({
  width: drawerWidth,
  transition: theme.transitions.create("width", {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.enteringScreen,
  }),
  overflowX: "hidden",
});

const closedMixin = (theme: Theme): CSSObject => ({
  transition: theme.transitions.create("width", {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.leavingScreen,
  }),
  overflowX: "hidden",
  width: `calc(${theme.spacing(7)} + 1px)`,
  [theme.breakpoints.up("sm")]: {
    width: `calc(${theme.spacing(8)} + 1px)`,
  },
});

interface AppBarProps extends MuiAppBarProps {
  open?: boolean;
}

const AppBar = styled(MuiAppBar, {
  shouldForwardProp: (prop) => prop !== "open",
})<AppBarProps>(({ theme, open }) => ({
  background: "white",
  color: "#004494",
  width: `calc(100% - calc(${theme.spacing(7)} + 1px))`,
  transition: theme.transitions.create(["width", "margin"], {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.leavingScreen,
  }),
  ...(open && {
    background: "white",
    color: "#004494",
    marginLeft: drawerWidth,
    width: `calc(100% - ${drawerWidth}px)`,
    transition: theme.transitions.create(["width", "margin"], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  }),
}));

const Drawer = styled(MuiDrawer, {
  shouldForwardProp: (prop) => prop !== "open",
})(({ theme, open }) => ({
  width: drawerWidth,
  flexShrink: 0,
  whiteSpace: "nowrap",
  boxSizing: "border-box",
  ...(open && {
    ...openedMixin(theme),
    "& .MuiDrawer-paper": openedMixin(theme),
  }),
  ...(!open && {
    ...closedMixin(theme),
    "& .MuiDrawer-paper": closedMixin(theme),
  }),
}));

export default function SideBar() {
  const { title, setTitle } = useContext(AppContext) as AppContextProps;
  const [open, setOpen] = useState(true);

  const tiles: { title: string; link: string; icon: ReactElement }[] = [
    {
      title: "Code Execution",
      link: "code-execution",
      icon: <CodeIcon />,
    },
    {
      title: "Library",
      link: "library",
      icon: <AccountBalanceIcon />,
    },
    {
      title: "Data Sources",
      link: "data-sources",
      icon: <StorageIcon />,
    },
    {
      title: "About",
      link: "about",
      icon: <InfoIcon />,
    },
  ];

  const handleDrawerOpen = () => {
    setOpen(true);
  };

  const handleDrawerClose = () => {
    setOpen(false);
  };

  return (
    <Box sx={{ display: "flex", background: "white", width: "100%" }}>
      <AppBar position="fixed" open={open}>
        <Toolbar style={{ display: "flex", verticalAlign: "middle" }}>
          <div
            style={{
              justifyContent: "flex-start",
              verticalAlign: "middle",
              width: "100%",
            }}
          >
            <h3>{title}</h3>
          </div>
          <a href={"https://darwin-eu.org/"} target={"_blank"} rel="noreferrer">
            <img
              style={{ maxHeight: "44px" }}
              className={"right"}
              src={darwinLogo}
              alt={"Erasmus MC logo"}
            />
          </a>
        </Toolbar>
      </AppBar>
      <Drawer
        variant="permanent"
        open={open}
        PaperProps={{ sx: { background: "#004494", color: "#FFFFFF" } }}
      >
        <DrawerHeader>
          {open && (
            <ListItem
              key={"Phenotype Manager"}
              disablePadding
              sx={{ display: "block", textAlign: "center", fontWeight: 900 }}
            >
              <ListItemText
                primary={"Execution Engine"}
                sx={{
                  opacity: open ? 1 : 0,
                  color: "white",
                  textAlign: "center",
                  fontWeight: "thin",
                }}
              />
            </ListItem>
          )}
          {open ? (
            <IconButton onClick={handleDrawerClose}>
              <KeyboardDoubleArrowLeftIcon sx={{ color: "white" }} />
            </IconButton>
          ) : (
            <IconButton onClick={handleDrawerOpen}>
              <KeyboardDoubleArrowRightIcon sx={{ color: "white" }} />
            </IconButton>
          )}
        </DrawerHeader>
        <Divider sx={{ background: "#FFFFFF" }} />
        <List>
          {tiles.map((text) => (
            <ListItem
              component={Link}
              to={text.link}
              key={text.link}
              disablePadding
              sx={{
                display: "block",
                textDecoration: "none",
                color: "#FFFFFF",
              }}
              onClick={() => setTitle(<div>{text.title}</div>)}
            >
              <ListItemButton
                sx={{
                  minHeight: 48,
                  justifyContent: open ? "initial" : "center",
                  px: 2.5,
                }}
              >
                <ListItemIcon
                  sx={{
                    minWidth: 0,
                    mr: open ? 3 : "auto",
                    justifyContent: "center",
                    color: "#FFFFFF",
                  }}
                >
                  {text.icon}
                </ListItemIcon>
                <ListItemText
                  primary={text.title}
                  sx={{ opacity: open ? 1 : 0 }}
                />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
        {open && (
          <div
            style={{
              position: "absolute",
              bottom: "1em",
              left: "1em",
              fontSize: "small",
            }}
          >
            For terms, conditions and help
            <br />
            <a
              href={"https://servicedesk.darwin-eu.org/"}
              target={"_blank"}
              rel="noreferrer"
              style={{ color: "white" }}
            >
              contact the service desk
            </a>
          </div>
        )}
      </Drawer>
    </Box>
  );
}
