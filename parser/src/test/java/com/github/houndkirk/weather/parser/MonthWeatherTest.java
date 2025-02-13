package com.github.houndkirk.weather.parser;

import com.github.houndkirk.weather.parser.weather.MonthWeather;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MonthWeatherTest {

    private static final float MIN = -1.0f;
    private static final float MAX = 35.3f;
    private static final float AVG_MIN = 4.2f;
    private static final float AVG_MAX = 27.1f;
    private static final int YEAR = 2024;
    private static final int MONTH = 4;
    private static final int INVALID_MONTH_LOW = -1;
    private static final int INVALID_MONTH_13 = 13;

    @Test
    void testWeatherBuilder() {
        MonthWeather.Builder builder = new MonthWeather.Builder();
        MonthWeather weather = builder.month(MONTH)
                                      .year(YEAR)
                                      .min(MIN)
                                      .max(MAX)
                                      .averageMin(AVG_MIN)
                                      .averageMax(AVG_MAX)
                                      .build();

        assertThat(weather.getYear(), is(YEAR));
        assertThat(weather.getMonth(), is(MONTH));
        assertThat(weather.getMin(), is(MIN));
        assertThat(weather.getMax(), is(MAX));
        assertThat(weather.getAverageMin(), is(AVG_MIN));
        assertThat(weather.getAverageMax(), is(AVG_MAX));
    }

    @Test
    void testWeatherBuilderWithMonthTooLow() {
        MonthWeather.Builder builder = new MonthWeather.Builder();
        assertThrows(IllegalArgumentException.class, () -> builder.month(INVALID_MONTH_LOW));
    }

    @Test
    void testWeatherBuilderWithMonthTooHigh() {
        MonthWeather.Builder builder = new MonthWeather.Builder();
        assertThrows(IllegalArgumentException.class, () -> builder.month(INVALID_MONTH_13));
    }

    @Test
    void testWeatherBuilderWithMonthNotSet() {
        MonthWeather.Builder builder = new MonthWeather.Builder();
        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void testWeatherBuilderWithNulls() {
        MonthWeather.Builder builder = new MonthWeather.Builder();
        MonthWeather weather = builder.month(MONTH).year(YEAR).build();

        assertThat(weather.getMin(), nullValue());
        assertThat(weather.getMax(), nullValue());
        assertThat(weather.getAverageMin(), nullValue());
        assertThat(weather.getAverageMax(), nullValue());
    }
}
