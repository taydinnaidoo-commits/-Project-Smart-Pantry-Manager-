package za.ac.richfield.smartpantry.logic;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Converts quantities between units so that a pantry item measured in one unit
 * can be compared with a recipe that asks for another (2 kg against 250 g,
 * 1 cup against 1 tsp, and so on).
 *
 * <p>Units belong to a {@link Dimension}. Units of the same dimension always
 * convert. Different dimensions only convert through mass, and only when the
 * ingredient is in one of the two small tables below:
 * <ul>
 *     <li>{@code GRAMS_PER_PIECE} - so "2 onions" can satisfy "150 g onion";</li>
 *     <li>{@code GRAMS_PER_ML} - so "500 g salt" can satisfy "1 tsp salt".</li>
 * </ul>
 * For anything else, a mass and a volume cannot be compared and
 * {@link #convert} returns {@link Double#NaN}. The matcher then treats that
 * ingredient as not available, which keeps the strict rule strict: we never
 * guess.
 */
public final class UnitConverter {

    /** What a unit measures. Slices are separate so "1 loaf" is not "1 slice". */
    public enum Dimension { MASS, VOLUME, COUNT, SLICE }

    /** Units the user can pick in the Add/Edit screen, in spinner order. */
    public static final List<String> SUPPORTED_UNITS = Collections.unmodifiableList(
            Arrays.asList("g", "kg", "ml", "l", "tsp", "tbsp", "cup", "piece", "slice"));

    private static final Map<String, Dimension> DIMENSIONS = new HashMap<>();
    /** How many base units (g, ml, pieces, slices) one of each unit is. */
    private static final Map<String, Double> TO_BASE = new HashMap<>();
    /** Other spellings of the supported units. */
    private static final Map<String, String> UNIT_ALIASES = new HashMap<>();

    /** Typical weight of one item, keyed by normalised ingredient name. */
    private static final Map<String, Double> GRAMS_PER_PIECE = new HashMap<>();
    /** Density of common dry and liquid staples, keyed by normalised name. */
    private static final Map<String, Double> GRAMS_PER_ML = new HashMap<>();

    static {
        define("g", Dimension.MASS, 1);
        define("kg", Dimension.MASS, 1000);
        define("ml", Dimension.VOLUME, 1);
        define("l", Dimension.VOLUME, 1000);
        define("tsp", Dimension.VOLUME, 5);
        define("tbsp", Dimension.VOLUME, 15);
        define("cup", Dimension.VOLUME, 250);      // South African metric cup
        define("piece", Dimension.COUNT, 1);
        define("slice", Dimension.SLICE, 1);

        alias("g", "gram", "grams", "gr");
        alias("kg", "kilogram", "kilograms", "kgs");
        alias("ml", "millilitre", "millilitres", "milliliter", "milliliters", "mls");
        alias("l", "litre", "litres", "liter", "liters", "lt", "ltr");
        alias("tsp", "teaspoon", "teaspoons");
        alias("tbsp", "tablespoon", "tablespoons", "tbs");
        alias("cup", "cups");
        alias("piece", "pieces", "pc", "pcs", "each", "x", "item", "items", "unit", "units", "");
        alias("slice", "slices");

        GRAMS_PER_PIECE.put("egg", 50.0);
        GRAMS_PER_PIECE.put("onion", 150.0);
        GRAMS_PER_PIECE.put("tomato", 120.0);
        GRAMS_PER_PIECE.put("potato", 170.0);
        GRAMS_PER_PIECE.put("carrot", 70.0);
        GRAMS_PER_PIECE.put("banana", 120.0);
        GRAMS_PER_PIECE.put("apple", 180.0);
        GRAMS_PER_PIECE.put("avocado", 200.0);
        GRAMS_PER_PIECE.put("lemon", 100.0);
        GRAMS_PER_PIECE.put("green pepper", 150.0);
        GRAMS_PER_PIECE.put("cucumber", 300.0);
        GRAMS_PER_PIECE.put("garlic clove", 5.0);
        GRAMS_PER_PIECE.put("spring onion", 15.0);

        GRAMS_PER_ML.put("flour", 0.53);
        GRAMS_PER_ML.put("sugar", 0.85);
        GRAMS_PER_ML.put("salt", 1.2);
        GRAMS_PER_ML.put("oil", 0.92);
        GRAMS_PER_ML.put("olive oil", 0.91);
        GRAMS_PER_ML.put("butter", 0.96);
        GRAMS_PER_ML.put("milk", 1.03);
        GRAMS_PER_ML.put("honey", 1.42);
        GRAMS_PER_ML.put("rice", 0.85);
        GRAMS_PER_ML.put("maize meal", 0.6);
        GRAMS_PER_ML.put("oat", 0.41);
        GRAMS_PER_ML.put("baking powder", 0.9);
        GRAMS_PER_ML.put("curry powder", 0.45);
        GRAMS_PER_ML.put("paprika", 0.45);
        GRAMS_PER_ML.put("soy sauce", 1.15);
        GRAMS_PER_ML.put("mayonnaise", 0.95);
        GRAMS_PER_ML.put("yoghurt", 1.03);
    }

    private UnitConverter() {
    }

    private static void define(String unit, Dimension dimension, double toBase) {
        DIMENSIONS.put(unit, dimension);
        TO_BASE.put(unit, toBase);
    }

    private static void alias(String unit, String... others) {
        for (String other : others) {
            UNIT_ALIASES.put(other, unit);
        }
    }

    /** Returns the supported unit code for any spelling, or null if unknown. */
    public static String canonicalUnit(String unit) {
        if (unit == null) {
            return null;
        }
        String u = unit.trim().toLowerCase(Locale.ROOT).replace(".", "");
        if (DIMENSIONS.containsKey(u)) {
            return u;
        }
        return UNIT_ALIASES.get(u);
    }

    /** The dimension of a unit, or null if the unit is unknown. */
    public static Dimension dimensionOf(String unit) {
        String canonical = canonicalUnit(unit);
        return canonical == null ? null : DIMENSIONS.get(canonical);
    }

    /**
     * Converts {@code amount} from one unit to another.
     *
     * @param ingredientKey the normalised ingredient name (see
     *                      {@link IngredientNormalizer#normalize}), used to look
     *                      up piece weights and densities; may be null
     * @return the converted amount, or {@link Double#NaN} if the two units
     * cannot honestly be compared for this ingredient
     */
    public static double convert(double amount, String fromUnit, String toUnit, String ingredientKey) {
        String from = canonicalUnit(fromUnit);
        String to = canonicalUnit(toUnit);
        if (from == null || to == null) {
            return Double.NaN;
        }
        Dimension fromDimension = DIMENSIONS.get(from);
        Dimension toDimension = DIMENSIONS.get(to);
        double baseAmount = amount * TO_BASE.get(from);

        if (fromDimension == toDimension) {
            return baseAmount / TO_BASE.get(to);
        }

        // Different dimensions: go through grams, if this ingredient allows it.
        Double grams = toGrams(baseAmount, fromDimension, ingredientKey);
        if (grams == null) {
            return Double.NaN;
        }
        Double target = fromGrams(grams, toDimension, ingredientKey);
        return target == null ? Double.NaN : target / TO_BASE.get(to);
    }

    private static Double toGrams(double baseAmount, Dimension dimension, String key) {
        switch (dimension) {
            case MASS:
                return baseAmount;
            case COUNT:
                Double perPiece = key == null ? null : GRAMS_PER_PIECE.get(key);
                return perPiece == null ? null : baseAmount * perPiece;
            case VOLUME:
                Double density = key == null ? null : GRAMS_PER_ML.get(key);
                return density == null ? null : baseAmount * density;
            default:
                return null;
        }
    }

    private static Double fromGrams(double grams, Dimension dimension, String key) {
        switch (dimension) {
            case MASS:
                return grams;
            case COUNT:
                Double perPiece = key == null ? null : GRAMS_PER_PIECE.get(key);
                return perPiece == null ? null : grams / perPiece;
            case VOLUME:
                Double density = key == null ? null : GRAMS_PER_ML.get(key);
                return density == null ? null : grams / density;
            default:
                return null;
        }
    }
}
