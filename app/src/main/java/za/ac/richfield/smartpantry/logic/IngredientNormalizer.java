package za.ac.richfield.smartpantry.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns an ingredient name into a "matching key" so that trivially different
 * spellings of the same ingredient compare as equal.
 *
 * <p>Both the pantry item names and the recipe ingredient names go through
 * {@link #normalize(String)} before they are compared, so "Tomatoes",
 * "tomato" and " Fresh tomatoes " all become "tomato".
 *
 * <p>The steps, in order:
 * <ol>
 *     <li>lower-case, turn hyphens into spaces and drop punctuation and digits;</li>
 *     <li>make every word singular ("tomatoes" -> "tomato", "berries" -> "berry");</li>
 *     <li>drop descriptive words that do not change what the ingredient is
 *         ("fresh", "large", "chopped" ...);</li>
 *     <li>map known synonyms to one name ("mealie meal" -> "maize meal").</li>
 * </ol>
 *
 * <p>It deliberately does NOT do fuzzy or partial matching: "coconut milk"
 * must never satisfy a recipe that needs "milk", because the strict rule says
 * a recipe is only suggested when the user really has every ingredient.
 */
public final class IngredientNormalizer {

    /** Words that describe an ingredient without changing what it is. */
    private static final Set<String> DESCRIPTORS = new HashSet<>(Arrays.asList(
            "fresh", "large", "medium", "small", "ripe", "raw", "chopped", "diced",
            "sliced", "grated", "frozen", "organic", "free", "range", "tinned",
            "canned", "dried", "whole"));

    /** Plurals that the simple suffix rules below would get wrong. */
    private static final Map<String, String> IRREGULAR_PLURALS = new HashMap<>();

    /** Different names people use for the same ingredient, mapped to one key. */
    private static final Map<String, String> SYNONYMS = new HashMap<>();

    static {
        IRREGULAR_PLURALS.put("leaves", "leaf");
        IRREGULAR_PLURALS.put("loaves", "loaf");
        IRREGULAR_PLURALS.put("halves", "half");
        IRREGULAR_PLURALS.put("cookies", "cookie");
        IRREGULAR_PLURALS.put("pies", "pie");
        IRREGULAR_PLURALS.put("chillies", "chilli");

        SYNONYMS.put("mealie meal", "maize meal");
        SYNONYMS.put("maizemeal", "maize meal");
        SYNONYMS.put("scallion", "spring onion");
        SYNONYMS.put("green onion", "spring onion");
        SYNONYMS.put("cilantro", "coriander");
        SYNONYMS.put("chili", "chilli");
        SYNONYMS.put("chile", "chilli");
        SYNONYMS.put("ground beef", "beef mince");
        SYNONYMS.put("minced beef", "beef mince");
        SYNONYMS.put("mince", "beef mince");
        SYNONYMS.put("cheddar", "cheese");
        SYNONYMS.put("cheddar cheese", "cheese");
        SYNONYMS.put("feta", "feta cheese");
        SYNONYMS.put("mayo", "mayonnaise");
        SYNONYMS.put("yogurt", "yoghurt");
        SYNONYMS.put("plain yoghurt", "yoghurt");
        SYNONYMS.put("chicken breast", "chicken");
        SYNONYMS.put("chicken fillet", "chicken");
        SYNONYMS.put("cooking oil", "oil");
        SYNONYMS.put("sunflower oil", "oil");
        SYNONYMS.put("vegetable oil", "oil");
        SYNONYMS.put("cake flour", "flour");
        SYNONYMS.put("plain flour", "flour");
        SYNONYMS.put("white sugar", "sugar");
        SYNONYMS.put("castor sugar", "sugar");
        SYNONYMS.put("garlic", "garlic clove");
        SYNONYMS.put("macaroni", "pasta");
        SYNONYMS.put("penne", "pasta");
        SYNONYMS.put("fusilli", "pasta");
        SYNONYMS.put("white bread", "bread");
        SYNONYMS.put("brown bread", "bread");
        SYNONYMS.put("green bell pepper", "green pepper");
        SYNONYMS.put("rolled oat", "oat");
    }

    private IngredientNormalizer() {
    }

    /**
     * Returns the matching key for an ingredient name, or an empty string for
     * a null or blank name.
     */
    public static String normalize(String rawName) {
        if (rawName == null) {
            return "";
        }
        String cleaned = rawName.toLowerCase(Locale.ROOT)
                .replace('-', ' ')
                .replaceAll("[^a-z ]", " ")
                .trim()
                .replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            return "";
        }

        List<String> words = new ArrayList<>();
        for (String word : cleaned.split(" ")) {
            words.add(singular(word));
        }

        // Synonyms are checked before descriptors are removed, so that
        // "minced beef" is recognised before "minced"-style words go missing.
        String joined = String.join(" ", words);
        if (SYNONYMS.containsKey(joined)) {
            return SYNONYMS.get(joined);
        }

        List<String> kept = new ArrayList<>();
        for (String word : words) {
            if (!DESCRIPTORS.contains(word)) {
                kept.add(word);
            }
        }
        // A name made only of descriptors ("Fresh") is left as it was.
        String key = kept.isEmpty() ? joined : String.join(" ", kept);
        return SYNONYMS.containsKey(key) ? SYNONYMS.get(key) : key;
    }

    /** True when two names refer to the same ingredient. */
    public static boolean sameIngredient(String a, String b) {
        String keyA = normalize(a);
        return !keyA.isEmpty() && keyA.equals(normalize(b));
    }

    /** Makes one English word singular using simple suffix rules. */
    static String singular(String word) {
        if (IRREGULAR_PLURALS.containsKey(word)) {
            return IRREGULAR_PLURALS.get(word);
        }
        if (word.length() <= 3) {
            return word;
        }
        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";        // berries -> berry
        }
        if (word.endsWith("oes") || word.endsWith("sses") || word.endsWith("ches")
                || word.endsWith("shes") || word.endsWith("xes") || word.endsWith("zes")) {
            return word.substring(0, word.length() - 2);              // tomatoes -> tomato
        }
        if (word.endsWith("s") && !word.endsWith("ss") && !word.endsWith("us")
                && !word.endsWith("is")) {
            return word.substring(0, word.length() - 1);              // eggs -> egg
        }
        return word;
    }
}
