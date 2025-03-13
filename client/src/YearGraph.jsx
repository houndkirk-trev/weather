import React, { useEffect, useState } from "react";
import { CartesianGrid, Label, Legend, Line, LineChart, Tooltip, XAxis, YAxis } from "recharts"
import { isEmpty } from "lodash/lang";
import { remove } from "lodash";
import { Box, CircularProgress, Stack, Typography } from "@mui/material";
import { fetchWeatherForYears } from "./WeatherLoader";
import { colors } from "./colors";


export const YearGraph = ({years, onError, chartType}) => {
  const [isLoading, setLoading] = useState(false);
  const [yearData, setYearData] = useState([]);

  const yearsNeeded = years.slice();

  const updateWeatherData = (data) => {
    // Only display the data which corresponds with the selected years.
    const yearDataToDisplay = yearData.filter((yd) => years.includes(yd.year));
    const newData = isEmpty(data) ? yearDataToDisplay : yearDataToDisplay.filter(y => y.year !== data.year).concat([data]);
    setYearData(newData);
    setLoading(false);
  }

  const handleError = (message) => {
    setLoading(false);
    onError(message);
  }

  useEffect(() => {
    setLoading(true);
    if (!isEmpty(yearsNeeded)) {
      fetchWeatherForYears(yearsNeeded, handleError, updateWeatherData);
    } else {
      updateWeatherData({});
    }
  }, [years]);

  // From the years passed in, remove those which we already have data for
  // so that we're not refetching the same data all the time.
  yearData.forEach((d) => remove(yearsNeeded, (y) => y === d.year));
  console.log("YearGraph: yearsNeeded:", yearsNeeded, "; yearData:", yearData);

  if (isEmpty(years)) {
    return "";
  }

  if (isLoading) {
    return (
      <Box sx={{width: 400, height: 100, backgroundColor: '#cccccc', paddingTop:10}}>
        <Typography variant='body' color='primary'>Loading...</Typography>
        <CircularProgress size='20px' sx={{marginLeft: '10px'}}/>
      </Box>
    );
  }

  return (
    <Stack direction='column' spacing={1}>
      <LineChart width={500} height={300} data={yearData} >
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="month" allowDuplicatedCategory={false}>
          <Label value='Month' position='insideBottom' offset={-5}/>
        </XAxis>
        <YAxis tickCount='6' label={{ value: 'Temperature °C', angle: -90, position: 'insideLeft'}}/>
        <Tooltip />
        <Legend verticalAlign="top" height={35}/>

        {yearData.map((y, index) =>
          <Line
            name={y.year}
            type="monotone"
            dataKey={chartType}
            data={y.data}
            key={y.year}
            stroke={colors[index % 8]}
            activeDot={{ r: 8 }}
          />
        )}
      </LineChart>
    </Stack>
  );
}
