package com.stambul.library.tools;

import org.postgresql.jdbc.TimestampUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeTools {

    /**
     * @return current timestamp in date format: yyyy-mm-dd hh:mm:ss in Moscow time zone
     */
    public static String currentTimestamp() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        return dateFormat.format(date);
    }

    /**
     * Converts a textual representation of a duration in ISO format to milliseconds
     *
     * @param isoTime a textual representation of a duration in ISO format that you want to convert to milliseconds
     * @return duration specified in isoTime param in milliseconds
     */
    public static Long toMilliseconds(String isoTime) {
        return Duration.parse(isoTime).getSeconds() * 1000;
    }

    /**
     * Converts a textual representation of a duration in ISO format to seconds
     *
     * @param isoTime a textual representation of a duration in ISO format that you want to convert to seconds
     * @return duration specified in isoTime param in seconds
     */
    public static Long toSeconds(String isoTime) {
        return Duration.parse(isoTime).getSeconds();
    }

    /**
     * Converts a textual representation of a duration in ISO format to minutes
     *
     * @param isoTime a textual representation of a duration in ISO format that you want to convert to minutes
     * @return duration specified in isoTime param in minutes
     */
    public static double toMinutes(String isoTime) {
        return Duration.parse(isoTime).getSeconds() / 60.0;
    }

    /**
     * Converts a textual representation of a duration in ISO format to hours
     *
     * @param isoTime a textual representation of a duration in ISO format that you want to convert to hours
     * @return duration specified in isoTime param in hours
     */
    public static double toHours(String isoTime) {
        return Duration.parse(isoTime).getSeconds() / 3600.0;
    }

    /**
     * Converts a textual representation of a duration in ISO format to Duration object
     *
     * @param isoTime a textual representation of a duration in ISO format that you want to convert to Duration object
     * @return a Duration object representing the duration specified in isoTime param
     */
    public static Duration toDuration(String isoTime) {
        return Duration.parse(isoTime);
    }

    /**
     * Converts a textual representation of a duration in ISO format to Instant object
     * @param isoTime a textual representation of a duration in ISO format that you want to convert to Instant object
     * @return an Instant object representing the current Instant plus the duration specified in isoTime param
     */
    public static Instant toInstant(String isoTime) {
        return Instant.now().plusSeconds(toSeconds(isoTime));
    }

    /**
     * Converts a textual representation of a PostgreSQL TIMESTAMPTZ to LocalDateTime object
     * @param psqlDate a textual representation of a PostgreSQL TIMESTAMPTZ that you want to convert to Instant object
     * @return an LocalDateTime object representing the date specified in psqlDate param
     * @throws IllegalArgumentException if the textual representation of a PostgreSQL TIMESTAMPTZ specified in psqlDate param is incorrect
     */
    public static LocalDateTime toLocalDateTime(String psqlDate) {
        if (psqlDate == null)
            return null;

        if (psqlDate.isEmpty())
            return null;

        try {
            TimestampUtils utils = new TimestampUtils(true, null);
            return utils.toLocalDateTime(psqlDate);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Can not parse psql date time: " + psqlDate, e);
        }
    }

    /**
     * Converts a textual representation of timestamp to PostgreSQL TIMESTAMPTZ object
     * @param coinmarketcapDate a textual representation of timestamp in coinmarketcap date format that you want to convert to PostgreSQL TIMESTAMPTZ object
     * @return a Timestamp object representing the date specified in coinmarketcapDate param
     * @throws IllegalArgumentException if the textual representation of timestamp in coinmarketcap date format specified in coinmarketcapDate param is incorrect
     */
    public static Timestamp toPsqlTimestamp(String coinmarketcapDate) {
        if (coinmarketcapDate == null)
            return null;

        if (coinmarketcapDate.isEmpty())
            return null;

        return toPsqlTimestamp(toDate(coinmarketcapDate));
    }

    /**
     * Converts a Date object to PostgreSQL TIMESTAMPTZ object
     * @param date a Date object that you want to convert to PostgreSQL TIMESTAMPTZ object
     * @return a Timestamp object representing the date specified in date param
      */
    public static Timestamp toPsqlTimestamp(Date date) {
        long time = date.getTime();
        return new Timestamp(time);
    }

    /**
     * Converts a Date object to coinmarketcap date format
     *
     * @param date a Date object that you want to convert to coinmarketcap date format
     * @return a String representing the date specified in date param in  coinmarketcap date format
     */
    public static String toCoinmarketcapTimestamp(Date date) {
        if (date == null)
            return null;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return dateFormat.format(date);
    }

    /**
     * Converts a textual representation of timestamp to Date object
     *
     * @param coinmarketcapDate a textual representation of timestamp in coinmarketcap date format that you want to convert to Date object
     * @return a Date object representing the date specified in coinmarketcapDate param
     * @throws IllegalArgumentException if the textual representation of timestamp in coinmarketcap date format specified in coinmarketcapDate param is incorrect
     */
    public static Date toDate(String coinmarketcapDate) {
        if (coinmarketcapDate == null)
            return null;

        if (coinmarketcapDate.isEmpty())
            return null;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.parse(coinmarketcapDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Can not parse coinmarketcap date time: " + coinmarketcapDate, e);
        }
    }
}