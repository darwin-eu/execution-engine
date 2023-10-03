import { Alert, AlertColor, Snackbar } from "@mui/material";

interface AlertProps {
  open: boolean | undefined;
  onClose: (() => void) | undefined;
  message: string | undefined;
  severity: AlertColor | undefined;
}

export default function AlertMessage(props: AlertProps) {
  const { open, onClose, message, severity } = props;

  return (
    <div>
      <Snackbar
        open={open}
        autoHideDuration={10000}
        onClose={onClose}
        anchorOrigin={{ vertical: "top", horizontal: "right" }}
      >
        <Alert onClose={onClose} severity={severity} sx={{ width: "100%" }}>
          {message}
        </Alert>
      </Snackbar>
    </div>
  );
}
