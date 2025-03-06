package com.github.houndkirk.weather.db;

import com.github.houndkirk.weather.common.MonthWeather;

import java.io.Closeable;
import java.util.List;
import java.util.Set;

public interface WeatherDB extends Closeable {

    void createTable();
    void deleteTable();

    void saveWeatherData(List<MonthWeather> yearData);
    Set<Integer> getAvailableYears();
    List<MonthWeather> readDataForYear(int year);
    List<MonthWeather> readDataForMonthAndYear(int year, int month);
    List<MonthWeather> readDataForMonth(int month);
}
