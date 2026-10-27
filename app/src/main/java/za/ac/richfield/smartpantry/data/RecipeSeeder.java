package za.ac.richfield.smartpantry.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import za.ac.richfield.smartpantry.model.RecipeIngredient;

/**
 * Inserts the built-in recipe collection when the database is first created.
 *
 * <p>Water is never listed as an ingredient, because nobody records tap water
 * in their pantry; recipes that need it say so in the method instead.
 * Ingredient names are kept plain ("Tomato", "Egg") so they match the way
 * people type them; {@link za.ac.richfield.smartpantry.logic.IngredientNormalizer}
 * takes care of plurals and common synonyms.
 */
final class RecipeSeeder {

    private RecipeSeeder() {
    }

    static void seed(SQLiteDatabase db) {
        recipe(db, "Scrambled Eggs", "Soft, creamy eggs in ten minutes.", 10,
                new RecipeIngredient[]{
                        ing("Egg", 3, "piece"),
                        ing("Milk", 30, "ml"),
                        ing("Butter", 10, "g")},
                "Whisk the eggs and milk together in a bowl.",
                "Melt the butter in a pan over low heat.",
                "Add the eggs and stir slowly until just set.",
                "Serve straight away.");

        recipe(db, "Cheese Omelette", "A quick folded omelette with melted cheese.", 10,
                new RecipeIngredient[]{
                        ing("Egg", 3, "piece"),
                        ing("Cheese", 40, "g"),
                        ing("Butter", 10, "g")},
                "Beat the eggs well.",
                "Melt the butter in a pan and pour in the eggs.",
                "When the eggs are almost set, sprinkle the grated cheese on one half.",
                "Fold the omelette over and cook for one more minute.");

        recipe(db, "French Toast", "Stale bread turned into a sweet breakfast.", 15,
                new RecipeIngredient[]{
                        ing("Bread", 4, "slice"),
                        ing("Egg", 2, "piece"),
                        ing("Milk", 60, "ml"),
                        ing("Sugar", 1, "tbsp"),
                        ing("Butter", 20, "g")},
                "Whisk the eggs, milk and sugar in a shallow dish.",
                "Soak each slice of bread in the mixture for a few seconds per side.",
                "Fry the slices in butter until golden on both sides.");

        recipe(db, "Pancakes", "Thin, soft pancakes for breakfast or dessert.", 25,
                new RecipeIngredient[]{
                        ing("Flour", 1, "cup"),
                        ing("Egg", 1, "piece"),
                        ing("Milk", 250, "ml"),
                        ing("Sugar", 2, "tbsp"),
                        ing("Baking powder", 2, "tsp"),
                        ing("Butter", 20, "g")},
                "Mix the flour, sugar and baking powder in a bowl.",
                "Whisk in the egg and milk until the batter is smooth.",
                "Melt a little butter in a pan and pour in a ladle of batter.",
                "Cook until bubbles form, flip and cook the other side.");

        recipe(db, "Pap", "Stiff maize meal porridge, a South African staple.", 20,
                new RecipeIngredient[]{
                        ing("Maize meal", 250, "g"),
                        ing("Salt", 1, "tsp")},
                "Bring 750 ml of water and the salt to the boil.",
                "Slowly add the maize meal while stirring to avoid lumps.",
                "Cover and simmer on low heat for 15 minutes, stirring now and then.",
                "Serve with chakalaka or a stew.");

        recipe(db, "Chakalaka", "Spicy vegetable relish that uses up odds and ends.", 35,
                new RecipeIngredient[]{
                        ing("Onion", 1, "piece"),
                        ing("Green pepper", 1, "piece"),
                        ing("Carrot", 2, "piece"),
                        ing("Tomato", 2, "piece"),
                        ing("Baked beans", 410, "g"),
                        ing("Oil", 2, "tbsp"),
                        ing("Curry powder", 1, "tbsp")},
                "Chop the onion and green pepper, and grate the carrots.",
                "Fry the onion in oil until soft, then add the curry powder.",
                "Add the pepper, carrots and chopped tomatoes and cook for 10 minutes.",
                "Stir in the baked beans and simmer for 10 more minutes.");

        recipe(db, "Tomato Pasta", "Simple pasta in a fresh tomato and garlic sauce.", 20,
                new RecipeIngredient[]{
                        ing("Pasta", 250, "g"),
                        ing("Tomato", 4, "piece"),
                        ing("Garlic clove", 2, "piece"),
                        ing("Olive oil", 2, "tbsp"),
                        ing("Salt", 1, "tsp")},
                "Cook the pasta in salted boiling water until tender.",
                "Fry the crushed garlic in olive oil for one minute.",
                "Add the chopped tomatoes and cook down into a sauce.",
                "Toss the drained pasta in the sauce.");

        recipe(db, "Egg Fried Rice", "The best use for leftover rice.", 20,
                new RecipeIngredient[]{
                        ing("Rice", 200, "g"),
                        ing("Egg", 2, "piece"),
                        ing("Peas", 100, "g"),
                        ing("Soy sauce", 2, "tbsp"),
                        ing("Oil", 1, "tbsp"),
                        ing("Spring onion", 2, "piece")},
                "Cook the rice (or use leftover cooked rice) and let it cool.",
                "Scramble the eggs in hot oil and set them aside.",
                "Stir-fry the rice and peas for a few minutes.",
                "Add the eggs, soy sauce and sliced spring onion and toss together.");

        recipe(db, "Vegetable Soup", "A warming soup for tired vegetables.", 40,
                new RecipeIngredient[]{
                        ing("Potato", 2, "piece"),
                        ing("Carrot", 2, "piece"),
                        ing("Onion", 1, "piece"),
                        ing("Tomato", 2, "piece"),
                        ing("Stock cube", 1, "piece")},
                "Chop all the vegetables into small pieces.",
                "Dissolve the stock cube in 1 litre of boiling water.",
                "Add the vegetables and simmer for 30 minutes until soft.",
                "Blend or mash lightly and season to taste.");

        recipe(db, "Cheese Toastie", "A golden toasted cheese sandwich.", 10,
                new RecipeIngredient[]{
                        ing("Bread", 2, "slice"),
                        ing("Cheese", 40, "g"),
                        ing("Butter", 10, "g")},
                "Butter the outside of both slices of bread.",
                "Put the cheese between the slices, buttered sides out.",
                "Toast in a pan or sandwich press until golden and melted.");

        recipe(db, "Potato Wedges", "Crispy oven-baked wedges.", 40,
                new RecipeIngredient[]{
                        ing("Potato", 4, "piece"),
                        ing("Oil", 2, "tbsp"),
                        ing("Salt", 1, "tsp"),
                        ing("Paprika", 1, "tsp")},
                "Heat the oven to 200 °C.",
                "Cut the potatoes into wedges and toss with oil, salt and paprika.",
                "Spread on a tray and bake for 30 to 35 minutes, turning once.");

        recipe(db, "Banana Smoothie", "Rescue over-ripe bananas in five minutes.", 5,
                new RecipeIngredient[]{
                        ing("Banana", 2, "piece"),
                        ing("Milk", 250, "ml"),
                        ing("Yoghurt", 125, "g"),
                        ing("Honey", 1, "tbsp")},
                "Peel and slice the bananas.",
                "Blend everything until smooth.",
                "Serve cold.");

        recipe(db, "Mashed Potatoes", "Smooth, buttery mash.", 30,
                new RecipeIngredient[]{
                        ing("Potato", 4, "piece"),
                        ing("Butter", 30, "g"),
                        ing("Milk", 60, "ml"),
                        ing("Salt", 0.5, "tsp")},
                "Peel and cube the potatoes and boil them until soft.",
                "Drain well and mash.",
                "Beat in the butter, warm milk and salt until smooth.");

        recipe(db, "Tuna Pasta Salad", "A cold pasta salad for lunch boxes.", 20,
                new RecipeIngredient[]{
                        ing("Pasta", 200, "g"),
                        ing("Tuna", 170, "g"),
                        ing("Mayonnaise", 3, "tbsp"),
                        ing("Sweetcorn", 100, "g"),
                        ing("Cucumber", 1, "piece")},
                "Cook the pasta, drain and rinse under cold water.",
                "Drain the tuna and dice the cucumber.",
                "Mix everything together with the mayonnaise and chill.");

        recipe(db, "Spaghetti Bolognese", "A classic meat sauce over spaghetti.", 45,
                new RecipeIngredient[]{
                        ing("Spaghetti", 250, "g"),
                        ing("Beef mince", 500, "g"),
                        ing("Onion", 1, "piece"),
                        ing("Garlic clove", 2, "piece"),
                        ing("Tomato", 4, "piece"),
                        ing("Oil", 1, "tbsp")},
                "Fry the chopped onion and garlic in oil until soft.",
                "Add the mince and brown it, breaking it up as it cooks.",
                "Add the chopped tomatoes and simmer for 25 minutes.",
                "Cook the spaghetti and serve the sauce on top.");

        recipe(db, "Chicken Stir-Fry", "Fast, colourful stir-fry with rice.", 25,
                new RecipeIngredient[]{
                        ing("Chicken", 300, "g"),
                        ing("Green pepper", 1, "piece"),
                        ing("Carrot", 1, "piece"),
                        ing("Onion", 1, "piece"),
                        ing("Soy sauce", 3, "tbsp"),
                        ing("Rice", 200, "g"),
                        ing("Oil", 1, "tbsp")},
                "Cook the rice.",
                "Slice the chicken and vegetables into thin strips.",
                "Stir-fry the chicken in hot oil until cooked through.",
                "Add the vegetables and soy sauce and cook for 3 to 4 minutes.",
                "Serve over the rice.");

        recipe(db, "Greek Salad", "Fresh salad with feta and olives.", 10,
                new RecipeIngredient[]{
                        ing("Tomato", 3, "piece"),
                        ing("Cucumber", 1, "piece"),
                        ing("Feta cheese", 100, "g"),
                        ing("Olives", 50, "g"),
                        ing("Olive oil", 2, "tbsp")},
                "Cut the tomatoes and cucumber into chunks.",
                "Add the olives and crumble the feta on top.",
                "Drizzle with olive oil just before serving.");

        recipe(db, "Garlic Bread", "Crisp, buttery garlic bread.", 15,
                new RecipeIngredient[]{
                        ing("Bread", 6, "slice"),
                        ing("Butter", 50, "g"),
                        ing("Garlic clove", 3, "piece")},
                "Heat the oven to 180 °C.",
                "Mix the soft butter with the crushed garlic.",
                "Spread on the bread and bake for 8 to 10 minutes.");

        recipe(db, "Guacamole", "Creamy avocado dip.", 10,
                new RecipeIngredient[]{
                        ing("Avocado", 2, "piece"),
                        ing("Tomato", 1, "piece"),
                        ing("Onion", 0.5, "piece"),
                        ing("Lemon", 1, "piece"),
                        ing("Salt", 0.5, "tsp")},
                "Mash the avocado flesh in a bowl.",
                "Finely chop the tomato and onion and stir them in.",
                "Season with lemon juice and salt.");

        recipe(db, "Banana Bread", "Moist loaf made from over-ripe bananas.", 75,
                new RecipeIngredient[]{
                        ing("Banana", 3, "piece"),
                        ing("Flour", 2, "cup"),
                        ing("Sugar", 0.5, "cup"),
                        ing("Butter", 100, "g"),
                        ing("Egg", 2, "piece"),
                        ing("Baking powder", 2, "tsp")},
                "Heat the oven to 180 °C and grease a loaf tin.",
                "Mash the bananas and mix in the melted butter, sugar and eggs.",
                "Fold in the flour and baking powder.",
                "Bake for about 60 minutes, until a skewer comes out clean.");
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private static void recipe(SQLiteDatabase db, String name, String description,
                               int prepMinutes, RecipeIngredient[] ingredients, String... steps) {
        ContentValues recipe = new ContentValues();
        recipe.put(PantryDbHelper.COL_NAME, name);
        recipe.put(PantryDbHelper.COL_DESCRIPTION, description);
        recipe.put(PantryDbHelper.COL_PREP_MINUTES, prepMinutes);
        recipe.put(PantryDbHelper.COL_STEPS, String.join(PantryDbHelper.STEP_SEPARATOR, steps));
        long recipeId = db.insertOrThrow(PantryDbHelper.TABLE_RECIPES, null, recipe);

        for (RecipeIngredient ingredient : ingredients) {
            ContentValues row = new ContentValues();
            row.put(PantryDbHelper.COL_RECIPE_ID, recipeId);
            row.put(PantryDbHelper.COL_NAME, ingredient.getName());
            row.put(PantryDbHelper.COL_QUANTITY, ingredient.getQuantity());
            row.put(PantryDbHelper.COL_UNIT, ingredient.getUnit());
            db.insertOrThrow(PantryDbHelper.TABLE_RECIPE_INGREDIENTS, null, row);
        }
    }
}
