package com.github.houndkirk.weather.db;

import com.github.houndkirk.weather.parser.weather.MonthWeather;

import java.io.Closeable;
import java.util.List;

public interface WeatherDBIf extends Closeable {

    /*
     * Close the DB handler.
     */
    void close();

    void createTable();
    void deleteTable();

    void saveWeatherData(final List<MonthWeather> yearData);

    List<MonthWeather> readDataForYear(final int year);
    MonthWeather readDataForMonthAndYear(final int year, final int month);
    List<MonthWeather> readDataForMonth(final int month);
}
