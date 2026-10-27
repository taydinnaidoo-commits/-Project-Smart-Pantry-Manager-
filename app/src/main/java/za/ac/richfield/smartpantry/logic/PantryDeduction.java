package za.ac.richfield.smartpantry.logic;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;

/**
 * Works out what cooking a recipe takes out of the pantry, for the
 * "Cook this recipe" button on the detail screen.
 *
 * <p>It uses the same name and unit rules as {@link RecipeMatcher}, so the two
 * can never disagree. If an ingredient cannot be found, or its units cannot be
 * compared, nothing is subtracted for it: taking away a guess would be worse
 * than taking away nothing.
 *
 * <p>Quantities are subtracted in the unit the USER entered (2 kg less 250 g
 * leaves 1.75 kg, not 1750 g) and rounded to two decimal places.
 */
public final class PantryDeduction {

    private static final double EPSILON = 0.0001;

    /** One pantry item and the quantity it will have after cooking. */
    public static final class Change {
        private final PantryItem before;
        private final double newQuantity;

        Change(PantryItem before, double newQuantity) {
            this.before = before;
            this.newQuantity = newQuantity;
        }

        /** The pantry item as it was before cooking. */
        public PantryItem getBefore() {
            return before;
        }

        /** The quantity left, in the item's own unit. */
        public double getNewQuantity() {
            return newQuantity;
        }

        /** True when cooking uses the item up, so it should be removed. */
        public boolean isExhausted() {
            return newQuantity <= 0;
        }
    }

    private PantryDeduction() {
    }

    /** Lists the changes cooking {@code recipe} would make to {@code pantry}. */
    public static List<Change> forRecipe(Recipe recipe, List<PantryItem> pantry) {
        // Quantity left in each touched item, in that item's own unit. Identity
        // map, so two items with equal fields are still tracked separately.
        Map<PantryItem, Double> left = new IdentityHashMap<>();
        List<PantryItem> touched = new ArrayList<>();

        for (RecipeIngredient need : recipe.getIngredients()) {
            String key = IngredientNormalizer.normalize(need.getName());
            double stillNeeded = need.getQuantity();

            for (PantryItem item : pantry) {
                if (stillNeeded <= EPSILON) {
                    break;
                }
                if (!key.equals(IngredientNormalizer.normalize(item.getName()))) {
                    continue;
                }
                double itemLeft = left.containsKey(item) ? left.get(item) : item.getQuantity();
                double itemLeftInRecipeUnit = UnitConverter.convert(
                        itemLeft, item.getUnit(), need.getUnit(), key);
                if (Double.isNaN(itemLeftInRecipeUnit) || itemLeftInRecipeUnit <= 0) {
                    continue;
                }
                double take = Math.min(itemLeftInRecipeUnit, stillNeeded);
                double takeInItemUnit = UnitConverter.convert(take, need.getUnit(), item.getUnit(), key);

                if (!left.containsKey(item)) {
                    touched.add(item);
                }
                left.put(item, itemLeft - takeInItemUnit);
                stillNeeded -= take;
            }
        }

        List<Change> changes = new ArrayList<>();
        for (PantryItem item : touched) {
            changes.add(new Change(item, roundToCents(Math.max(0, left.get(item)))));
        }
        return changes;
    }

    /** Rounds to 2 decimal places so what is shown is exactly what is saved. */
    private static double roundToCents(double value) {
        double rounded = Math.round(value * 100.0) / 100.0;
        return rounded < 0.01 ? 0 : rounded;
    }
}
