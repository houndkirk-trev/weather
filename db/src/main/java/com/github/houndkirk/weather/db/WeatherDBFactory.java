package com.github.houndkirk.weather.db;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.github.houndkirk.weather.db.impl.DynamoDBHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WeatherDBFactory {

    private static final Logger log = LoggerFactory.getLogger(WeatherDBFactory.class);

    private WeatherDBFactory() {}

    public static WeatherDB getWeatherDB() {
        log.warn("getWeatherDB: creating DynamoDBHandler");
        return new DynamoDBHandler();
    }


    public static WeatherDB getWeatherDB(final LambdaLogger logger) {
        logger.log("getWeatherDB: creating DynamoDBHandler");
        return new DynamoDBHandler();
    }
}
