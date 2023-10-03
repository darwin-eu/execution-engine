import { Autocomplete } from "@mui/material";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import TextField from "@mui/material/TextField";
import { useContext, useEffect, useState } from "react";
import * as React from "react";

import { ConfigParams, DataSource, Submission } from "../@types/data-source";
import { AppContext, AppContextProps } from "../context/AppContext";
import { fetchDataSources } from "../service/DataSourceService";
import { rerun } from "../service/SubmissionService";
import ConfigParamField from "./ConfigParamField.tsx";

export default function Rerun(props: {
  configs: Submission;
  open: boolean;
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
  id: number;
  isLib?: boolean;
}) {
  const { id, isLib, configs, open, setOpen } = props;
  const { doAlert, client } = useContext(AppContext) as AppContextProps;
  const [sources, setSources] = useState<DataSource[]>([]);
  const [sourceId, setSourceId] = useState<number>(0);
  const [studyId, setStudyId] = useState("");
  const [studyTitle, setStudyTitle] = useState("");
  const [entrypoint, setEntrypoint] = useState("");
  const [engine, setEngine] = useState("");
  const [params, setParams] = useState<ConfigParams[]>([]);
  const [keyValueMap, setKeyValueMap] = useState(
    new Map<string, string | boolean | string[] | undefined>(),
  );

  useEffect(() => {
    if (client) {
      fetchDataSources(client)
        .then((r) => {
          setSources(r);
        })
        .catch(() => doAlert("error", "Failed to fetch data sources"));
    }
  }, []);

  useEffect(() => {
    if (configs?.studyId) {
      setStudyId(configs.studyId);
    }
    if (configs?.studyTitle) {
      setStudyTitle(configs.studyTitle);
    }
    if (configs?.entrypoint) {
      setEntrypoint(configs.entrypoint);
    }
    if (configs?.engine) {
      setEngine(configs.engine);
    }
    if (configs?.params) {
      setParams(configs.params);
      params?.forEach((p) => {
        if (p.type.toLowerCase() === "boolean") {
          const booleanValue = p.value == "false" ? false : !!p.value;
          keyValueMap.set(p.key, booleanValue);
        } else {
          keyValueMap.set(p.key, p.value);
        }
        setKeyValueMap(keyValueMap);
      });
    }
  }, [configs]);

  function handleClose() {
    setOpen(false);
  }

  function uploadData() {
    if (client) {
      if (!sourceId || sourceId === 0) {
        doAlert(
          "error",
          "Please select a data source on which to execute the study",
        );
      } else {
        const s = {
          studyTitle: studyTitle,
          studyId: studyId,
          entrypoint: entrypoint,
          engine: engine,
          datasourceId: sourceId.toString(),
          params: params.map((p) => {
            return { ...p, value: keyValueMap.get(p.key) };
          }),
        };
        rerun(id, s, !!isLib, client)
          .then(() => {
            doAlert(
              "success",
              "Study was successfully submitted for execution",
            );
            setOpen(false);
          })
          .catch((err) => {
            doAlert("error", err.response.data.message);
          });
      }
    }
  }

  return (
    <div>
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>Run a study</DialogTitle>
        <DialogContent>
          <Autocomplete
            getOptionLabel={(option) => option.name ?? "unknown"}
            options={sources}
            onChange={(_e, value) => {
              if (value && value.id) {
                setSourceId(value.id);
              }
            }}
            renderInput={(params) => (
              <TextField {...params} label="Datasource" />
            )}
          />
          <TextField
            disabled={configs && configs.studyId !== ""}
            key={"studyId"}
            value={studyId}
            autoFocus
            margin="normal"
            id="studyId"
            label={"Study ID"}
            type="string"
            fullWidth
            variant="outlined"
            onChange={(e) => setStudyId(e.target.value)}
          />
          <TextField
            disabled={configs && configs.studyTitle !== ""}
            key={"studyTitle"}
            value={studyTitle}
            autoFocus
            margin="normal"
            id="description"
            label={"Study Title"}
            type="string"
            fullWidth
            variant="outlined"
            onChange={(e) => setStudyTitle(e.target.value)}
          />
          <TextField
            disabled={configs && configs.entrypoint !== undefined}
            key={"entrypoint"}
            value={entrypoint}
            autoFocus
            margin="normal"
            id="description"
            label={"Entrypoint"}
            type="string"
            fullWidth
            variant="outlined"
            onChange={(e) => setEntrypoint(e.target.value)}
          />
          <TextField
            disabled={configs && configs.engine !== undefined}
            key={"engine"}
            value={engine}
            autoFocus
            margin="normal"
            id="engine"
            label={"Execution environment"}
            type="string"
            fullWidth
            variant="outlined"
            onChange={(e) => setEngine(e.target.value)}
          />
          {params?.map((param) => {
            return (
              <ConfigParamField
                key={param.key}
                param={param}
                map={keyValueMap}
                setMap={setKeyValueMap}
              />
            );
          })}
        </DialogContent>
        <DialogActions
          style={{ justifyContent: "space-between", paddingLeft: "1em" }}
        >
          <Button onClick={handleClose}>Cancel</Button>
          <Button onClick={uploadData}>Submit</Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}
