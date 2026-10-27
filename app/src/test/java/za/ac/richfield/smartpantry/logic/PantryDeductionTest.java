package za.ac.richfield.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;

/**
 * Tests for what cooking takes out of the pantry.
 *
 * <p>This is the only part of the application that changes the user's own
 * record of their kitchen without being told what to write, so the cases that
 * matter most are the ones where it should do nothing: an ingredient it cannot
 * find, and an ingredient it cannot honestly convert. Taking away a guess would
 * be worse than taking away nothing.
 */
public class PantryDeductionTest {

    private static Recipe recipe(String name, String ingredient, double quantity, String unit) {
        return new Recipe(1L, name, "", 10,
                Collections.singletonList(new RecipeIngredient(ingredient, quantity, unit)),
                Collections.singletonList("Cook it."));
    }

    private static PantryItem item(String name, double quantity, String unit) {
        return new PantryItem(1L, name, quantity, unit, null);
    }

    @Test
    public void subtractsInTheUnitTheUserEnteredNotTheRecipeUnit() {
        Recipe pap = recipe("Pap", "Maize Meal", 250, "g");
        List<PantryItem> pantry = Arrays.asList(item("Maize Meal", 2, "kg"));

        List<PantryDeduction.Change> changes = PantryDeduction.forRecipe(pap, pantry);

        assertEquals(1, changes.size());
        assertFalse(changes.get(0).isExhausted());
        // 2 kg less 250 g is 1.75 kg. The user wrote kg, so kg is what stays.
        assertEquals(1.75, changes.get(0).getNewQuantity(), 1e-9);
    }

    @Test
    public void marksAnIngredientExhaustedWhenTheRecipeUsesItAllUp() {
        Recipe omelette = recipe("Omelette", "Eggs", 3, "piece");
        List<PantryItem> pantry = Arrays.asList(item("Eggs", 3, "piece"));

        List<PantryDeduction.Change> changes = PantryDeduction.forRecipe(omelette, pantry);

        assertEquals(1, changes.size());
        assertTrue(changes.get(0).isExhausted());
    }

    @Test
    public void bridgesACountAgainstAMassTheSameWayTheRuleDoes() {
        // The rule lets "2 onions" satisfy "150 g onion" at 150 g each, so the
        // deduction has to come out of the same table or the two would disagree.
        Recipe soup = recipe("Soup", "Onions", 150, "g");
        List<PantryItem> pantry = Arrays.asList(item("Onions", 2, "piece"));

        List<PantryDeduction.Change> changes = PantryDeduction.forRecipe(soup, pantry);

        assertEquals(1, changes.size());
        assertEquals(1.0, changes.get(0).getNewQuantity(), 1e-9);
    }

    @Test
    public void leavesAnIngredientAloneWhenItIsNotInThePantry() {
        Recipe omelette = recipe("Omelette", "Cheese", 50, "g");
        List<PantryItem> pantry = Arrays.asList(item("Eggs", 6, "piece"));

        assertTrue(PantryDeduction.forRecipe(omelette, pantry).isEmpty());
    }

    @Test
    public void leavesAnIngredientAloneWhenTheUnitsCannotBeCompared() {
        // A mass against a volume, with no density to bridge them. The matcher
        // refuses to call this satisfied, so the deduction must refuse to
        // subtract it rather than invent a conversion.
        Recipe sauce = recipe("Sauce", "Cream", 100, "ml");
        List<PantryItem> pantry = Arrays.asList(item("Cream", 200, "g"));

        assertTrue(PantryDeduction.forRecipe(sauce, pantry).isEmpty());
    }

    @Test
    public void everyChangeCarriesTheItemAsItWasSoItCanBePutBack() {
        Recipe pap = recipe("Pap", "Maize Meal", 250, "g");
        List<PantryItem> pantry = Arrays.asList(item("Maize Meal", 2, "kg"));

        PantryDeduction.Change change = PantryDeduction.forRecipe(pap, pantry).get(0);

        assertEquals("Maize Meal", change.getBefore().getName());
        assertEquals(2.0, change.getBefore().getQuantity(), 1e-9);
        assertEquals("kg", change.getBefore().getUnit());
    }

    @Test
    public void roundsToTwoDecimalPlaces() {
        // An unrounded figure would be shown as one number and saved as another.
        Recipe bake = recipe("Bake", "Flour", 1, "tsp");
        List<PantryItem> pantry = Arrays.asList(item("Flour", 1, "cup"));

        double left = PantryDeduction.forRecipe(bake, pantry).get(0).getNewQuantity();

        assertEquals(0.98, left, 1e-9);
        assertEquals(left, Math.round(left * 100.0) / 100.0, 0.0);
    }

    @Test
    public void takesFromASecondEntryWhenTheFirstRunsOut() {
        Recipe mash = recipe("Mash", "Milk", 300, "ml");
        PantryItem first = new PantryItem(1L, "Milk", 200, "ml", null);
        PantryItem second = new PantryItem(2L, "Milk", 1, "l", null);

        List<PantryDeduction.Change> changes =
                PantryDeduction.forRecipe(mash, Arrays.asList(first, second));

        assertEquals(2, changes.size());
        assertTrue(changes.get(0).isExhausted());
        assertEquals(0.9, changes.get(1).getNewQuantity(), 1e-9);
    }
}
