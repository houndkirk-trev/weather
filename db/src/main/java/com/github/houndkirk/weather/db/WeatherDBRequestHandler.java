package com.github.houndkirk.weather.db;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.github.houndkirk.weather.db.impl.DynamoDBHandler;
import com.github.houndkirk.weather.parser.weather.MonthWeather;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

@SuppressWarnings("unused")
public class WeatherDBRequestHandler implements RequestHandler<SQSEvent, String> {

    @Override
    public String handleRequest(SQSEvent event, Context context) {
        context.getLogger().log("Processing " + event.getRecords() + " records from SQS queue", LogLevel.INFO);
        for (SQSEvent.SQSMessage message : event.getRecords()) {
            String body = message.getBody();
            List<MonthWeather> weatherData = new Gson().fromJson(body,
                                                                 new TypeToken<List<MonthWeather>>() {}.getType());
            context.getLogger().log("Processing weather data for " + weatherData.size() + " months");
            if (!weatherData.isEmpty()) {
                try (WeatherDBIf dbHandler = new DynamoDBHandler()) {
                    dbHandler.saveWeatherData(weatherData);
                }
            }
        }
        context.getLogger().log("Successfully saved weather data.");
        return "SUCCESS";
    }
}
