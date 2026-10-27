package za.ac.richfield.smartpantry.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Tests for the name clean-up that makes "Tomatoes" match "tomato". */
public class IngredientNormalizerTest {

    @Test
    public void makesPluralsSingular() {
        assertEquals("tomato", IngredientNormalizer.normalize("Tomatoes"));
        assertEquals("potato", IngredientNormalizer.normalize("potatoes"));
        assertEquals("egg", IngredientNormalizer.normalize("Eggs"));
        assertEquals("berry", IngredientNormalizer.normalize("berries"));
        assertEquals("peach", IngredientNormalizer.normalize("Peaches"));
        assertEquals("olive", IngredientNormalizer.normalize("Olives"));
        assertEquals("leaf", IngredientNormalizer.normalize("leaves"));
    }

    @Test
    public void leavesWordsThatOnlyLookPluralAlone() {
        assertEquals("hummus", IngredientNormalizer.normalize("Hummus"));
        assertEquals("couscous", IngredientNormalizer.normalize("couscous"));
        assertEquals("rice", IngredientNormalizer.normalize("rice"));
    }

    @Test
    public void ignoresCaseSpacingAndPunctuation() {
        assertEquals("spring onion", IngredientNormalizer.normalize("  Spring-Onions! "));
    }

    @Test
    public void dropsDescriptiveWords() {
        assertEquals("egg", IngredientNormalizer.normalize("Large free-range eggs"));
        assertEquals("tomato", IngredientNormalizer.normalize("Fresh ripe tomatoes"));
    }

    @Test
    public void mapsSynonymsToOneName() {
        assertTrue(IngredientNormalizer.sameIngredient("Mealie meal", "Maize meal"));
        assertTrue(IngredientNormalizer.sameIngredient("Cheddar cheese", "Cheese"));
        assertTrue(IngredientNormalizer.sameIngredient("Ground beef", "Beef mince"));
        assertTrue(IngredientNormalizer.sameIngredient("Sunflower oil", "Oil"));
        assertTrue(IngredientNormalizer.sameIngredient("Garlic cloves", "Garlic clove"));
    }

    @Test
    public void keepsDifferentIngredientsApart() {
        assertFalse(IngredientNormalizer.sameIngredient("Milk", "Coconut milk"));
        assertFalse(IngredientNormalizer.sameIngredient("Butter", "Peanut butter"));
        assertFalse(IngredientNormalizer.sameIngredient("Onion", "Red onion"));
    }

    @Test
    public void blankNamesNeverMatch() {
        assertEquals("", IngredientNormalizer.normalize(null));
        assertFalse(IngredientNormalizer.sameIngredient("", ""));
    }
}
