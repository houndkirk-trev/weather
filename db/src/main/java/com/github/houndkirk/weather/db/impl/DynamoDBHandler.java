package com.github.houndkirk.weather.db.impl;

import com.github.houndkirk.weather.db.WeatherDBIf;
import com.github.houndkirk.weather.parser.weather.MonthWeather;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.internal.AttributeValues;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteResult;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.retries.api.BackoffStrategy;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ListTablesRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TableClass;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DynamoDBHandler implements WeatherDBIf {

    private static final Logger log = LoggerFactory.getLogger(DynamoDBHandler.class);
    private static final String YEAR_DATA_TABLE_NAME = "YearData";

    private final DynamoDbClient dbClient;
    private final DynamoDbEnhancedClient dbEnhancedClient;
    private DynamoDbTable<MonthWeather> weatherTable = null;

    public DynamoDBHandler() {
        Region region = Region.EU_WEST_1;
        dbClient = DynamoDbClient.builder()
                                 .region(region)
                                 .build();
        dbEnhancedClient = DynamoDbEnhancedClient.builder()
                                                 .dynamoDbClient(dbClient)
                                                 .build();
        createTable();
    }

    @Override
    public void close() {
        dbClient.close();
    }

    @Override
    public void createTable() {
        createTableEnhanced();
    }


    @Override
    public void deleteTable() {
        log.info("Deleting table {}", YEAR_DATA_TABLE_NAME);
        DeleteTableRequest request = DeleteTableRequest.builder().tableName(YEAR_DATA_TABLE_NAME).build();
        try {
            long start = System.currentTimeMillis();
            dbClient.deleteTable(request);

            // Wait for the table to be deleted
            try (DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(dbClient).build()) {
                ResponseOrException<DescribeTableResponse> response = waiter
                        .waitUntilTableNotExists(builder -> builder.tableName(YEAR_DATA_TABLE_NAME).build(),
                                                 configBuilder -> configBuilder.backoffStrategyV2(BackoffStrategy.fixedDelay(Duration.ofMillis(500))))
                        .matched();
                Optional<DescribeTableResponse> describeTableResponse = response.response();
                describeTableResponse.ifPresent(tableResponse -> log.info("Table description: {}", tableResponse));
            } catch (ResourceNotFoundException ignored) {
                // Table was already gone
            }
            long end = System.currentTimeMillis();
            log.info("Deleted table {} in {}ms", YEAR_DATA_TABLE_NAME, (end - start));
        } catch (Exception e) {
            log.error("Unable to delete table: {}", YEAR_DATA_TABLE_NAME, e);
        }
    }

    @Override
    public void saveWeatherData(final List<MonthWeather> yearData) {
        int dataSize = yearData.size();
        for (int startIndex = 0; startIndex < dataSize; startIndex += 25) {
            int endIndex = startIndex + 25;
            if (endIndex > dataSize) {
                endIndex = dataSize;
            }
            log.info("Write batch to DB from {} to {}", startIndex, endIndex);
            writeBatch(yearData.subList(startIndex, endIndex));
        }
    }

    @Override
    public List<MonthWeather> readDataForYear(final int year) {
        ReadBatch.Builder<MonthWeather> builder = ReadBatch.builder(MonthWeather.class)
                                                           .mappedTableResource(weatherTable);
        for (int i = 0; i < 12; i++) {
            Key key = Key.builder().partitionValue(year).sortValue(i).build();
            builder.addGetItem(key);
        }
        BatchGetResultPageIterable response = dbEnhancedClient.batchGetItem(b -> b.readBatches(builder.build()));
        List<MonthWeather> yearData = new ArrayList<>(12);
        response.forEach(r -> yearData.addAll(r.resultsForTable(weatherTable)));
        return yearData;
    }

    @Override
    public MonthWeather readDataForMonthAndYear(final int year, final int month) {
        return readDataForMonthAndYearUsingQuery(year, month);
    }

    @Override
    public List<MonthWeather> readDataForMonth(final int month) {
        List<MonthWeather> result = new ArrayList<>();
        Map<String, AttributeValue> expressionValues = Map.of(":month", AttributeValues.numberValue(month));
        // month is a reserved word, so adapt it
        Map<String, String> expressionNames = Map.of("#month", "month");
        Expression filterExpression = Expression.builder()
                                                .expressionNames(expressionNames)
                                                .expression("#month = :month")
                                                .expressionValues(expressionValues)
                                                .build();

        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                                                         .consistentRead(true)
                                                         .filterExpression(filterExpression)
                                                         .build();

        try {
            PageIterable<MonthWeather> response = weatherTable.scan(request);
            return response.items().stream().toList();
        } catch (ResourceNotFoundException e) {
            // Table does not exist
            log.warn("readDataForMonth: table {} does not exist!", YEAR_DATA_TABLE_NAME);
            return Collections.emptyList();
        }
    }

    private void createTableEnhanced() {
        try {
            weatherTable = dbEnhancedClient.table(YEAR_DATA_TABLE_NAME,
                                                  TableSchema.fromImmutableClass(MonthWeather.class));
            weatherTable.createTable();
            try (DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(dbClient).build()) {
                ResponseOrException<DescribeTableResponse> response = waiter
                        .waitUntilTableExists(builder -> builder.tableName(YEAR_DATA_TABLE_NAME).build())
                        .matched();
                response.response().orElseThrow(
                        () -> new RuntimeException("Table \"" + YEAR_DATA_TABLE_NAME + "\" was not created."));
                log.info("Table {} created", YEAR_DATA_TABLE_NAME);
            }
        } catch (ResourceInUseException ignored) {
            // Table exists, do nothing
            log.info("Table {} already exists", YEAR_DATA_TABLE_NAME);
        }
    }

    private void writeBatch(final List<MonthWeather> weatherBatch) {
        assert(weatherBatch.size() <= 25);
        WriteBatch.Builder<MonthWeather> builder =
                WriteBatch.builder(MonthWeather.class).mappedTableResource(weatherTable);
        weatherBatch.forEach(builder::addPutItem);
        WriteBatch yearDataBatch = builder.build();
        BatchWriteResult response = dbEnhancedClient.batchWriteItem(b -> b.writeBatches(yearDataBatch));
        response.unprocessedPutItemsForTable(weatherTable)
                .forEach(key -> log.warn("Month weather {} was not saved.", key.toString()));
    }

    private void createTableOld() {
        KeySchemaElement primaryKeyElement = KeySchemaElement.builder()
                                                             .keyType(KeyType.HASH)
                                                             .attributeName("year")
                                                             .build();
        KeySchemaElement secondaryKeyElement = KeySchemaElement.builder()
                                                               .keyType(KeyType.RANGE)
                                                               .attributeName("month")
                                                               .build();

        AttributeDefinition attr0 = AttributeDefinition.builder()
                                                       .attributeName("year")
                                                       .attributeType(ScalarAttributeType.N)
                                                       .build();

        AttributeDefinition attr1 = AttributeDefinition.builder()
                                                       .attributeName("month")
                                                       .attributeType(ScalarAttributeType.N)
                                                       .build();

        CreateTableRequest request = CreateTableRequest.builder()
                                                       .tableName(YEAR_DATA_TABLE_NAME)
                                                       .tableClass(TableClass.STANDARD_INFREQUENT_ACCESS)
                                                       .keySchema(primaryKeyElement, secondaryKeyElement)
                                                       .billingMode(BillingMode.PAY_PER_REQUEST)
                                                       .attributeDefinitions(attr0, attr1)
                                                       .build();

        try {
            CreateTableResponse response = dbClient.createTable(request);
            log.info("Create table response: {}", response.toString());
        } catch (Exception e) {
            log.error("Unable to create table \"{}\".", YEAR_DATA_TABLE_NAME, e);
        }
    }
    private MonthWeather readDataForMonthAndYearUsingBatch(final int year, final int month) {
        Key key = Key.builder().partitionValue(year).sortValue(month).build();
        ReadBatch readBatch = ReadBatch.builder(MonthWeather.class)
                                       .mappedTableResource(weatherTable)
                                       .addGetItem(key)
                                       .build();
        BatchGetResultPageIterable response = dbEnhancedClient.batchGetItem(b -> b.readBatches(readBatch));
        List<MonthWeather> resultList =  response.stream()
                                                 .findAny().map(r -> r.resultsForTable(weatherTable))
                                                 .stream()
                                                 .findFirst()
                                                 .orElse(Collections.emptyList());
        return resultList.isEmpty() ? null : resultList.getFirst();
    }

    private MonthWeather readDataForMonthAndYearUsingQuery(final int year, final int month) {
        Key key = Key.builder().partitionValue(year).sortValue(month).build();
        QueryConditional keyEqualTo = QueryConditional.keyEqualTo(key);
        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                                                           .queryConditional(keyEqualTo)
                                                           .build();
        PageIterable<MonthWeather> response = weatherTable.query(request);
        return response.items().stream().findFirst().orElse(null);
    }

    public void listAllTables() {
        boolean moreTables = true;
        String lastName = null;

        while (moreTables) {
            try {
                ListTablesResponse response = null;
                if (lastName == null) {
                    ListTablesRequest request = ListTablesRequest.builder().build();
                    response = dbClient.listTables(request);
                } else {
                    ListTablesRequest request = ListTablesRequest.builder()
                                                                 .exclusiveStartTableName(lastName).build();
                    response = dbClient.listTables(request);
                }

                List<String> tableNames = response.tableNames();
                if (tableNames.size() > 0) {
                    for (String curName : tableNames) {
                        System.out.format("* %s\n", curName);
                    }
                } else {
                    System.out.println("No tables found!");
                    break;
                }

                lastName = response.lastEvaluatedTableName();
                if (lastName == null) {
                    moreTables = false;
                }

            } catch (DynamoDbException e) {
                System.err.println(e.getMessage());
                break;
            }
        }
        System.out.println("\nDone!");
    }
}
