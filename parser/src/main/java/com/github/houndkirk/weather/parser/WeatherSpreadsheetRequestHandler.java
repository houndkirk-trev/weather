package com.github.houndkirk.weather.parser;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import com.github.houndkirk.weather.common.MonthWeather;
import com.github.houndkirk.weather.common.WeatherConstants;
import com.google.gson.Gson;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.InputStream;
import java.util.List;

@SuppressWarnings("unused")
public class WeatherSpreadsheetRequestHandler implements RequestHandler<S3Event, String> {
    private static final String QUEUE_NAME = WeatherConstants.QUEUE_NAME;

    @Override
    public String handleRequest(final S3Event event, Context context) {
        context.getLogger().log("Processing uploaded spreadsheet", LogLevel.INFO);
        String response = "";
        try {
            S3EventNotification.S3EventNotificationRecord record = event.getRecords().getFirst();

            String srcBucket = record.getS3().getBucket().getName();
            String srcKey = record.getS3().getObject().getUrlDecodedKey();

            // Download the image from S3 into a stream
            S3Client s3Client = S3Client.builder().build();
            InputStream spreadSheetInputStream = getObject(s3Client, srcBucket, srcKey);

            // Parse the spreadsheet, send the parsed data to the weather queue and return it to caller
            WeatherSpreadsheetParser spreadSheetParser = new WeatherSpreadsheetParser();
            WeatherSpreadsheet spreadSheet = spreadSheetParser.parse(spreadSheetInputStream);
            if (spreadSheet != null) {
                List<MonthWeather> weather = spreadSheet.getAllWeather();
                response = new Gson().toJson(weather);
                sendMessage(response, context);
            } else {
                context.getLogger().log("Parsing of spreadsheet failed", LogLevel.ERROR);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        context.getLogger().log("Successfully processed spreadsheet", LogLevel.INFO);

        return response;
    }

    private InputStream getObject(S3Client s3Client, String bucket, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                                            .bucket(bucket)
                                                            .key(key)
                                                            .build();
        return s3Client.getObject(getObjectRequest);
    }

    private String getQueueUrl(final SqsClient sqsClient) {
        GetQueueUrlResponse getQueueUrlResponse =
                sqsClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(QUEUE_NAME).build());
        return getQueueUrlResponse.queueUrl();
    }

    private void sendMessage(final String message, final Context context) {
        try (SqsClient sqsClient = SqsClient.create()) {
            sqsClient.sendMessage(SendMessageRequest.builder()
                                                    .queueUrl(getQueueUrl(sqsClient))
                                                    .messageBody(message)
                                                    .build());
        } catch (Exception e) {
            context.getLogger().log("Failed to send spreadsheet data to queue: " + e.getMessage(), LogLevel.ERROR);
        }
    }
}
