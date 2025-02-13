package com.github.houndkirk.weather.db;

import com.github.houndkirk.weather.db.impl.DynamoDBHandler;
import com.github.houndkirk.weather.db.utils.TestUtils;
import com.github.houndkirk.weather.parser.weather.MonthWeather;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/*
 * Integration test for the Dynamo DB handler class.
 * Requires credentials to be provided in one of the AWS credentials provider mechanisms.
 * In IntelliJ, I found that adding environment variables: AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY
 * to the Run Configuration for this class did the trick.
 */
public class DynamoDBHandlerIT {

    private DynamoDBHandler classUnderTest;

    @BeforeEach
    void setUp() {
        classUnderTest = new DynamoDBHandler();
    }

    @AfterEach
    void tearDown() {
        classUnderTest.close();
    }

    @Test
    void canWriteDataToTableAndReadItBack() {
        List<MonthWeather> sampleYearWeather = TestUtils.createExpectedResults2024();
        classUnderTest.saveWeatherData(sampleYearWeather);
        List<MonthWeather> savedYearWeather = classUnderTest.readDataForYear(TestUtils.YEAR);
        MatcherAssert.assertThat(savedYearWeather, Matchers.hasSize(12));
        sampleYearWeather.forEach(w -> MatcherAssert.assertThat(savedYearWeather, Matchers.hasItem(w)));
    }

    @Test
    void canWriteDataToTableAndReadOneMonthForAYear() {
        List<MonthWeather> sampleYearWeather = TestUtils.createExpectedResults2024();
        classUnderTest.saveWeatherData(sampleYearWeather);
        MonthWeather savedMonthWeather = classUnderTest.readDataForMonthAndYear(TestUtils.YEAR, 0);
        MatcherAssert.assertThat(savedMonthWeather, Matchers.is(sampleYearWeather.getFirst()));
    }

    @Test
    void canReadDataForMonth() {
        List<MonthWeather> sampleYearWeather = TestUtils.createExpectedResults2024();
        // Add in an extra month so that the query should return two items
        MonthWeather monthWeather = MonthWeather.builder()
                                                .month(1)
                                                .year(TestUtils.YEAR - 1)
                                                .build();
        sampleYearWeather.add(monthWeather);
        classUnderTest.saveWeatherData(sampleYearWeather);
        List<MonthWeather> result = classUnderTest.readDataForMonth(1);
        MatcherAssert.assertThat(result, Matchers.hasSize(2));
        MatcherAssert.assertThat(result, Matchers.hasItem(monthWeather));
    }

    @Test
    void noDataForMonthWithNoTable() {
        classUnderTest.deleteTable();
        List<MonthWeather> result = classUnderTest.readDataForMonth(1);
        MatcherAssert.assertThat(result, Matchers.empty());
    }

    @Test
    void noDataForMonthWithEmptyTable() {
        // To empty the table, the simplest thing is to delete and recreate the table
        classUnderTest.deleteTable();
        classUnderTest.createTable();
        List<MonthWeather> result = classUnderTest.readDataForMonth(1);
        MatcherAssert.assertThat(result, Matchers.empty());
    }
}
