package com.github.houndkirk.weather.db;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.github.houndkirk.weather.common.MonthWeather;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class WeatherDBRequestHandlerTest {

    @Mock private Context mockContext;
    @Mock private LambdaLogger mockLogger;

    private List<MonthWeather> weatherData;
    private WeatherDBRequestHandler classUnderTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getLogger()).thenReturn(mockLogger);
        weatherData = createWeatherData();
        classUnderTest = new WeatherDBRequestHandler();
    }

    @Test
    void getAvailableYearsReturns() throws IOException {
        try (var mockWeatherFactory = mockStatic(WeatherDBFactory.class);
            var mockWeatherDB = mock(WeatherDB.class)) {
            Set<Integer> years = Set.of(2021, 2024, 2023, 2022, 2025);
            when(mockWeatherDB.getAvailableYears()).thenReturn(years);
            mockWeatherFactory.when(WeatherDBFactory::getWeatherDB).thenReturn(mockWeatherDB);

            var request = new WeatherDBRequestHandler.WeatherRequest(
                    WeatherDBRequestHandler.WeatherRequest.WeatherRequestType.GET_AVAILABLE_YEARS);
            String result = classUnderTest.handleRequest(request, mockContext);

            // Sort the year list in place and use it to manually construct the expected JSON string
            Set<Integer> sortedYears = new TreeSet<>(Collections.reverseOrder());
            sortedYears.addAll(years);
            List<String> stringYears = sortedYears.stream().map(String::valueOf).toList();
            String expectedResult = "[" + String.join(",", stringYears) + "]";
            assertThat(result, is(expectedResult));
        }
    }

    @Test
    void getAvailableYearsReturnsEmptyList() throws IOException {
        try (var mockWeatherFactory = mockStatic(WeatherDBFactory.class);
             var mockWeatherDB = mock(WeatherDB.class)) {
            when(mockWeatherDB.getAvailableYears()).thenReturn(Collections.emptySet());
            mockWeatherFactory.when(WeatherDBFactory::getWeatherDB).thenReturn(mockWeatherDB);

            var request = new WeatherDBRequestHandler.WeatherRequest(
                    WeatherDBRequestHandler.WeatherRequest.WeatherRequestType.GET_AVAILABLE_YEARS);
            String result = classUnderTest.handleRequest(request, mockContext);
            String expectedResult = "[]";
            assertThat(result, is(expectedResult));
        }
    }

    @Test
    void getMonthDataWithValidMonth() throws IOException {
        try (var mockWeatherFactory = mockStatic(WeatherDBFactory.class);
             var mockWeatherDB = mock(WeatherDB.class)) {
            List<MonthWeather> januaryWeather = weatherData.stream().filter(mw -> mw.getMonth() == 0).toList();
            when(mockWeatherDB.readDataForMonth(anyInt())).thenReturn(januaryWeather);
            mockWeatherFactory.when(WeatherDBFactory::getWeatherDB).thenReturn(mockWeatherDB);

            var request = new WeatherDBRequestHandler.WeatherRequest(
                    WeatherDBRequestHandler.WeatherRequest.WeatherRequestType.GET_MONTH, "0");
            String result = classUnderTest.handleRequest(request, mockContext);
            String expectedResult =
                    "[" +
                            String.join(",", januaryWeather.stream().map(this::jsonMonthWeather).toList()) +
                    "]";
            assertThat(result, is(expectedResult));
        }
    }

    @Test
    void getMonthDataWithInvalidMonthReturnsEmptyList() throws IOException {
        try (var mockWeatherFactory = mockStatic(WeatherDBFactory.class);
             var mockWeatherDB = mock(WeatherDB.class)) {
            when(mockWeatherDB.readDataForMonth(anyInt())).thenReturn(Collections.emptyList());
            mockWeatherFactory.when(WeatherDBFactory::getWeatherDB).thenReturn(mockWeatherDB);

            var request = new WeatherDBRequestHandler.WeatherRequest(
                    WeatherDBRequestHandler.WeatherRequest.WeatherRequestType.GET_MONTH, "99");
            String result = classUnderTest.handleRequest(request, mockContext);
            String expectedResult = "[]";
            assertThat(result, is(expectedResult));
        }
    }

    @Test
    void getYearDataWithValidYear() throws IOException {
        try (var mockWeatherFactory = mockStatic(WeatherDBFactory.class);
             var mockWeatherDB = mock(WeatherDB.class)) {
            List<MonthWeather> weather2024 = weatherData.stream().filter(mw -> mw.getYear() == 2024).toList();
            when(mockWeatherDB.readDataForYear(2024)).thenReturn(weather2024);
            mockWeatherFactory.when(WeatherDBFactory::getWeatherDB).thenReturn(mockWeatherDB);

            var request = new WeatherDBRequestHandler.WeatherRequest(
                    WeatherDBRequestHandler.WeatherRequest.WeatherRequestType.GET_YEAR, "2024");
            String result = classUnderTest.handleRequest(request, mockContext);
            String expectedResult =
                    "[" +
                            String.join(",", weather2024.stream().map(this::jsonMonthWeather).toList()) +
                    "]";
            assertThat(result, is(expectedResult));
        }
    }

    @Test
    void getYearDataWithInvalidYearReturnsEmptyList() throws IOException {
        try (var mockWeatherFactory = mockStatic(WeatherDBFactory.class);
             var mockWeatherDB = mock(WeatherDB.class)) {
            when(mockWeatherDB.readDataForYear(1970)).thenReturn(Collections.emptyList());
            mockWeatherFactory.when(WeatherDBFactory::getWeatherDB).thenReturn(mockWeatherDB);

            var request = new WeatherDBRequestHandler.WeatherRequest(
                    WeatherDBRequestHandler.WeatherRequest.WeatherRequestType.GET_MONTH, "1970");
            String result = classUnderTest.handleRequest(request, mockContext);
            String expectedResult = "[]";
            assertThat(result, is(expectedResult));
        }
    }

    @Test
    void getYearMonthDataWithValidYear() throws IOException {
        try (var mockWeatherFactory = mockStatic(WeatherDBFactory.class);
             var mockWeatherDB = mock(WeatherDB.class)) {
            MonthWeather weather =
                    weatherData.stream()
                               .filter(mw -> (mw.getYear() == 2024) && (mw.getMonth() == 4))
                               .findAny()
                               .orElseThrow( () -> new RuntimeException("no weather data for given month and year"));
            when(mockWeatherDB.readDataForMonthAndYear(2024, 4)).thenReturn(weather);
            mockWeatherFactory.when(WeatherDBFactory::getWeatherDB).thenReturn(mockWeatherDB);

            var request = new WeatherDBRequestHandler.WeatherRequest(
                    WeatherDBRequestHandler.WeatherRequest.WeatherRequestType.GET_MONTH_YEAR, "2024", "4");
            String result = classUnderTest.handleRequest(request, mockContext);
            String expectedResult = jsonMonthWeather(weather);
            assertThat(result, is(expectedResult));
        }
    }

    @Test
    void getYearMonthDataWithInvalidYear() throws IOException {
        try (var mockWeatherFactory = mockStatic(WeatherDBFactory.class);
             var mockWeatherDB = mock(WeatherDB.class)) {
            when(mockWeatherDB.readDataForMonthAndYear(1970, 1)).thenReturn(null);
            mockWeatherFactory.when(WeatherDBFactory::getWeatherDB).thenReturn(mockWeatherDB);

            var request = new WeatherDBRequestHandler.WeatherRequest(
                    WeatherDBRequestHandler.WeatherRequest.WeatherRequestType.GET_MONTH_YEAR, "2024", "4");
            String result = classUnderTest.handleRequest(request, mockContext);
            String expectedResult = "";
            assertThat(result, is(expectedResult));
        }
    }

    private String jsonMonthWeather(final MonthWeather mw) {
        // Assumes that any null fields are not returned from the class in the resulting JSON
        return "{" + "\"month\":" + mw.getMonth() + ",\"year\":" + mw.getYear() + "}";
    }

    private List<MonthWeather> createWeatherData() {
        List<MonthWeather> weatherData = new ArrayList<>();
        for (int y = 2021; y <= 2025; y++) {
            for (int m = 0; m < 12; m++) {
                MonthWeather mw = MonthWeather.builder().year(y).month(m).build();
                weatherData.add(mw);
            }
        }
        return weatherData;
    }
}
