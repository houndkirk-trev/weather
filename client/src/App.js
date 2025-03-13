import './App.css';
import {WeatherHeader} from "./WeatherHeader";
import {YearSelector} from "./YearSelector";
import { YearGraph } from "./YearGraph";
import React, { useState } from "react";
import { Box, Stack } from "@mui/material";
import { ErrorBox } from "./ErrorBox";
import { ChartTypeSelector } from "./ChartTypeSelector";
import { isEmpty } from "lodash/lang";

function App() {
  const [years, setYears] = useState([])
  const [error, setError] = useState("");
  const [chartType, setChartType] = useState('');

  const onYearChange = (selectedYears) => {
    setYears(selectedYears);
  }

  const onChartTypeChange = (chartType) => {
    setChartType(chartType);
  }

  return (
      <div className="App">
        <header className="App-header">
          <WeatherHeader/>
        </header>
        <ErrorBox message={error}/>
        <Box ml={5} mt={5} mb={2}>
          <Stack direction='column'>
            <YearSelector onError={setError} onChange={onYearChange}/>
            <ChartTypeSelector show={!isEmpty(years)} onChartTypeChange={onChartTypeChange}/>
            <YearGraph years={years} onError={setError} chartType={chartType}/>
          </Stack>
        </Box>
      </div>
  );
}

export default App;
