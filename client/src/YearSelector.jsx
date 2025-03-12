import { useEffect, useState } from "react";
import { isEmpty } from "lodash/lang";
import {
  Box,
  Chip,
  CircularProgress,
  Input,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Typography
} from "@mui/material";
import { fetchAvailableYears } from "./WeatherLoader";

export const YearSelector = ({onError, onChange}) => {
  const [isLoading, setLoading] = useState(false);
  const [yearsAvailable, setYearsAvailable] = useState([]);
  const [selectedYears, setSelectedYears] = useState([]);
  const [isMenuOpen, setMenuOpen] = useState(false);

  const handleError = (message) => {
    setLoading(false);
    onError(message);
  }
  const yearsFetched = (years) => {
    setLoading(false);
    setYearsAvailable(years);
  }

  useEffect(() => {
    setLoading(true);
    fetchAvailableYears(handleError, yearsFetched);
  }, []);

  const handleYearSelectionChange = (event) => {
    const years = event.target.value;
    setMenuOpen(false);
    setSelectedYears(years);
    onChange(years);
  }

  const handleOnClick = () => {
    setMenuOpen(!isMenuOpen);
  }

  return isEmpty(yearsAvailable) ? "" :
    isLoading ? (<Stack direction='row' sx={{marginLeft: '20px'}}>
      <Typography variant='body' color='primary'>Loading available years</Typography>
      <CircularProgress size='20px' sx={{marginLeft: '10px'}}/>
    </Stack>) : (
    <Box sx={{ display: 'flex', width: '50vw', gap: 1.0 }}>
      <InputLabel id="select-year-label" sx={{marginTop: '10px', marginLeft: '1vw'}}>
        Select Year(s)
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
    </Box>
  );
}
