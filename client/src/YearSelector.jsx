import { useEffect, useState } from "react";
import { isEmpty } from "lodash/lang";
import {
  Box,
  Chip,
  CircularProgress,
  Input,
  InputLabel,
  MenuItem,
  Select
} from "@mui/material";
import RefreshIcon from '@mui/icons-material/Refresh';
import { fetchAvailableYears } from "./WeatherLoader";

export const YearSelector = ({onError, onChange}) => {
  const [isLoading, setLoading] = useState(false);
  const [yearsAvailable, setYearsAvailable] = useState([]);
  const [selectedYears, setSelectedYears] = useState([]);
  const [isMenuOpen, setMenuOpen] = useState(false);

  useEffect(() => {
    refreshYearData();
  }, []);

  const handleError = (message) => {
    setLoading(false);
    onError(message);
  }
  const yearsFetched = (years) => {
    setLoading(false);
    setYearsAvailable(years);
  }

  const refreshYearData = () => {
    onError("");
    setLoading(true);
    fetchAvailableYears(handleError, yearsFetched);
  }

  const handleYearSelectionChange = (event) => {
    const years = event.target.value;
    setMenuOpen(false);
    setSelectedYears(years)
    onChange(years);
  }

  const handleOnClick = () => {
    setMenuOpen(!isMenuOpen);
  }


  if (isEmpty(yearsAvailable)) {
    return "";
  }

  return (
    <Box sx={{ display: 'flex', width: '50vw', gap: 1.0, marginBottom: 3 }}>
      <InputLabel id="select-year-label" sx={{marginTop: '10px', marginLeft: '1vw'}}>
        Select Year(s):
      </InputLabel>
      <Select
        labelId="select-year-label"
        id="select-year"
        value={selectedYears}
        open={isMenuOpen}
        multiple
        compact="true"
        onChange={handleYearSelectionChange}
        onClick={handleOnClick}
        input={<Input id="select-multiple-chip" label="Chip" sx={{minWidth: '200px'}}/>}
        renderValue={(selected) =>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
            {selected.map((year) => (
              <Chip key={year} label={year}/>
            ))}
          </Box>
        }
      >
        {yearsAvailable.map((year, i) =>
          <MenuItem key={i} value={year}>{year}</MenuItem>
        )}
      </Select>
      <Box paddingTop='10px'>
        {isLoading && <CircularProgress size="20px" />}
        {!isLoading && <RefreshIcon onClick={refreshYearData} />}
      </Box>
    </Box>
  );
}
