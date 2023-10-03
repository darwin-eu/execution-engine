import AddCommentOutlinedIcon from "@mui/icons-material/AddCommentOutlined";
import ArticleIcon from "@mui/icons-material/Article";
import CancelIcon from "@mui/icons-material/Cancel";
import CloseIcon from "@mui/icons-material/Close";
import CommentIcon from "@mui/icons-material/Comment";
import DeleteIcon from "@mui/icons-material/Delete";
import DoneIcon from "@mui/icons-material/Done";
import DownloadIcon from "@mui/icons-material/Download";
import LoopIcon from "@mui/icons-material/Loop";
import { CircularProgress, Tooltip } from "@mui/material";
import Box from "@mui/material/Box";
import IconButton from "@mui/material/IconButton";
import { DataGrid } from "@mui/x-data-grid";
import { useContext, useEffect, useState } from "react";

import { Analysis } from "../../@types/data-source";
import "../../App.css";
import Rerun from "../../components/Rerun.tsx";
import { AppContext, AppContextProps } from "../../context/AppContext";
import {
  SocketContext,
  SocketContextProps,
} from "../../context/SocketContext.tsx";
import {
  cancel,
  downloadSubmissionResults,
  fetchSubmissionLogs,
  fetchSubmissions,
} from "../../service/SubmissionService";
import Comment from "./Comment.tsx";
import CreateSubmission from "./CreateSubmission";
import Logs from "./Logs.tsx";

