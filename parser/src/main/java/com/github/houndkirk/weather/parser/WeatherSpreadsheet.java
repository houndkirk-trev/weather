package com.github.houndkirk.weather.parser;

import com.github.houndkirk.weather.common.MonthWeather;
import com.github.miachm.sods.Range;
import com.github.miachm.sods.Sheet;
import com.github.miachm.sods.SpreadSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class WeatherSpreadsheet {
    private static final Logger log = LoggerFactory.getLogger(WeatherSpreadsheet.class);

    private final SpreadSheet spreadSheet;

    public WeatherSpreadsheet(@NotNull final SpreadSheet spreadSheet) {
        if (spreadSheet == null) {
            throw new IllegalArgumentException("Spreadsheet must not be null!");
        }
        this.spreadSheet = spreadSheet;
    }

    /*
     * Return an ordered set of sheet names which should correspond to years.
     */
    public Set<Integer> getAvailableYears() {
        List<Integer> years =  spreadSheet.getSheets().stream()
                                          .map(Sheet::getName)
                                          .map(this::getSheetNameAsYear)
                                          .filter(Objects::nonNull)
                                          .toList();
        return new TreeSet<>(years);
    }

    public List<MonthWeather> getAllWeather() {
        List<MonthWeather> yearData = new ArrayList<>();
        Set<Integer> yearSheets = getAvailableYears();
        for (int year : yearSheets) {
            Sheet sheet = spreadSheet.getSheet(String.valueOf(year));
            yearData.addAll(getWeatherFromSheet(sheet, year));
        }
        return yearData;
    }

    public List<MonthWeather> getWeatherForYear(final int year) {
        List<MonthWeather> yearData = Collections.emptyList();
        Sheet sheet = spreadSheet.getSheet(String.valueOf(year));
        if (sheet != null) {
            yearData = getWeatherFromSheet(sheet, year);
        }
        return yearData;
    }

    private Integer getSheetNameAsYear(final String sheetName) {
        Integer year = null;
        try {
            year = Integer.parseInt(sheetName);
        } catch (NumberFormatException e) {
            log.info("Sheet named \"{}\" does not correspond to a year (must be an integer)", sheetName);
        }
        return year;
    }

    private List<MonthWeather> getWeatherFromSheet(@NotNull final Sheet sheet, final int year) {
        Range range = sheet.getDataRange();
        int cols = range.getNumColumns();
        int rows = range.getNumRows();

        if (rows > 20) {
            rows = 20;
        }

        log.info("Sheet \"{}\" contains {} rows and {} columns", sheet.getName(), rows, cols);
        int avgTableFirstRow = -1;
        int avgTableFirstCol = -1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Range cellRange = range.getCell(r, c);
                if ("Month".equals(cellRange.getValue())) {
                    // Header row for averages table found
                    log.info("Averages table starts at row {}, column {} ", r, c);
                    avgTableFirstRow = r + 1; // January averages row
                    avgTableFirstCol = c + 1;
                    break;
                }
            }
        }

        List<MonthWeather> monthlyAverages = new ArrayList<>();
        if (avgTableFirstRow != -1 && avgTableFirstCol != -1) {
            for (int r = 0; r < 12; r++) {
                Float avgMin = floatValueOrNull(range,avgTableFirstRow + r, avgTableFirstCol);
                Float avgMax = floatValueOrNull(range,avgTableFirstRow + r, avgTableFirstCol + 1);
                Float min = floatValueOrNull(range,avgTableFirstRow + r, avgTableFirstCol + 2);
                Float max = floatValueOrNull(range,avgTableFirstRow + r, avgTableFirstCol + 3);
                MonthWeather weather = new MonthWeather.Builder()
                        .month(r)
                        .year(year)
                        .min(min)
                        .max(max)
                        .averageMin(avgMin)
                        .averageMax(avgMax)
                        .build();
                monthlyAverages.add(weather);
            }
        } else {
            log.info("Averages table not found on sheet \"{}\"", sheet.getName());
        }

        return monthlyAverages;
    }

    private Float floatValueOrNull(final Range range, final int row, final int col) {
        Object cellValue = range.getCell(row, col).getValue();
        Float floatValue = null;
        if (cellValue != null) {
            try {
                floatValue = Float.parseFloat(cellValue.toString());
            } catch (NumberFormatException e) {
                log.warn("Unrecognised float value at row {}, column {} ", row, col);
            }
        } else {
            log.debug("No value in cell at row {}, column {}", row, col);
        }
        return floatValue;
    }
}
