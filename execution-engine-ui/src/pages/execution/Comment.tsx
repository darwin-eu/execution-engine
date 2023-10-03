import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogTitle from "@mui/material/DialogTitle";
import TextField from "@mui/material/TextField";
import * as React from "react";
import { useContext } from "react";

import { Analysis } from "../../@types/data-source";
import { AppContext, AppContextProps } from "../../context/AppContext.tsx";
import { postComment } from "../../service/SubmissionService.tsx";

export default function Comment(props: {
  analysis: Analysis;
  setOpen: React.Dispatch<React.SetStateAction<boolean>>;
  comment: string;
  setComment: React.Dispatch<React.SetStateAction<string>>;
  open: boolean;
}) {
  const { doAlert, client } = useContext(AppContext) as AppContextProps;
  const { analysis, open, setOpen, comment, setComment } = props;

  function close() {
    setComment("");
    setOpen(false);
  }

  function save() {
    if (client) {
      postComment(analysis.id, comment, client)
        .then(() => close())
        .catch(() => doAlert("error", "Failed to save comment"));
    }
  }

  return (
    <Dialog
      open={open}
      onClose={close}
      fullWidth
      style={{ minWidth: "25vw", minHeight: "25vh" }}
    >
      <DialogTitle>Comment</DialogTitle>
      <DialogContent>
        <TextField
          multiline
          key={"comment"}
          value={comment}
          autoFocus
          margin="normal"
          id="comment"
          label={"Comment"}
          type="string"
          fullWidth
          variant="outlined"
          onChange={(e) => setComment(e.target.value)}
        />{" "}
      </DialogContent>
      <DialogActions
        style={{ justifyContent: "space-between", paddingLeft: "1em" }}
      >
        <Button onClick={close}>Cancel</Button>
        <Button onClick={save}>Save</Button>
      </DialogActions>
    </Dialog>
  );
}
