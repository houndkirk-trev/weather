import React, { useEffect, useState } from "react";
import { CartesianGrid, Label, Legend, Line, LineChart, Tooltip, XAxis, YAxis } from "recharts"
import { isEmpty } from "lodash/lang";
import { remove } from "lodash";
import { Box, CircularProgress } from "@mui/material";
import { fetchWeatherForYears } from "./WeatherLoader";
import { colors } from "./colors";


export const YearGraph = ({years, onError}) => {
  const [isLoading, setLoading] = useState(false);
  const [yearData, setYearData] = useState([]);
  const yearsCopy = years.slice();

  const updateWeatherData = (data) => {
    // Only display the data which corresponds with the selected years.
    const yearDataToDisplay = yearData.filter((yd) => years.includes(yd.year));
    const newData = isEmpty(data) ? yearDataToDisplay : yearDataToDisplay.filter(y => y.year !== data.year).concat([data]);
    setYearData(newData);
    setLoading(false);
  }

  useEffect(() => {
    setLoading(true);
    if (!isEmpty(yearsCopy)) {
      fetchWeatherForYears(yearsCopy, onError, updateWeatherData);
    } else {
      updateWeatherData({});
    }
  }, [years]);

  // From the years passed in, remove those which we already have data for
  // so that we're not refetching the same data all the time.
  yearData.forEach((d) => remove(yearsCopy, (y) => y === d.year));
  console.log("YearGraph: years:", yearsCopy, "; yearData:", yearData);

  return isEmpty(years) ? "" :
    isLoading ? (<Box sx={{width: 800, height:300, backgroundColor: '#cccccc', paddingTop:10}}><CircularProgress /></Box>) : (
    <LineChart
      width={800}
      height={300}
      data={yearData}
    >
      <CartesianGrid strokeDasharray="3 3" />
      <XAxis dataKey="month" allowDuplicatedCategory={false}>
        <Label value='Month' position='insideBottom' offset={-5}/>
      </XAxis>
      <YAxis label={{ value: 'Temperature °C', angle: -90, position: 'insideLeft'}}/>
      <Tooltip />
      <Legend verticalAlign="top" height={35}/>
      {yearData.map((y, index) =>
        <Line name={y.year} type="monotone" dataKey="averageMax" data={y.data} key={y.year} stroke={colors[index % 8]} activeDot={{ r: 8 }} />
        // <Line name={y.year} type="monotone" dataKey="averageMax" data={y.data} key={y.year} stroke="red" activeDot={{ r: 8 }} />
      )}
      {/*<Line name="Average Minimum" type="monotone" dataKey="averageMin"  stroke="#a00000" activeDot={{ r: 8 }} />*/}
    </LineChart>
  );
}
