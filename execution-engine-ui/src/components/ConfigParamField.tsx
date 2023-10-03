import {
  Autocomplete,
  Checkbox,
  FormControlLabel,
  FormGroup,
  Tooltip,
} from "@mui/material";
import TextField from "@mui/material/TextField";
import * as React from "react";

import { ConfigParams } from "../@types/data-source";

export default function ConfigParamField(props: {
  param: ConfigParams;
  map: Map<string, string | boolean | string[] | undefined>;
  setMap: React.Dispatch<
    React.SetStateAction<Map<string, string | boolean | string[] | undefined>>
  >;
}) {
  const { param, map, setMap } = props;

  function setValueInMap(key: string, value: string | string[] | boolean) {
    const map_: Map<string, string | boolean | string[] | undefined> = new Map(
      map,
    );
    map_.set(key, value);
    setMap(map_);
  }

  if (param.type === "string") {
    return (
      <TextField
        key={param.key}
        value={map.get(param.key)}
        margin="normal"
        id={"field-" + param.key}
        label={
          <Tooltip title={param.description}>
            <div>{param.label}</div>
          </Tooltip>
        }
        type={param.type}
        fullWidth
        variant="outlined"
        onChange={(e) => setValueInMap(param.key, e.target.value)}
      />
    );
  } else if (param.type === "array") {
    return (
      <Autocomplete
        id={"array_" + param.key}
        multiple
        options={[]}
        freeSolo
        value={map.get(param.key) as string[]}
        onChange={(_e, items) => {
          if (items) {
            setValueInMap(param.key, [...items]);
          }
        }}
        renderInput={(params) => <TextField {...params} label="Keywords" />}
      />
    );
  } else if (param.type === "boolean") {
    if (map.get(param.key) === undefined) {
      setValueInMap(param.key, false);
    }
    return (
      <FormGroup>
        <FormControlLabel
          control={
            <Checkbox
              checked={!!map.get(param.key)}
              onClick={() => setValueInMap(param.key, !map.get(param.key))}
            />
          }
          label={
            <Tooltip title={param.description}>
              <div>{param.label}</div>
            </Tooltip>
          }
        />
      </FormGroup>
    );
  }
}
