import './App.css';
import {WeatherHeader} from "./WeatherHeader";
import {YearSelector} from "./YearSelector";
import { YearGraph } from "./YearGraph";
import { useState } from "react";
import { Box } from "@mui/material";
import { ErrorBox } from "./ErrorBox";

function App() {
  const [years, setYears] = useState([])
  const [error, setError] = useState("");

  const onYearChange = (selectedYears) => {
    console.log("onYearChange:", selectedYears);
    setYears(selectedYears);
  }

  return (
      <div className="App">
        <header className="App-header">
          <WeatherHeader/>
        </header>
        <ErrorBox message={error}/>
        <YearSelector onError={setError} onChange={onYearChange}/>
        <Box ml={10} mt={5} mb={2}>
          <YearGraph years={years} onError={setError}/>
        </Box>
      </div>
  );
}

export default App;
