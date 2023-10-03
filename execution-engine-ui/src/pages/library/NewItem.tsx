import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import TextField from "@mui/material/TextField";
import JSZip, { JSZipObject } from "jszip";
import * as React from "react";
import { ChangeEvent, useContext, useState } from "react";
import YAML from "yaml";

import { Submission } from "../../@types/data-source";
import { AppContext, AppContextProps } from "../../context/AppContext";
import { upload } from "../../service/LibraryService.tsx";

export default function New(props: {
  open: boolean;
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
}) {
  const { client, doAlert } = useContext(AppContext) as AppContextProps;
  const { setOpen, open } = props;
  const [filename, setFilename] = useState<string | undefined>(undefined);
  const [configs, setConfigs] = useState<Submission | undefined>(undefined);
  const [currentFile, setCurrentFile] = useState<File>();
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  function handleClose() {
    setOpen(false);
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

  function uploadData() {
    if (client) {
      if (!currentFile) {
        doAlert("error", "please upload a .zip file with the study in it");
      } else {
        upload(
          currentFile,
          {
            id: 0,
            name: name,
            description: description,
            engine: configs?.engine,
            entrypoint: configs?.entrypoint,
            params: configs?.params,
            created: new Date(),
          },
          client,
        )
          .then(() => {
            setOpen(false);
          })
          .catch((err: { response: { data: { message: string } } }) => {
            doAlert("error", err.response.data.message);
          });
      }
    }
  }

  return (
    <div>
      <div>
        <Dialog open={open} onClose={handleClose}>
          <DialogTitle>{"Add a new item to library"}</DialogTitle>
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
            <TextField
              value={name}
              autoFocus
              margin="normal"
              id="name"
              label="Name"
              type="string"
              fullWidth
              variant="outlined"
              onChange={(e) => setName(e.target.value)}
            />
            <TextField
              value={description}
              margin="normal"
              id="description"
              label="Description"
              type="string"
              fullWidth
              variant="outlined"
              onChange={(e) => setDescription(e.target.value)}
            />
          </DialogContent>
          <DialogActions
            style={{ justifyContent: "space-between", paddingLeft: "1em" }}
          >
            <Button onClick={handleClose}>Close</Button>
            <Button onClick={uploadData}>Save</Button>
          </DialogActions>
        </Dialog>
      </div>
    </div>
  );
}
