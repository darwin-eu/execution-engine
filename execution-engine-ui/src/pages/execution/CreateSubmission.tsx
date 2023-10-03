import { Autocomplete } from "@mui/material";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import TextField from "@mui/material/TextField";
import JSZip, { JSZipObject } from "jszip";
import * as React from "react";
import { ChangeEvent, useContext, useEffect, useState } from "react";
import YAML from "yaml";

import { ConfigParams, DataSource, Submission } from "../../@types/data-source";
import ConfigParamField from "../../components/ConfigParamField.tsx";
import { AppContext, AppContextProps } from "../../context/AppContext";
import { fetchDataSources } from "../../service/DataSourceService";
import { upload } from "../../service/SubmissionService";

export default function CreateSubmission() {
  const { doAlert, client } = useContext(AppContext) as AppContextProps;
  const [creatConceptSetOpen, setCreatConceptSetOpen] = useState(false);
  const [filename, setFilename] = useState<string | undefined>(undefined);
  const [sources, setSources] = useState<DataSource[]>([]);
  const [sourceId, setSourceId] = useState<number>(0);
  const [configs, setConfigs] = useState<Submission | undefined>(undefined);
  const [currentFile, setCurrentFile] = useState<File>();
  const [studyId, setStudyId] = useState<string>("");
  const [studyTitle, setStudyTitle] = useState<string>("");
  const [entrypoint, setEntrypoint] = useState<string>("");
  const [engine, setEngine] = useState<string>("");
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
    setConfigs(undefined);
    setCurrentFile(undefined);
    setFilename(undefined);
    setStudyId("");
    setStudyTitle("");
    setEntrypoint("");
    setEngine("");
    setParams([]);
    setKeyValueMap(new Map());
  }, [creatConceptSetOpen]);

  useEffect(() => {
    if (configs) {
      setStudyId(configs.studyId);
      setStudyTitle(configs.studyTitle);
      setEntrypoint(configs.entrypoint);
      setEngine(configs.engine);
      if (configs.params) {
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
    } else {
      setConfigs(undefined);
    }
  }, [configs]);

  function handleClose() {
    setCreatConceptSetOpen(false);
  }

  function uploadData() {
    if (client) {
      if (!sourceId || sourceId === 0) {
        doAlert(
          "error",
          "Please select a data source on which to execute the study",
        );
      } else if (!currentFile) {
        doAlert("error", "please upload a .zip file with the study in it");
      } else {
        upload(
          currentFile,
          {
            studyTitle: studyTitle,
            studyId: studyId,
            entrypoint: entrypoint,
            engine: engine,
            datasourceId: sourceId.toString(),
            params: params.map((p) => {
              return { ...p, value: keyValueMap.get(p.key) };
            }),
          },
          client,
        )
          .then(() => {
            doAlert(
              "success",
              "Study was successfully submitted for execution",
            );
            setCreatConceptSetOpen(false);
          })
          .catch((err) => {
            doAlert("error", err.response.data.message);
          });
      }
    }
  }

  const selectFile = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { files } = event.target;
    const selectedFiles = files as FileList;
    setCurrentFile(selectedFiles?.[0]);
    readConfigs(event);
  };

  const readConfigs = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      const { name } = file;
      setFilename(name);
      const reader = new FileReader();
      reader.onload = (evt) => {
        if (!evt?.target?.result) {
          return;
        }
        try {
          const zipFile = evt.target.result;
          const jszip = new JSZip();
          jszip
            .loadAsync(zipFile)
            .then((r: JSZip) => {
              r.forEach((p: string, f: JSZipObject) => {
                if (
                  p.includes("execution-config.yml") &&
                  !p.includes("_execution-config.yml")
                ) {
                  f.async("string")
                    .then((str: string) => {
                      const yml = YAML.parse(str) as Submission;
                      setConfigs(yml);
                    })
                    .catch((err) => {
                      throw err;
                    });
                }
              });
            })
            .catch((err) => {
              throw err;
            });
        } catch (e) {
          doAlert("error", "Selected file could not be processed");
          setFilename(undefined);
        }
      };
      reader.readAsBinaryString(file);
    }
  };

  return (
    <div>
      <Button
        variant="contained"
        size="small"
        className={"Button__new"}
        onClick={() => setCreatConceptSetOpen(true)}
      >
        NEW SUBMISSION
      </Button>
      <div>
        <Dialog open={creatConceptSetOpen} onClose={handleClose}>
          <DialogTitle>Submit a study for execution</DialogTitle>
          <DialogContent>
            <div
              style={{
                display: "flex",
                paddingTop: "1em",
                paddingBottom: "1em",
                width: "100%",
              }}
            >
              <Button
                variant={"outlined"}
                component="label"
                style={{ justifyContent: "space-between" }}
              >
                Select {filename && " other "} File
                <input type="file" hidden onChange={selectFile} />
              </Button>
              {filename && (
                <div style={{ justifyContent: "flex-end", margin: "auto" }}>
                  <b>{filename}</b>
                </div>
              )}
            </div>
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
            {filename && (
              <div>
                <TextField
                  disabled={configs && configs.studyId !== undefined}
                  key={"studyId"}
                  value={studyId}
                  margin="normal"
                  id="studyId"
                  label={"Study ID"}
                  type="string"
                  fullWidth
                  variant="outlined"
                  onChange={(e) => setStudyId(e.target.value)}
                />
                <TextField
                  disabled={configs && configs.studyTitle !== undefined}
                  key={"studyTitle"}
                  value={studyTitle}
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
              </div>
            )}
          </DialogContent>
          <DialogActions
            style={{ justifyContent: "space-between", paddingLeft: "1em" }}
          >
            <Button onClick={handleClose}>Cancel</Button>
            <Button onClick={uploadData}>Submit</Button>
          </DialogActions>
        </Dialog>
      </div>
    </div>
  );
}
