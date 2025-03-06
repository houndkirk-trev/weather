package com.github.houndkirk.weather.parser.utils;

import com.github.houndkirk.weather.common.MonthWeather;

import java.util.ArrayList;
import java.util.List;

public final class TestUtils {

    public static final int YEAR = 2024;
    private static final int JANUARY = 0;
    private static final int FEBRUARY = 1;
    private static final int MARCH = 2;
    private static final int APRIL = 3;
    private static final int MAY = 4;
    private static final int JUNE = 5;
    private static final int JULY = 6;
    private static final int AUGUST = 7;
    private static final int SEPTEMBER = 8;
    private static final int OCTOBER = 9;
    private static final int NOVEMBER = 10;
    private static final int DECEMBER = 11;

    private TestUtils() {}

    public static List<MonthWeather> createExpectedResults2024() {
        List<MonthWeather> yearWeather = new ArrayList<>();
        MonthWeather janWeather = createMonthWeather(JANUARY, -4.4f, 12.7f, 2.3193548f, 6.4548388f);
        yearWeather.add(janWeather);
        MonthWeather febWeather = createMonthWeather(FEBRUARY, -0.6f, 14.9f, 4.682759f, 8.986207f);
        yearWeather.add(febWeather);
        MonthWeather marWeather = createMonthWeather(MARCH, -1.0f, 14.3f, 4.5310345f, 9.593103f);
        yearWeather.add(marWeather);
        MonthWeather aprWeather = createMonthWeather(APRIL, 1.4f, 17.8f, 5.8966665f, 11.706667f);
        yearWeather.add(aprWeather);
        MonthWeather mayWeather = createMonthWeather(MAY, 6.5f, 22.2f, 10.874193f, 17.174194f);
        yearWeather.add(mayWeather);
        MonthWeather junWeather = createMonthWeather(JUNE, 6.6f, 24.9f, 11.19f, 17.603449f);
        yearWeather.add(junWeather);
        MonthWeather julWeather = createMonthWeather(JULY, 7.8f, 26.4f, 13.034483f, 19.27931f);
        yearWeather.add(julWeather);
        MonthWeather augWeather = createMonthWeather(AUGUST, 7.9f, 24.5f, 13.3f, 19.886957f);
        yearWeather.add(augWeather);
        MonthWeather sepWeather = createMonthWeather(SEPTEMBER, 3.7f, 22.4f, 10.49f, 15.446667f);
        yearWeather.add(sepWeather);
        MonthWeather octWeather = createMonthWeather(OCTOBER, 3.7f, 15.6f, 8.154839f, 12.735484f);
        yearWeather.add(octWeather);
        MonthWeather novWeather = createMonthWeather(NOVEMBER, -2.6f, 14.0f, 4.7566667f, 8.666667f);
        yearWeather.add(novWeather);
        MonthWeather decWeather = createMonthWeather(DECEMBER, 1.1f, 12.1f, 5.2724137f, 8.317242f);
        yearWeather.add(decWeather);
        return yearWeather;
    }

    private static MonthWeather createMonthWeather(final int month, final float min, final float max,
                                                   final float avgMin, final float avgMax) {
        return new MonthWeather.Builder()
                .year(YEAR)
                .month(month)
                .min(min)
                .max(max)
                .averageMin(avgMin)
                .averageMax(avgMax)
                .build();
    }
}
