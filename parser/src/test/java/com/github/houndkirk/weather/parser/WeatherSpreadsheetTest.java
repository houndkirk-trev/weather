package com.github.houndkirk.weather.parser;

import com.github.houndkirk.weather.parser.utils.TestUtils;
import com.github.houndkirk.weather.common.MonthWeather;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

public class WeatherSpreadsheetTest {

    private static final int YEAR = TestUtils.YEAR;

    private WeatherSpreadsheet spreadsheet;

    @BeforeEach
    void setUp() {
        spreadsheet = new WeatherSpreadsheetParser().parse(WeatherSpreadsheetParserTest.SPREADSHEET_FILE_NAME);
    }

    @Test
    void testNoWeatherForYear() {
        List<MonthWeather> weather = spreadsheet.getWeatherForYear(1990);
        assertThat(weather, empty());

    }
    @Test
    void testHasWeatherForYear() {
        List<MonthWeather> weather = spreadsheet.getWeatherForYear(YEAR);
        assertThat(weather, notNullValue());
    }

    @Test
    void testHasWeatherForYearAndMonths() {
        List<MonthWeather> weather = spreadsheet.getWeatherForYear(YEAR);
        for (int month = 0; month < 12; month++) {
            assertThat(weather.get(month), notNullValue());
        }
    }

    @Test
    void testMonthlyWeatherHasCorrectValues() {
        List<MonthWeather> weather = spreadsheet.getWeatherForYear(YEAR);
        List<MonthWeather> expectedWeather = TestUtils.createExpectedResults2024();
        assertThat(weather, hasSize(expectedWeather.size()));
        expectedWeather.forEach(w -> assertThat(weather, hasItem(w)));
    }
}