export default function Submissions() {
  const { client, doAlert } = useContext(AppContext) as AppContextProps;
  const { updatedAnalysis } = useContext(SocketContext) as SocketContextProps;
  const [rows, setRows] = useState<Analysis[]>([]);
  const [loading, setLoading] = useState(false);
  const [logsOpen, setLogsOpen] = useState(false);
  const [rerunOpen, setRerunOpen] = useState(false);
  const [commentOpen, setCommentOpen] = useState(false);
  const [comment, setComment] = useState("");
  const [selectedAnalysis, setSelectedAnalysis] = useState<
    Analysis | undefined
  >(undefined);

  useEffect(() => {
    if (client) {
      setLoading(true);
      fetchSubmissions(client)
        .then((r) => {
          setRows(r);
          setLoading(false);
        })
        .catch(() => setLoading(false));
    }
  }, []);

  useEffect(() => {
    if (updatedAnalysis) {
      const updatedRows = rows.filter((r) => r.id !== updatedAnalysis?.id);
      updatedRows.push(updatedAnalysis);
      setRows(updatedRows);
      if (selectedAnalysis?.id === updatedAnalysis.id) {
        setSelectedAnalysis(updatedAnalysis);
      }
    }
  }, [updatedAnalysis]);

  function downloadResults(id: number) {
    if (client) {
      downloadSubmissionResults(id, client)
        .then(() => {})
        .catch(() => {
          doAlert("error", "Failed to download results");
        });
    }
  }

  function openLogsModal(analysis: Analysis) {
    if (client) {
      fetchSubmissionLogs(analysis.id, client)
        .then((analysisWithLogs) => {
          const updatedRows = rows.filter((r) => r.id !== analysisWithLogs?.id);
          updatedRows.push(analysisWithLogs);
          setRows(updatedRows);
          setSelectedAnalysis(analysisWithLogs);
          setLogsOpen(true);
        })
        .catch(() => doAlert("error", "Failed to retrieve logs"));
    }
  }

  function openRerun(analysis: Analysis) {
    setSelectedAnalysis(analysis);
    setRerunOpen(true);
  }

  function cancelSubmission(id: number) {
    if (client) {
      cancel(id, client)
        .then(() => {})
        .catch(() => "Something went wrong it is not your fault");
    }
  }

  function openCommentModal(analysis: Analysis) {
    setSelectedAnalysis(analysis);
    setComment(analysis.comment);
    setCommentOpen(true);
  }

  function durationSeconds(analysis: Analysis) {
    const start = new Date(analysis.created).getTime();
    const end = analysis.finished
      ? new Date(analysis.finished).getTime()
      : new Date().getTime();
    return (end - start) / 1000;
  }

  function durationDisplay(analysis: Analysis) {
    const seconds = durationSeconds(analysis);
    // 2 mins
    if (seconds < 120) {
      return Math.round(seconds) + " seconds";
      // 10 hours
    } else if (seconds < 36000) {
      return Math.round(seconds / 60) + " minutes";
    } else {
      return Math.round(seconds / 60 / 24) + " hours";
    }
  }

  function statusIcon(status: string) {
    if (status === "EXECUTED") {
      return <DoneIcon color={"success"} />;
    } else if (status === "FAILED" || status === "CANCELLED") {
      return <CloseIcon color={"error"} />;
    } else {
      return (
        <CircularProgress
          size={20}
          disableShrink
          style={{ animationDuration: "3s" }}
        />
      );
    }
  }

  return (
    <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
      <div>
        <div className={"Buttons-wrapper__new"}>
          <CreateSubmission />
        </div>
        <div className={"DataGrids-wrapper"}>
          <DataGrid
            initialState={{
              columns: {
                columnVisibilityModel: {
                  id: false,
                  study: true,
                  studyId: false,
                  dataSource: true,
                  created: true,
                  finished: false,
                  delete: false,
                },
              },
              sorting: {
                sortModel: [{ field: "created", sort: "desc" }],
              },
            }}
            rows={rows}
            columns={[
              {
                width: 80,
                field: "status",
                type: "string",
                align: "center",
                headerName: "",
                flex: 1,
                renderCell: (params) => (
                  <div className={"Icons-wrapper__align"}>
                    <Tooltip
                      id="button-cancel"
                      title={params.row.status}
                      placement={"left"}
                    >
                      {statusIcon(params.row.status)}
                    </Tooltip>
                  </div>
                ),
              },
              {
                field: "id",
                headerName: "id",
                type: "number",
                flex: 0.5,
                align: "center",
                headerAlign: "center",
              },
              {
                field: "studyTitle",
                headerName: "study",
                type: "string",
                flex: 2,
              },
              {
                field: "studyId",
                headerName: "study id",
                type: "string",
                flex: 1,
              },
              {
                field: "dataSource",
                headerName: "data source",
                type: "string",
                flex: 2,
              },
              {
                field: "created",
                headerName: "started",
                headerAlign: "center",
                align: "center",
                type: "dateTime",
                flex: 2,
                valueGetter: (params) => new Date(params.row.created),
              },
              {
                field: "finished",
                headerName: "finished",
                headerAlign: "center",
                align: "center",
                type: "dateTime",
                flex: 2,
                valueGetter: (params) => new Date(params.row.finished),
              },
              {
                field: "duration",
                headerName: "duration",
                type: "number",
                align: "center",
                headerAlign: "center",
                flex: 2,
                valueGetter: (params) => durationSeconds(params.row),
                renderCell: (params) => (
                  <div>{durationDisplay(params.row)}</div>
                ),
              },
              {
                width: 80,
                field: "download",
                flex: 1,
                headerName: "download",
                sortable: false,
                align: "center",
                headerAlign: "center",
                filterable: false,
                renderCell: (params) => (
                  <IconButton onClick={() => downloadResults(params.row.id)}>
                    <DownloadIcon color={"primary"} />
                  </IconButton>
                ),
              },
              {
                width: 80,
                field: "logs",
                flex: 1,
                headerName: "logs",
                sortable: false,
                align: "center",
                headerAlign: "center",
                filterable: false,
                renderCell: (params) => (
                  <IconButton onClick={() => openLogsModal(params.row)}>
                    <ArticleIcon color={"primary"} />
                  </IconButton>
                ),
              },
              {
                field: "comment",
                headerName: "comment",
                type: "boolean",
                flex: 1,
                align: "center",
                headerAlign: "center",
                minWidth: 80,
                renderCell: (params) => {
                  return (
                    <IconButton onClick={() => openCommentModal(params.row)}>
                      {params.row.comment ? (
                        <CommentIcon color={"primary"} />
                      ) : (
                        <AddCommentOutlinedIcon color={"primary"} />
                      )}
                    </IconButton>
                  );
                },
              },
              {
                width: 80,
                flex: 1,
                field: "rerun",
                headerName: "",
                sortable: false,
                align: "center",
                headerAlign: "center",
                filterable: false,
                renderCell: (params) => {
                  if (params.row.status === "EXECUTING") {
                    return (
                      <IconButton
                        onClick={() => cancelSubmission(params.row.id)}
                      >
                        <Tooltip
                          id="button-cancel"
                          title="cancel"
                          placement={"left"}
                        >
                          <CancelIcon color={"error"} />
                        </Tooltip>
                      </IconButton>
                    );
                  } else if (params.row.status !== "CREATED") {
                    return (
                      <IconButton onClick={() => openRerun(params.row)}>
                        <Tooltip
                          id="button-rerun"
                          title="rerun"
                          placement={"left"}
                        >
                          <LoopIcon color={"primary"} />
                        </Tooltip>
                      </IconButton>
                    );
                  } else {
                    return <div />;
                  }
                },
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
                renderCell: () => (
                  <IconButton disabled>
                    <DeleteIcon color={"disabled"} />
                  </IconButton>
                ),
              },
            ]}
            loading={loading}
          />
        </div>
      </div>
      {selectedAnalysis && (
        <div>
          <Logs
            analysis={selectedAnalysis}
            setOpen={setLogsOpen}
            open={logsOpen}
          />
          <Comment
            analysis={selectedAnalysis}
            setOpen={setCommentOpen}
            open={commentOpen}
            comment={comment}
            setComment={setComment}
          />
        </div>
      )}
      {selectedAnalysis && (
        <Rerun
          configs={selectedAnalysis}
          id={selectedAnalysis.id}
          open={rerunOpen}
          setOpen={setRerunOpen}
          isLib={false}
        />
      )}
    </Box>
  );
}
