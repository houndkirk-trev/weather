package com.github.houndkirk.weather.common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbImmutable(builder = MonthWeather.Builder.class)
public class MonthWeather {
    private Float averageMin;
    private Float averageMax;
    private Float min;
    private Float max;
    private int month;
    private int year;

    public MonthWeather() {
    }

    private MonthWeather(final Builder builder) {
        this.year = builder.year;
        this.month = builder.month;
        this.min = builder.min;
        this.averageMin = builder.averageMin;
        this.max = builder.max;
        this.averageMax = builder.averageMax;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Float getAverageMin() {
        return averageMin;
    }

    public Float getAverageMax() {
        return averageMax;
    }

    public Float getMin() {
        return min;
    }

    public Float getMax() {
        return max;
    }

    @DynamoDbSortKey
    public int getMonth() {
        return month;
    }

    @DynamoDbPartitionKey
    public int getYear() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MonthWeather weather = (MonthWeather) o;

        return new EqualsBuilder()
                .append(year, weather.year)
                .append(month, weather.month)
                .append(averageMin, weather.averageMin)
                .append(averageMax, weather.averageMax)
                .append(min, weather.min)
                .append(max, weather.max)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(year)
                .append(month)
                .append(averageMin)
                .append(averageMax)
                .append(min)
                .append(max)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("year", year)
                .append("month", month)
                .append("averageMin", averageMin)
                .append("averageMax", averageMax)
                .append("min", min)
                .append("max", max)
                .toString();
    }

    public static class Builder {
        private Float averageMin;
        private Float averageMax;
        private Float min;
        private Float max;
        private int month = -1;
        private int year = -1;

        public Builder averageMin(Float averageMin) {
            this.averageMin = averageMin;
            return this;
        }

        public Builder averageMax(Float averageMax) {
            this.averageMax = averageMax;
            return this;
        }

        public Builder min(Float min) {
            this.min = min;
            return this;
        }

        public Builder max(Float max) {
            this.max = max;
            return this;
        }

        public Builder month(int month) {
            validateMonth(month);
            this.month = month;
            return this;
        }

        public Builder year(int year) {
            this.year = year;
            return this;
        }

        public MonthWeather build() {
            if (year == -1) {
                throw new IllegalArgumentException("Year must be set");
            }
            validateMonth(month);
            return new MonthWeather(this);
        }

        private void validateMonth(final int month) {
            if (month < 0 || month > 11) {
                throw new IllegalArgumentException("Month is invalid: " + month);
            }
        }
    }
}
