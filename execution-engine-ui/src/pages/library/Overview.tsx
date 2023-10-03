import CodeIcon from "@mui/icons-material/Code";
import DeleteIcon from "@mui/icons-material/Delete";
import Box from "@mui/material/Box";
import Button from "@mui/material/Button";
import IconButton from "@mui/material/IconButton";
import { DataGrid } from "@mui/x-data-grid";
import { useContext, useEffect, useState } from "react";

import { LibraryItem, Submission } from "../../@types/data-source";
import "../../App.css";
import Rerun from "../../components/Rerun.tsx";
import { AppContext, AppContextProps } from "../../context/AppContext.tsx";
import {
  deleteLibraryItem,
  fetchLibraryItems,
} from "../../service/LibraryService.tsx";
import New from "./NewItem.tsx";

export const LibraryOverview = () => {
  const { client, doAlert } = useContext(AppContext) as AppContextProps;
  const [addingItem, setAddingItem] = useState(false);
  const [rows, setRows] = useState<LibraryItem[]>([]);
  const [runOpen, setRunOpen] = useState(false);
  const [id, setId] = useState(0);
  const [selectedForRun, setSelectedForRun] = useState<Submission | undefined>(
    undefined,
  );

  useEffect(() => {
    updateItems();
  }, [addingItem]);

  function updateItems() {
    if (client) {
      fetchLibraryItems(client)
        .then((r) => {
          setRows(r);
        })
        .catch(() => doAlert("error", "Failed to connect to library"));
    }
  }

  function deleteItem(id: number) {
    if (client) {
      deleteLibraryItem(id, client)
        .then(() => updateItems())
        .catch((err) => doAlert("error", err.response.data.message));
    }
  }

  function run(item: LibraryItem) {
    setId(item.id);
    setSelectedForRun({
      studyId: "",
      studyTitle: "",
      datasourceId: "",
      entrypoint: item.entrypoint ?? "",
      params: item.params,
      engine: item.engine ?? "",
    });
    setRunOpen(true);
  }

  return (
    <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
      <div>
        <div className={"Buttons-wrapper__new"}>
          <Button
            variant="contained"
            className={"Button__new"}
            size="small"
            onClick={() => setAddingItem(true)}
          >
            NEW ITEM
          </Button>
          <New setOpen={setAddingItem} open={addingItem} />
        </div>
        <div className={"DataGrids-wrapper"}>
          <DataGrid
            initialState={{
              sorting: {
                sortModel: [{ field: "created", sort: "desc" }],
              },
            }}
            rows={rows}
            columns={[
              {
                field: "id",
                headerName: "id",
                type: "number",
                flex: 0.5,
                align: "center",
                headerAlign: "center",
              },
              {
                field: "name",
                headerName: "name",
                type: "string",
                flex: 2,
              },
              {
                field: "description",
                headerName: "description",
                type: "string",
                flex: 4,
              },
              {
                field: "created",
                headerName: "added",
                headerAlign: "center",
                align: "center",
                type: "dateTime",
                flex: 2,
                valueGetter: (params) => new Date(params.row.created),
              },
              {
                width: 80,
                field: "execute",
                flex: 1,
                headerName: "run code",
                sortable: false,
                align: "center",
                filterable: false,
                headerAlign: "center",
                renderCell: (params) => (
                  <IconButton onClick={() => run(params.row)}>
                    <CodeIcon color={"primary"} />
                  </IconButton>
                ),
              },
              {
                width: 80,
                field: "delete",
                flex: 1,
                headerName: "delete",
                sortable: false,
                align: "center",
                filterable: false,
                headerAlign: "center",
                renderCell: (params) => (
                  <IconButton onClick={() => deleteItem(params.row.id)}>
                    <DeleteIcon color={"error"} />
                  </IconButton>
                ),
              },
            ]}
          />
        </div>
      </div>
      {selectedForRun && (
        <Rerun
          configs={selectedForRun}
          open={runOpen}
          setOpen={setRunOpen}
          id={id}
          isLib
        />
      )}
    </Box>
  );
};
