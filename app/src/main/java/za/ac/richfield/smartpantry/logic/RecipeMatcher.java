package za.ac.richfield.smartpantry.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;

/**
 * The strict-matching rule (assignment section 2.3).
 *
 * <p>A recipe is suggested only if EVERY ingredient it needs is in the pantry
 * in AT LEAST the quantity it needs. For each recipe ingredient we:
 * <ol>
 *     <li>find every pantry item with the same normalised name
 *         ({@link IngredientNormalizer});</li>
 *     <li>convert each of those items into the recipe's unit
 *         ({@link UnitConverter}) and add them up;</li>
 *     <li>compare that total with the amount the recipe needs.</li>
 * </ol>
 * If even one ingredient is missing, short, or in a unit we cannot compare,
 * the recipe is not suggested. Recipes that fail on exactly one ingredient are
 * returned separately by {@link #almostThere} so the UI can show them in their
 * own, clearly labelled section.
 */
public final class RecipeMatcher {

    /** Allowance for floating-point rounding after unit conversion. */
    private static final double EPSILON = 0.0001;

    /** How one recipe ingredient compares with the pantry. */
    public enum Status {
        /** Present in at least the required quantity. */
        HAVE,
        /** Present, but not enough of it. */
        NOT_ENOUGH,
        /** Present, but in a unit that cannot be compared (e.g. g against ml). */
        INCOMPATIBLE_UNITS,
        /** Not in the pantry at all. */
        MISSING
    }

    /** The result of checking one recipe ingredient against the pantry. */
    public static final class IngredientCheck {
        private final RecipeIngredient ingredient;
        private final Status status;
        private final double available;

        IngredientCheck(RecipeIngredient ingredient, Status status, double available) {
            this.ingredient = ingredient;
            this.status = status;
            this.available = available;
        }

        public RecipeIngredient getIngredient() {
            return ingredient;
        }

        public Status getStatus() {
            return status;
        }

        /** Total amount in the pantry, expressed in the recipe ingredient's unit. */
        public double getAvailable() {
            return available;
        }

        public boolean isSatisfied() {
            return status == Status.HAVE;
        }
    }

    /** The result of checking a whole recipe against the pantry. */
    public static final class MatchResult {
        private final Recipe recipe;
        private final List<IngredientCheck> checks;

        MatchResult(Recipe recipe, List<IngredientCheck> checks) {
            this.recipe = recipe;
            this.checks = Collections.unmodifiableList(checks);
        }

        public Recipe getRecipe() {
            return recipe;
        }

        public List<IngredientCheck> getChecks() {
            return checks;
        }

        /** True only when every single ingredient is satisfied. */
        public boolean canMake() {
            return getMissingCount() == 0;
        }

        public int getMissingCount() {
            return getMissingIngredients().size();
        }

        public List<RecipeIngredient> getMissingIngredients() {
            List<RecipeIngredient> missing = new ArrayList<>();
            for (IngredientCheck check : checks) {
                if (!check.isSatisfied()) {
                    missing.add(check.getIngredient());
                }
            }
            return missing;
        }
    }

    private RecipeMatcher() {
    }

    /** Checks every ingredient of one recipe against the pantry. */
    public static MatchResult check(Recipe recipe, List<PantryItem> pantry) {
        List<IngredientCheck> checks = new ArrayList<>();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            checks.add(checkIngredient(ingredient, pantry));
        }
        return new MatchResult(recipe, checks);
    }

    /** Checks one recipe ingredient against the whole pantry. */
    public static IngredientCheck checkIngredient(RecipeIngredient need, List<PantryItem> pantry) {
        String key = IngredientNormalizer.normalize(need.getName());
        boolean foundByName = false;
        boolean comparable = false;
        double available = 0;

        for (PantryItem item : pantry) {
            if (!key.equals(IngredientNormalizer.normalize(item.getName()))) {
                continue;
            }
            foundByName = true;
            double amount = UnitConverter.convert(
                    item.getQuantity(), item.getUnit(), need.getUnit(), key);
            if (Double.isNaN(amount)) {
                continue;   // e.g. pantry has grams, recipe wants ml, no density known
            }
            comparable = true;
            available += amount;
        }

        Status status;
        if (!foundByName) {
            status = Status.MISSING;
        } else if (!comparable) {
            status = Status.INCOMPATIBLE_UNITS;
        } else if (available + EPSILON >= need.getQuantity()) {
            status = Status.HAVE;
        } else {
            status = Status.NOT_ENOUGH;
        }
        return new IngredientCheck(need, status, available);
    }

    /** The strict suggestions: recipes the user can cook right now, and nothing else. */
    public static List<MatchResult> suggest(List<Recipe> recipes, List<PantryItem> pantry) {
        List<MatchResult> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult match = check(recipe, pantry);
            if (match.canMake()) {
                result.add(match);
            }
        }
        return result;
    }

    /** The optional "Almost there" list: recipes missing exactly one ingredient. */
    public static List<MatchResult> almostThere(List<Recipe> recipes, List<PantryItem> pantry) {
        List<MatchResult> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            MatchResult match = check(recipe, pantry);
            if (match.getMissingCount() == 1) {
                result.add(match);
            }
        }
        return result;
    }
}
