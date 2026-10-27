package za.ac.richfield.smartpantry.data;

import java.util.Arrays;
import java.util.List;

import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.util.DateUtils;

/**
 * A ready-made pantry the user can load from Settings to try the app quickly.
 * It is chosen so that a few recipes match exactly and a few are one
 * ingredient short, and it mixes units ("Potatoes 1 kg" against recipes that
 * ask for 4 potatoes) and spellings ("Mealie meal" for "Maize meal").
 */
public final class SampleData {

    private SampleData() {
    }

    public static List<PantryItem> samplePantry() {
        return Arrays.asList(
                item("Eggs", 6, "piece", DateUtils.daysFromToday(10)),
                item("Milk", 1, "l", DateUtils.daysFromToday(2)),
                item("Butter", 250, "g", null),
                item("Bread", 10, "slice", DateUtils.daysFromToday(4)),
                item("Cheddar cheese", 200, "g", DateUtils.daysFromToday(14)),
                item("Tomatoes", 4, "piece", DateUtils.daysFromToday(5)),
                item("Onions", 3, "piece", null),
                item("Potatoes", 1, "kg", null),
                item("Mealie meal", 2, "kg", null),
                item("Salt", 500, "g", null),
                item("Sunflower oil", 750, "ml", null));
    }

    private static PantryItem item(String name, double quantity, String unit, String expiry) {
        return new PantryItem(PantryItem.NO_ID, name, quantity, unit, expiry);
    }
}
