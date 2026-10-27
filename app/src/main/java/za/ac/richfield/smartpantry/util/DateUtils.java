package za.ac.richfield.smartpantry.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Expiry date helpers. Dates are stored in the database as ISO "yyyy-MM-dd"
 * strings, which sort correctly as text and are easy to read in a DB browser.
 *
 * <p>java.util.Calendar is used instead of java.time because the app supports
 * Android 7.0 (API 24), and java.time needs API 26.
 */
public final class DateUtils {

    private static final String DB_PATTERN = "yyyy-MM-dd";
    private static final long MILLIS_PER_DAY = 24L * 60 * 60 * 1000;

    private DateUtils() {
    }

    /** Builds the stored form of a date. {@code month} is 0-based, as in Calendar. */
    public static String toDbString(int year, int month, int day) {
        return String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day);
    }

    /** Today's date plus a number of days, in stored form. */
    public static String daysFromToday(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return toDbString(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    /** Parses a stored date into a Calendar at midnight, or null if invalid. */
    public static Calendar parse(String dbDate) {
        if (dbDate == null || dbDate.isEmpty()) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(DB_PATTERN, Locale.ROOT);
        format.setLenient(false);
        try {
            Date date = format.parse(dbDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return startOfDay(calendar);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Whole days from today until the date: 0 = today, 1 = tomorrow,
     * negative = already expired. Null when there is no valid date.
     */
    public static Integer daysUntil(String dbDate) {
        Calendar target = parse(dbDate);
        if (target == null) {
            return null;
        }
        Calendar today = startOfDay(Calendar.getInstance());
        long diff = target.getTimeInMillis() - today.getTimeInMillis();
        return (int) Math.round(diff / (double) MILLIS_PER_DAY);
    }

    /** A friendly form of a stored date, e.g. "14 Nov 2026". */
    public static String toDisplay(String dbDate) {
        Calendar calendar = parse(dbDate);
        if (calendar == null) {
            return "";
        }
        return new SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(calendar.getTime());
    }

    private static Calendar startOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
