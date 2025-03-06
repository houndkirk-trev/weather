package com.github.houndkirk.weather.db;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.github.houndkirk.weather.common.MonthWeather;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("unused")
public class WeatherDBRequestHandler implements RequestHandler<WeatherDBRequestHandler.WeatherRequest, String> {

    public record WeatherRequest(WeatherRequestType operation, String year, String month) {
        public enum WeatherRequestType { GET_AVAILABLE_YEARS, GET_MONTH, GET_YEAR, GET_MONTH_YEAR }

        public String toJsonString() {
            return "\"request\":{\"type\":\"" + operation + "\",\"year\":" + year + ",\"month\":" + month + "}";
        }
    }

    @Override
    public String handleRequest(WeatherRequest request, Context context) {
        String result = "";
        context.getLogger().log("Processing request: " + request);
        try (WeatherDB dbClient = WeatherDBFactory.getWeatherDB(context.getLogger())) {
            context.getLogger().log("Got DB client, handling request: " + request);
            String year = request.year;
            String month = request.month;
            switch (request.operation) {
                case GET_AVAILABLE_YEARS:
                    result = availableYears(dbClient);
                    context.getLogger().log("DB client returned: " + result);
                    break;
                case GET_MONTH:
                    if (month != null && !month.isBlank()) {
                        result = monthData(dbClient, month);
                    } else {
                        result = error("Month must be provided!", request);
                    }
                    break;
                case GET_YEAR:
                    if (year != null && !year.isBlank()) {
                        // TODO validate year?
                        result = yearData(dbClient, year);
                    } else {
                        result = error("Year must be provided!", request);
                    }
                    break;
                case GET_MONTH_YEAR:
                    if (year != null && !year.isBlank() && month != null && !month.isBlank()) {
                        result = monthYearData(dbClient, year, month);
                    } else {
                        result = error("Year and month must be provided!", request);
                    }
                    break;
            }
            context.getLogger().log("Result: " + result);
        } catch (IOException e) {
            // Contract of Closeable says that this can be thrown
            // but DynamoDBClient.close() does not throw it.
            context.getLogger().log("Unexpected problem closing DB: " + e.getMessage());
        }
        return result;
    }

    /**
     * Get a JSON formatted list of years for which there is weather data in the database.
     * The returned list is sorted by year descending.
     *
     * @param dbClient Database client.
     * @return  JSON array containing a list of years sorted in descending order or an empty array.
     */
    private String availableYears(final WeatherDB dbClient) {
        // Copy results into a new set so we can order the results.
        Set<Integer> availableYears = new TreeSet<>(Collections.reverseOrder());
        availableYears.addAll(dbClient.getAvailableYears());

        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.toJson(availableYears, listType);
    }

    private String yearData(final WeatherDB dbClient, final String year) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<MonthWeather>>() {}.getType();
        List<MonthWeather> yearWeather = dbClient.readDataForYear(Integer.parseInt(year));
        return gson.toJson(yearWeather, listType);
    }

    private String monthData(final WeatherDB dbClient, final String month) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<MonthWeather>>() {}.getType();
        List<MonthWeather> monthWeather = dbClient.readDataForMonth(Integer.parseInt(month));
        return gson.toJson(monthWeather, listType);
    }

    private String monthYearData(final WeatherDB dbClient, final String year, final String month) {
        Gson gson = new Gson();
        List<MonthWeather> monthWeather = dbClient.readDataForMonthAndYear(Integer.parseInt(year),
                                                                           Integer.parseInt(month));
        return gson.toJson(monthWeather);
    }

    private String error(final String message, final WeatherRequest request) {
        return "{\"errorMessage\":\"" + message + "\"," + request.toJsonString() + "}";
    }
}
