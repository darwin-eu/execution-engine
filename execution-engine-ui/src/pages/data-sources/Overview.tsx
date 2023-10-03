import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import IconButton from "@mui/material/IconButton";
import { DataGrid } from "@mui/x-data-grid";
import { useContext, useEffect, useState } from "react";

import { DataSource } from "../../@types/data-source";
import "../../App.css";
import { AppContext, AppContextProps } from "../../context/AppContext";
import {
  deleteDataSource,
  fetchDataSources,
} from "../../service/DataSourceService";
import New from "./New";

export const DataSourcesOverview = () => {
  const { client, doAlert } = useContext(AppContext) as AppContextProps;
  const [rows, setRows] = useState<DataSource[]>([]);
  const delay = (ms: number) => new Promise((res) => setTimeout(res, ms));
  const [selected, setSelected] = useState<DataSource>({ id: 0 });
  const [editing, setEditing] = useState<boolean>(false);

  useEffect(() => {
    if (client) {
      fetchDataSources(client)
        .then((r) => {
          setRows(r);
        })
        .catch(() => doAlert("error", "Failed to retrieve data sources"));
    }
  }, [editing]);

  function edit(ds: DataSource) {
    setSelected(ds);
    setEditing(true);
  }

  function deleteItem(id: number) {
    if (client) {
      deleteDataSource(id, client)
        .then(() => {
          // Give it a tiny bit of time
          delay(300)
            .then(() =>
              fetchDataSources(client).then((r) => {
                setRows(r);
              }),
            )
            .catch(() => {});
        })
        .catch(() => doAlert("error", "Failed to delete datasource"));
    }
  }

  return (
    <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
      <div>
        <div className={"Buttons-wrapper__new"}>
          <Button
            variant="contained"
            className={"Button__new"}
            size="small"
            onClick={() => {
              setSelected({ id: 0 });
              setEditing(true);
            }}
          >
            NEW DATA SOURCE
          </Button>
          <New
            dataSource={selected}
            setDataSource={setSelected}
            setOpen={setEditing}
            open={editing}
          />
        </div>
        {rows && (
          <div className={"DataGrids-wrapper"}>
            <DataGrid
              initialState={{
                columns: {
                  columnVisibilityModel: {
                    id: false,
                    type: false,
                    connectionString: false,
                    cdmSchema: false,
                    targetSchema: false,
                    resultSchema: false,
                  },
                },
                sorting: {
                  sortModel: [{ field: "created", sort: "desc" }],
                },
              }}
              rows={rows}
              columns={[
                {
                  width: 70,
                  field: "link",
                  align: "center",
                  headerName: "",
                  sortable: false,
                  filterable: false,
                  renderCell: (params) => (
                    <IconButton
                      onClick={() => edit(params.row)}
                      key={params.row.id + "icon-button"}
                    >
                      <EditIcon color={"primary"} />
                    </IconButton>
                  ),
                },
                {
                  minWidth: 80,
                  field: "id",
                  headerName: "id",
                  type: "number",
                  flex: 1,
                },
                {
                  minWidth: 250,
                  field: "name",
                  headerName: "name",
                  type: "string",
                  flex: 3,
                },
                {
                  field: "description",
                  headerName: "description",
                  type: "string",
                  flex: 4,
                },
                {
                  minWidth: 80,
                  field: "type",
                  headerName: "type",
                  type: "string",
                  flex: 1,
                },
                {
                  minWidth: 80,
                  field: "cdmSchema",
                  headerName: "cdm schema",
                  type: "string",
                  flex: 1,
                },
                {
                  minWidth: 80,
                  field: "targetSchema",
                  headerName: "target schema",
                  type: "string",
                  flex: 1,
                },
                {
                  minWidth: 80,
                  field: "resultSchema",
                  headerName: "result schema",
                  type: "string",
                  flex: 1,
                },
                {
                  field: "connectionString",
                  headerName: "connection string",
                  type: "string",
                  flex: 4,
                },
                {
                  minWidth: 80,
                  field: "delete",
                  flex: 1,
                  headerName: "delete",
                  sortable: false,
                  align: "center",
                  headerAlign: "center",
                  filterable: false,
                  renderCell: (params) => (
                    <IconButton
                      color={"error"}
                      onClick={() => deleteItem(params.row.id)}
                    >
                      <DeleteIcon />
                    </IconButton>
                  ),
                },
              ]}
            />
          </div>
        )}
      </div>
    </Box>
  );
};

export default DataSourcesOverview;
