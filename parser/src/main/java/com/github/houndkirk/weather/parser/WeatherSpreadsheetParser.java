package com.github.houndkirk.weather.parser;

import com.github.miachm.sods.Sheet;
import com.github.miachm.sods.SpreadSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class WeatherSpreadsheetParser {
    private static final Logger log = LoggerFactory.getLogger(WeatherSpreadsheetParser.class);

    public WeatherSpreadsheet parse(final String filename) {
        WeatherSpreadsheet spreadsheet = null;
        try {
            spreadsheet = parse(Files.newInputStream(Paths.get(filename)));
        } catch (IOException e) {
            log.error("Unable to read file {}: {}", filename, e.getMessage());
        }
        return spreadsheet;
    }

    public WeatherSpreadsheet parse(final InputStream inputFile) {
        WeatherSpreadsheet spreadsheet = null;
        try {
            SpreadSheet inputSpreadSheet = new SpreadSheet(inputFile);

            if (log.isDebugEnabled()) {
                List<String> sheets = inputSpreadSheet.getSheets().stream().map(Sheet::getName).toList();
                String sheetNames = String.join(", ", sheets);
                log.debug("Spreadsheet contains {} sheet(s).", inputSpreadSheet.getNumSheets());
                log.debug("Sheet name(s): {}", sheetNames);
            }

            spreadsheet = new WeatherSpreadsheet(inputSpreadSheet);
        } catch (IOException e){
            log.error("Unable to complete read of spreadsheet: {}", e.getMessage());
        }

        return spreadsheet;
    }
}
