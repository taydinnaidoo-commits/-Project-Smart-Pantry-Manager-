package za.ac.richfield.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import za.ac.richfield.smartpantry.logic.IngredientNormalizer;
import za.ac.richfield.smartpantry.logic.PantryDeduction;
import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;

/**
 * The app's on-device SQLite database.
 *
 * <p>Three tables:
 * <pre>
 * pantry_items        (_id, name, quantity, unit, expiry_date)
 * recipes             (_id, name, description, prep_minutes, steps)
 * recipe_ingredients  (_id, recipe_id -> recipes._id, name, quantity, unit)
 * </pre>
 * The 20 recipes are inserted by {@link RecipeSeeder} the first time the
 * database is created. Pantry items are the user's own data and get full CRUD.
 *
 * <p>There is one shared instance ({@link #getInstance}) so every screen talks
 * to the same open connection.
 */
public class PantryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // pantry_items
    static final String TABLE_PANTRY = "pantry_items";
    static final String COL_ID = "_id";
    static final String COL_NAME = "name";
    static final String COL_QUANTITY = "quantity";
    static final String COL_UNIT = "unit";
    static final String COL_EXPIRY = "expiry_date";

    // recipes
    static final String TABLE_RECIPES = "recipes";
    static final String COL_DESCRIPTION = "description";
    static final String COL_PREP_MINUTES = "prep_minutes";
    static final String COL_STEPS = "steps";

    // recipe_ingredients
    static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    static final String COL_RECIPE_ID = "recipe_id";

    /** Steps are stored in one TEXT column, one step per line. */
    static final String STEP_SEPARATOR = "\n";

    private static PantryDbHelper instance;

    /** The shared helper. Uses the application context so no Activity is leaked. */
    public static synchronized PantryDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PantryDbHelper(context.getApplicationContext(), DATABASE_NAME);
        }
        return instance;
    }

    /**
     * @param name database file name, or null for an in-memory database
     *             (used by the instrumented tests so they never touch real data)
     */
    PantryDbHelper(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Needed for ON DELETE CASCADE on recipe_ingredients.
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL CHECK (" + COL_QUANTITY + " >= 0), "
                + COL_UNIT + " TEXT NOT NULL, "
                + COL_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL UNIQUE, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_PREP_MINUTES + " INTEGER NOT NULL, "
                + COL_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_ID + " INTEGER NOT NULL REFERENCES "
                + TABLE_RECIPES + "(" + COL_ID + ") ON DELETE CASCADE, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL)");

        RecipeSeeder.seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Only version 1 exists so far. A future version would migrate here
        // with ALTER TABLE so the user's pantry is not lost.
    }

    // ------------------------------------------------------------------
    // Pantry items: Create, Read, Update, Delete
    // ------------------------------------------------------------------

    /** CREATE: saves a new pantry item and returns its new id. */
    public long insertPantryItem(PantryItem item) {
        return getWritableDatabase().insert(TABLE_PANTRY, null, toValues(item));
    }

    /** READ: every pantry item, sorted by name. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query(TABLE_PANTRY, null, null, null,
                null, null, COL_NAME + " COLLATE NOCASE")) {
            while (cursor.moveToNext()) {
                items.add(readPantryItem(cursor));
            }
        }
        return items;
    }

    /** READ: one pantry item by id, or null if it no longer exists. */
    public PantryItem getPantryItem(long id) {
        try (Cursor cursor = getReadableDatabase().query(TABLE_PANTRY, null,
                COL_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null)) {
            return cursor.moveToFirst() ? readPantryItem(cursor) : null;
        }
    }

    /** UPDATE: saves changes to an existing item. Returns the rows changed (0 or 1). */
    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(TABLE_PANTRY, toValues(item),
                COL_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    /** DELETE: removes one item. Returns the rows deleted (0 or 1). */
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(TABLE_PANTRY,
                COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /** DELETE: empties the pantry (from the Settings screen). */
    public void deleteAllPantryItems() {
        getWritableDatabase().delete(TABLE_PANTRY, null, null);
    }

    /**
     * Finds another pantry item that is the same ingredient as {@code name}
     * ("Tomatoes" and "tomato" count as the same), ignoring {@code excludeId}.
     * Used by the form so the user edits an existing item instead of adding a
     * duplicate.
     */
    public PantryItem findPantryItemByName(String name, long excludeId) {
        for (PantryItem item : getAllPantryItems()) {
            if (item.getId() != excludeId
                    && IngredientNormalizer.sameIngredient(item.getName(), name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Applies the result of cooking a recipe in one transaction: used-up
     * items are deleted, the rest get their new quantity.
     */
    public void applyDeduction(List<PantryDeduction.Change> changes) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (PantryDeduction.Change change : changes) {
                PantryItem before = change.getBefore();
                if (change.isExhausted()) {
                    deletePantryItem(before.getId());
                } else {
                    updatePantryItem(before.withQuantity(change.getNewQuantity()));
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private static ContentValues toValues(PantryItem item) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName().trim());
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_EXPIRY, item.getExpiryDate());
        return values;
    }

    private static PantryItem readPantryItem(Cursor cursor) {
        int expiryIndex = cursor.getColumnIndexOrThrow(COL_EXPIRY);
        return new PantryItem(
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT)),
                cursor.isNull(expiryIndex) ? null : cursor.getString(expiryIndex));
    }

    // ------------------------------------------------------------------
    // Recipes (read-only: they are seeded on first run)
    // ------------------------------------------------------------------

    /** Every recipe with its ingredients, sorted by name. */
    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();

        // Load all ingredient rows once and group them by recipe, rather than
        // running one query per recipe.
        Map<Long, List<RecipeIngredient>> ingredientsByRecipe = new LinkedHashMap<>();
        try (Cursor cursor = db.query(TABLE_RECIPE_INGREDIENTS, null, null, null,
                null, null, COL_RECIPE_ID + ", " + COL_ID)) {
            while (cursor.moveToNext()) {
                long recipeId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_RECIPE_ID));
                List<RecipeIngredient> list = ingredientsByRecipe.get(recipeId);
                if (list == null) {
                    list = new ArrayList<>();
                    ingredientsByRecipe.put(recipeId, list);
                }
                list.add(readIngredient(cursor));
            }
        }

        List<Recipe> recipes = new ArrayList<>();
        try (Cursor cursor = db.query(TABLE_RECIPES, null, null, null,
                null, null, COL_NAME + " COLLATE NOCASE")) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID));
                List<RecipeIngredient> ingredients = ingredientsByRecipe.get(id);
                recipes.add(readRecipe(cursor,
                        ingredients == null ? new ArrayList<>() : ingredients));
            }
        }
        return recipes;
    }

    /** One recipe with its ingredients, or null if the id is unknown. */
    public Recipe getRecipe(long recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        String[] args = {String.valueOf(recipeId)};

        List<RecipeIngredient> ingredients = new ArrayList<>();
        try (Cursor cursor = db.query(TABLE_RECIPE_INGREDIENTS, null,
                COL_RECIPE_ID + " = ?", args, null, null, COL_ID)) {
            while (cursor.moveToNext()) {
                ingredients.add(readIngredient(cursor));
            }
        }
        try (Cursor cursor = db.query(TABLE_RECIPES, null, COL_ID + " = ?", args,
                null, null, null)) {
            return cursor.moveToFirst() ? readRecipe(cursor, ingredients) : null;
        }
    }

    public long getRecipeCount() {
        return DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE_RECIPES);
    }

    private static RecipeIngredient readIngredient(Cursor cursor) {
        return new RecipeIngredient(
                cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QUANTITY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT)));
    }

    private static Recipe readRecipe(Cursor cursor, List<RecipeIngredient> ingredients) {
        String steps = cursor.getString(cursor.getColumnIndexOrThrow(COL_STEPS));
        return new Recipe(
                cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_PREP_MINUTES)),
                ingredients,
                Arrays.asList(steps.split(STEP_SEPARATOR)));
    }
}
