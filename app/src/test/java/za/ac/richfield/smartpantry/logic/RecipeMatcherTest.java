package za.ac.richfield.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;

/**
 * Tests for the strict-matching rule (assignment section 2.3): a recipe is
 * suggested only when every ingredient is in the pantry in at least the
 * required quantity.
 */
public class RecipeMatcherTest {

    private static PantryItem item(String name, double quantity, String unit) {
        return new PantryItem(1L, name, quantity, unit, null);
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private static Recipe recipe(String name, RecipeIngredient... ingredients) {
        return new Recipe(1L, name, "", 10, Arrays.asList(ingredients),
                Collections.singletonList("Cook it."));
    }

    private static final Recipe FIVE_INGREDIENTS = recipe("Chakalaka",
            ing("Onion", 1, "piece"),
            ing("Carrot", 2, "piece"),
            ing("Tomato", 2, "piece"),
            ing("Baked beans", 410, "g"),
            ing("Oil", 2, "tbsp"));

    private static List<PantryItem> fullPantry() {
        return new ArrayList<>(Arrays.asList(
                item("Onions", 2, "piece"),
                item("Carrots", 3, "piece"),
                item("Tomatoes", 4, "piece"),
                item("Baked beans", 410, "g"),
                item("Sunflower oil", 750, "ml")));
    }

    @Test
    public void suggestsARecipeWhenEveryIngredientIsPresentInFull() {
        assertTrue(RecipeMatcher.check(FIVE_INGREDIENTS, fullPantry()).canMake());
    }

    @Test
    public void doesNotSuggestARecipeWithFourOfItsFiveIngredients() {
        // The exact example from the brief.
        List<PantryItem> pantry = fullPantry();
        pantry.remove(3);   // no baked beans

        RecipeMatcher.MatchResult result = RecipeMatcher.check(FIVE_INGREDIENTS, pantry);

        assertFalse(result.canMake());
        assertTrue(RecipeMatcher.suggest(Collections.singletonList(FIVE_INGREDIENTS), pantry)
                .isEmpty());
    }

    @Test
    public void doesNotSuggestARecipeWhenOneIngredientIsShort() {
        List<PantryItem> pantry = fullPantry();
        pantry.set(1, item("Carrots", 1, "piece"));   // needs 2

        RecipeMatcher.MatchResult result = RecipeMatcher.check(FIVE_INGREDIENTS, pantry);

        assertFalse(result.canMake());
        assertEquals(RecipeMatcher.Status.NOT_ENOUGH, result.getChecks().get(1).getStatus());
    }

    @Test
    public void exactlyTheRequiredQuantityIsEnough() {
        Recipe pap = recipe("Pap", ing("Maize meal", 250, "g"));
        assertTrue(RecipeMatcher.check(pap,
                Collections.singletonList(item("Maize meal", 250, "g"))).canMake());
    }

    @Test
    public void almostThereListsOnlyRecipesMissingExactlyOneIngredient() {
        List<PantryItem> pantry = fullPantry();
        pantry.remove(3);   // one missing
        Recipe twoMissing = recipe("Omelette", ing("Egg", 3, "piece"), ing("Cheese", 40, "g"));
        List<Recipe> recipes = Arrays.asList(FIVE_INGREDIENTS, twoMissing);

        List<RecipeMatcher.MatchResult> almost = RecipeMatcher.almostThere(recipes, pantry);

        assertEquals(1, almost.size());
        assertEquals("Chakalaka", almost.get(0).getRecipe().getName());
        assertTrue(RecipeMatcher.suggest(recipes, pantry).isEmpty());
    }

    @Test
    public void matchesSingularAgainstPlural() {
        Recipe salad = recipe("Salad", ing("Tomatoes", 2, "piece"));
        assertTrue(RecipeMatcher.check(salad,
                Collections.singletonList(item("tomato", 2, "piece"))).canMake());
    }

    @Test
    public void convertsBetweenUnits() {
        Recipe pap = recipe("Pap", ing("Maize meal", 250, "g"));
        assertTrue(RecipeMatcher.check(pap,
                Collections.singletonList(item("Mealie meal", 1, "kg"))).canMake());
        assertFalse(RecipeMatcher.check(pap,
                Collections.singletonList(item("Maize meal", 0.2, "kg"))).canMake());
    }

    @Test
    public void addsUpSeveralPantryEntriesForTheSameIngredient() {
        Recipe mash = recipe("Mash", ing("Milk", 300, "ml"));
        List<PantryItem> pantry = Arrays.asList(item("Milk", 200, "ml"), item("milk", 0.2, "l"));
        assertTrue(RecipeMatcher.check(mash, pantry).canMake());
    }

    @Test
    public void neverGuessesBetweenAMassAndAVolumeWithoutADensity() {
        Recipe sauce = recipe("Sauce", ing("Cream", 100, "ml"));
        RecipeMatcher.MatchResult result = RecipeMatcher.check(sauce,
                Collections.singletonList(item("Cream", 500, "g")));

        assertFalse(result.canMake());
        assertEquals(RecipeMatcher.Status.INCOMPATIBLE_UNITS,
                result.getChecks().get(0).getStatus());
    }

    @Test
    public void aSimilarButDifferentIngredientDoesNotCount() {
        Recipe smoothie = recipe("Smoothie", ing("Milk", 250, "ml"));
        assertFalse(RecipeMatcher.check(smoothie,
                Collections.singletonList(item("Coconut milk", 400, "ml"))).canMake());
    }

    @Test
    public void anEmptyPantryMatchesNothing() {
        assertTrue(RecipeMatcher.suggest(Collections.singletonList(FIVE_INGREDIENTS),
                Collections.emptyList()).isEmpty());
    }
}
