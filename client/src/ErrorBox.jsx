import React from "react";
import { Stack, Typography } from "@mui/material";
import ReportGmailerrorredIcon from '@mui/icons-material/ReportGmailerrorred';

export const ErrorBox = ({message}) => {
  if (!message)
    return "";

  console.log("ErrorBox: message: ", message)
  return (
    <Stack direction='row' spacing={1}>
      <ReportGmailerrorredIcon color='error'/>
      <Typography
        id="error"
        textAlign="middle"
        variant="body"
        color="error"
        sx={{paddingTop: '3px'}}>
        {message}
      </Typography>
    </Stack>
  );
}
