import { CircularProgress, Grid } from "@mui/material";
import Dialog from "@mui/material/Dialog";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import * as React from "react";

import { Analysis } from "../../@types/data-source";

export default function Logs(props: {
  analysis: Analysis;
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
  open: boolean;
}) {
  const { analysis, open, setOpen } = props;

  function close() {
    setOpen(false);
  }

  return (
    <Dialog fullWidth maxWidth={"md"} open={open} onClose={close}>
      <DialogTitle>
        <div style={{ display: "flex", justifyContent: "space-between" }}>
          <div>Logs for {analysis.studyId}</div>
          {analysis.status === "EXECUTING" && (
            <CircularProgress
              size={20}
              disableShrink
              style={{ animationDuration: "3s" }}
            />
          )}
        </div>
      </DialogTitle>
      <DialogContent style={{ whiteSpace: "pre-wrap" }}>
        {analysis.logs
          ?.sort((a, b) => a.date - b.date)
          .map((line) => {
            return (
              <Grid container spacing={0} key={line.date}>
                <Grid item xs={3}>
                  {new Date(line.date).toLocaleString()}
                </Grid>
                <Grid item xs={9}>
                  {line.line}
                </Grid>
              </Grid>
            );
          })}
      </DialogContent>
    </Dialog>
  );
}
