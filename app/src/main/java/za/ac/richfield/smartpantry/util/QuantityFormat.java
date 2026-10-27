package za.ac.richfield.smartpantry.util;

import java.text.DecimalFormat;

/** Formats quantities for display, e.g. "1.75 kg", "3 pieces", "1 cup". */
public final class QuantityFormat {

    private QuantityFormat() {
    }

    /** A number without pointless decimals: 3.0 -> "3", 1.750 -> "1.75". */
    public static String number(double value) {
        return new DecimalFormat("0.##").format(value);
    }

    /** A quantity with its unit, pluralising the count-style units. */
    public static String amount(double quantity, String unit) {
        String number = number(quantity);
        boolean one = number.equals("1");
        switch (unit) {
            case "piece":
                return number + (one ? " piece" : " pieces");
            case "slice":
                return number + (one ? " slice" : " slices");
            case "cup":
                return number + (one ? " cup" : " cups");
            default:
                return number + " " + unit;
        }
    }

    /**
     * Parses user input into a number, accepting either "1.5" or "1,5" (South
     * African keyboards often use a comma). Returns null if it is not a number.
     */
    public static Double parse(String text) {
        if (text == null) {
            return null;
        }
        String cleaned = text.trim().replace(',', '.');
        if (cleaned.isEmpty()) {
            return null;
        }
        try {
            double value = Double.parseDouble(cleaned);
            return Double.isNaN(value) || Double.isInfinite(value) ? null : value;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
