import { isEmpty } from "lodash/lang";
import { first, isNumber, remove } from "lodash";

const loadingYears = [];
const isLoadingYear = (year) => loadingYears.includes(year);
const setLoadingYear = (year, loading) => {
  if (loading && !isLoadingYear(year))
    loadingYears.push(year);
  else if (!loading)
    remove(loadingYears, (y) => y === year);
}

export const isLoading = () => { const result = !isEmpty(loadingYears); console.log("isLoading: ", result); return result;}

const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

const updateWeatherData = (data) => {
  const year = first(data).year;
  data.sort((a, b) => a.month > b.month);
  data.map((d) => {
    if (isNumber(d.month))
      d.month = months[d.month];
    return d;
  });
  data = {
    'year': year,
    'data': data
  };
  console.log("Processed weather data for year:", year);
  return data;
}

const fetchWeatherForYear = (year, onError, onSuccess) => {
  if (isLoadingYear(year))
    return;

  setLoadingYear(year, true);
  fetch(`https://u406ruqhf4.execute-api.eu-west-1.amazonaws.com/WeatherYears/weather?year=${year}`)
    .then((response) => {
      if (response.ok) {
        response.json()
          .then((json) => JSON.parse(json || "[]"))
          .then((parsed) => onSuccess(updateWeatherData(parsed)))
          .catch(() => onError("Error parsing weather data"))
          .finally(() => setLoadingYear(year, false));
      } else {
        response.json()
          .then((json) => {
            console.log("error:", json);
            onError(`Unable to fetch weather response body. Status: ${response.status}: ${json}`)
          })
          .finally(() => setLoadingYear(year, false));
      }
    })
    .catch(() => onError(`Failed to fetch weather data for years: ${year}`))
    .finally(() => setLoadingYear(year, false));
}

export const fetchWeatherForYears = (years, onError, onSuccess) => {
  years.forEach((y) => fetchWeatherForYear(y, onError, onSuccess))
}
