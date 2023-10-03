import { MenuItem, Select } from "@mui/material";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import TextField from "@mui/material/TextField";
import * as React from "react";
import { useContext } from "react";

import { DataSource } from "../../@types/data-source";
import { AppContext, AppContextProps } from "../../context/AppContext";
import { saveDataSource } from "../../service/DataSourceService";

interface CreateDataSourceProps {
  dataSource: DataSource;
  setDataSource: React.Dispatch<React.SetStateAction<DataSource>>;
  open: boolean;
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
}

export default function New(props: CreateDataSourceProps) {
  const { doAlert, client } = useContext(AppContext) as AppContextProps;
  const { dataSource, setDataSource, setOpen, open } = props;

  const textFields = [
    { name: "name", label: "CDM Name" },
    { name: "description", label: "CDM Description" },
    { name: "connectionString", label: "JDBC Connection String" },
    { name: "dbCatalog", label: "Database Catalog" },
    { name: "dbServer", label: "Database Server Path" },
    { name: "dbName", label: "Database Name" },
    { name: "dbPort", label: "Database Port" },
    { name: "username", label: "Database Username" },
    { name: "password", label: "Database Password" },
    { name: "cdmSchema", label: "CDM Schema" },
    { name: "targetSchema", label: "Target Schema" },
    { name: "resultSchema", label: "Result Schema" },
    { name: "cohortTargetTable", label: "Cohort Table Name" },
  ];

  const handleInputChange = (field: string, value: string) => {
    setDataSource({ ...dataSource, [field]: value });
  };

  const save = () => {
    if (!client) {
      return;
    }
    if (!dataSource.type) {
      doAlert("error", "Please select a database");
      return;
    }
    if (!dataSource.username) {
      doAlert("error", "Please enter a username");
      return;
    }
    if (!dataSource.cdmSchema) {
      doAlert("error", "Please enter a CDM schema");
      return;
    }
    if (!dataSource.name) {
      doAlert("error", "Please enter a name");
      return;
    }

    saveDataSource(dataSource, client, dataSource.id === 0)
      .then(() => {
        doAlert(
          "success",
          "Database was successfully " + (dataSource ? "updated" : "stored"),
        );
        setOpen(false);
      })
      .catch(() => {
        doAlert(
          "error",
          "Something went wrong submitting database details, phone Adam Black he is to blame",
        );
      });
  };

  return (
    <div>
      <div>
        <Dialog open={open} onClose={() => setOpen(false)}>
          <DialogTitle>
            {dataSource.id ? "Update data source" : "Add a new data source"}
          </DialogTitle>
          <DialogContent>
            <Select
              value={dataSource.type ?? ""}
              fullWidth
              displayEmpty
              labelId="request-select-label"
              id="request-simple-select"
              onChange={(e) => handleInputChange("type", e.target.value)}
            >
              {/* We should get this list from the back-end depending on what drivers are loaded*/}
              <MenuItem value="" disabled>
                Select a database
              </MenuItem>
              <MenuItem value={"MS_SQL_SERVER"}>Microsoft SQL Server</MenuItem>
              <MenuItem value={"ORACLE"}>Oracle</MenuItem>
              <MenuItem value={"POSTGRESQL"}>PostgreSQL</MenuItem>
              <MenuItem value={"REDSHIFT"}>Redshift</MenuItem>
              <MenuItem value={"SNOWFLAKE"}>Snowflake</MenuItem>
              <MenuItem value={"SPARK"}>Spark</MenuItem>
            </Select>
            <div>&#8205;</div>{" "}
            {/* The invisible character to add space. Probably should use css instead. */}
            <Select
              value={dataSource.cdmVersion ?? ""}
              fullWidth
              displayEmpty
              labelId="request-select-label"
              id="request-simple-select"
              onChange={(e) => handleInputChange("cdmVersion", e.target.value)}
            >
              <MenuItem value="" disabled>
                OMOP CDM version
              </MenuItem>
              <MenuItem value={"5.3"}>5.3</MenuItem>
              <MenuItem value={"5.4"}>5.4</MenuItem>
            </Select>
            {/* generate text inputs */}
            {textFields.map((field) => (
              <TextField
                value={dataSource[field.name as keyof DataSource]?.toString()}
                margin="normal"
                id={field.name}
                key={"input-field-" + field.name}
                label={field.label}
                type="string"
                fullWidth
                variant="outlined"
                onChange={(e) => handleInputChange(field.name, e.target.value)}
              />
            ))}
          </DialogContent>
          <DialogActions
            style={{ justifyContent: "space-between", paddingLeft: "1em" }}
          >
            <Button onClick={() => setOpen(false)}>Close</Button>
            <Button onClick={save}>Save</Button>
          </DialogActions>
        </Dialog>
      </div>
    </div>
  );
}
