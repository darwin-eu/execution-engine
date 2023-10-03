import { FilterAlt, MoreVert } from "@mui/icons-material";
import Box from "@mui/material/Box";
import IconButton from "@mui/material/IconButton";

export const About = () => {
  return (
    <Box
      component="main"
      sx={{ flexGrow: 1, p: 3, background: "white", color: "black" }}
    >
      The Execution Engine is an application that allows you to easily run
      DARWIN studies. It is based on the <i>Arachne Data Node</i> and{" "}
      <i>Arachne Execution Engine</i> developed by{" "}
      <a href={"https://odysseusinc.com/"} target={"_blank"} rel="noreferrer">
        Odysseus Data Services
      </a>{" "}
      and has been modified for the DARWIN EU CC.
      <h3 className={"blue"}>How can I filter items in the table?</h3>
      When you hover on a table header you should see an icon like this{" "}
      <IconButton>
        <MoreVert />
      </IconButton>{" "}
      when you click on it options will appear, choose{" "}
      <IconButton>
        <FilterAlt />
      </IconButton>
      to filter.
      <h3 className={"blue"}>How does it work?</h3>
      When you submit a file through the code execution page the application
      spins up a Docker container in which the code is run. Studies either
      contain a configuration file specifying which docker image to use, or the
      end users fills this out in the <i>Execution Environment</i> field. It is
      possible to create your own Docker images through which to execute R code.
      <h3 className={"blue"}>
        Writing R code that can run in the execution engine
      </h3>
      If you want to be able to run your R code in the execution engine there
      are some prerequisites that must be met.
      <li>Your code should be uploaded in zip format</li>
      <li>
        If you are creating your own custom image it must include a /code
        folder, and it should be able to execute the Rscript command
      </li>
      <li>
        Any results that you want to be included in the download at the end must
        be written to a /results folder
      </li>
      <li>
        {" "}
        During runtime, you have access to the following environment variables
        DATA_SOURCE_NAME DBMS_USERNAME DBMS_PASSWORD DBMS_TYPE CONNECTION_STRING
        DBMS_SCHEMA TARGET_SCHEMA RESULT_SCHEMA DBMS_CATALOG DBMS_SERVER
        DBMS_NAME DBMS_PORT CDM_VERSION COHORT_TARGET_TABLE ANALYSIS_ID
      </li>
      <h3 className={"blue"}>Other</h3>
      If you have other questions or would like to report an issue get in touch
      with the DARWIN EU CC{" "}
      <a
        href={"https://servicedesk.darwin-eu.org/"}
        target={"_blank"}
        rel="noreferrer"
      >
        service desk
      </a>
    </Box>
  );
};
