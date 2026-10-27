package za.ac.richfield.smartpantry.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;

/**
 * Runs on a device or emulator against an in-memory SQLite database, so the
 * user's real pantry is never touched.
 */
@RunWith(AndroidJUnit4.class)
public class PantryDbHelperTest {

    private PantryDbHelper db;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = new PantryDbHelper(context, null);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void seedsTwentyRecipesWithIngredientsAndSteps() {
        List<Recipe> recipes = db.getAllRecipes();
        assertEquals(20, recipes.size());
        for (Recipe recipe : recipes) {
            assertTrue(recipe.getName(), recipe.getIngredients().size() >= 2);
            assertTrue(recipe.getName(), recipe.getSteps().size() >= 2);
        }
    }

    @Test
    public void createReadUpdateDeleteAPantryItem() {
        long id = db.insertPantryItem(new PantryItem(PantryItem.NO_ID, "Eggs", 6, "piece", "2026-12-01"));
        PantryItem saved = db.getPantryItem(id);
        assertNotNull(saved);
        assertEquals("Eggs", saved.getName());
        assertEquals("2026-12-01", saved.getExpiryDate());

        db.updatePantryItem(new PantryItem(id, "Eggs", 4, "piece", null));
        PantryItem updated = db.getPantryItem(id);
        assertEquals(4, updated.getQuantity(), 1e-9);
        assertNull(updated.getExpiryDate());

        assertEquals(1, db.getAllPantryItems().size());
        assertEquals(1, db.deletePantryItem(id));
        assertNull(db.getPantryItem(id));
    }

    @Test
    public void findsDuplicatesRegardlessOfPlural() {
        long id = db.insertPantryItem(new PantryItem(PantryItem.NO_ID, "Tomatoes", 4, "piece", null));
        assertNotNull(db.findPantryItemByName("tomato", PantryItem.NO_ID));
        // An item is not a duplicate of itself (editing it must still work).
        assertNull(db.findPantryItemByName("tomato", id));
    }
}
