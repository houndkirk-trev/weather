import { first, isNumber, remove } from "lodash";

const BASE_URL = "https://u406ruqhf4.execute-api.eu-west-1.amazonaws.com/WeatherYears/weather";

const loadingYears = [];
const isLoadingYear = (year) => loadingYears.includes(year);
const setLoadingYear = (year, loading) => {
  if (loading && !isLoadingYear(year))
    loadingYears.push(year);
  else if (!loading)
    remove(loadingYears, (y) => y === year);
}

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

export const fetchAvailableYears = (onError, onSuccess) => {
  fetch(`${BASE_URL}/availableyears`)
    .then((response) => {
      if (response.ok) {
        response.json()
          .then((json) => JSON.parse(json || "[]"))
          .then((parsed) => onSuccess(parsed))
          .catch((reason) => onError(`Unable to parse available years: ${reason}`));
      } else {
        response.json()
          .then((json) => {
            console.log("error fetching available years:", json);
            const errMessage = (typeof json === 'object') ? (json.message || JSON.stringify(json)) : json;
            onError(`Unable to fetch data for available years. Server responded with ${response.status}, message: ${errMessage}`)
          });
      }
    })
    .catch((reason) => onError(`Query for available years failed: ${reason}`));
}

const fetchWeatherForYear = (year, onError, onSuccess) => {
  if (isLoadingYear(year))
    return;

  setLoadingYear(year, true);
  fetch(`${BASE_URL}?year=${year}`)
    .then((response) => {
      if (response.ok) {
        response.json()
          .then((json) => JSON.parse(json || "[]"))
          .then((parsed) => onSuccess(updateWeatherData(parsed)))
          .catch(() => onError(`Error parsing weather data for ${year}`))
          .finally(() => setLoadingYear(year, false));
      } else {
        response.json()
          .then((json) => {
            console.log("error fetching weather for year:", json);
            onError(`Unable to fetch weather for ${year} response body. Status: ${response.status}: ${json}`)
          })
          .finally(() => setLoadingYear(year, false));
      }
    })
    .catch(() => onError(`Failed to fetch weather data for year: ${year}`))
    .finally(() => setLoadingYear(year, false));
}

export const fetchWeatherForYears = (years, onError, onSuccess) => {
  years.forEach((y) => fetchWeatherForYear(y, onError, onSuccess))
}
