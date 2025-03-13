import React, { useEffect, useState } from "react";
import { FormControlLabel, FormLabel, Radio, Stack } from "@mui/material";

export const ChartTypeSelector = ({onChartTypeChange, show}) => {
  const [chartType, setChartType] = useState('averageMax');

  useEffect(() => {
    onChartTypeChange(chartType);
  });

  const handleChartTypeChange = (event) => {
    setChartType(event.target.value);
    onChartTypeChange(chartType);
  }

  if (!show) {
    return "";
  }

  return (
    <Stack direction='row' spacing={1} sx={{paddingBottom: 2}}>
      <FormLabel id="chart-type-label" sx={{paddingTop: 1, paddingLeft: 1.5}}>Chart Type:</FormLabel>
      <Stack direction='column' spacing={-1}>
        {/* Average max & min*/}
        <FormControlLabel
          control={<Radio />}
          checked={chartType === 'averageMax'}
          onChange={handleChartTypeChange}
          value='averageMax'
          label='Average Maximum' />

        <FormControlLabel
          control={<Radio />}
          checked={chartType === 'averageMin'}
          onChange={handleChartTypeChange}
          value='averageMin'
          label='Average Minimum' />
      </Stack>

      {/* Max & min*/}
      <Stack direction='column' spacing={-1}>
        <FormControlLabel
          control={<Radio />}
          checked={chartType === 'max'}
          onChange={handleChartTypeChange}
          value='max'
          label='Maximum' />

        <FormControlLabel
          control={<Radio />}
          checked={chartType === 'min'}
          onChange={handleChartTypeChange}
          value='min'
          label='Minimum' />
      </Stack>
    </Stack>
  );
  }
