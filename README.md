# Smart Pantry Manager

A Java Android app that helps cut food waste. You record the ingredients you
actually have at home, and the app suggests recipes you can cook **using only
those ingredients**: no shopping trip needed, and no recipe is suggested unless
you really have everything it needs.

Built for Mobile App Development 700 (Richfield Graduate Institute of Technology).

## Features

| Screen | What it does |
| --- | --- |
| **Pantry** (launcher) | RecyclerView of every pantry item with quantity and expiry status. A banner warns about items that are expired or about to expire. Tap an item to edit it, tap the bin to delete it (with Undo). |
| **Add / Edit ingredient** | Form for name, quantity, unit and an optional expiry date. Every field is validated (required, numeric, more than 0, no duplicates such as "Tomato" vs "Tomatoes"). |
| **Recipes** | Runs the strict-matching rule and lists only the recipes you can cook right now. A separate **Almost there** section lists recipes missing exactly one ingredient. A toggle switches to browsing all 20 recipes. If nothing matches, it says so instead of showing a blank screen. |
| **Recipe detail** | Full ingredient list, each marked *in your pantry*, *not enough* or *not in your pantry*, plus the method. **Cook this recipe** takes the used quantities out of the pantry. |
| **Settings** | Turn expiry highlighting on or off, choose how many days ahead to warn, show or hide "Almost there", load a sample pantry, or clear the pantry. |

Navigation uses a bottom navigation bar (Pantry / Recipes / Settings) and the
toolbar back arrow. Screens are separate Activities started with explicit
Intents; item and recipe ids are passed as Intent extras.

## The strict-matching rule

`logic/RecipeMatcher.java` suggests a recipe only if **every** ingredient is in
the pantry in **at least** the required quantity. For each recipe ingredient it:

1. finds pantry items with the same name after normalisation
   (`IngredientNormalizer`): lower-case, singular ("tomatoes" → "tomato"),
   descriptive words dropped ("large free-range eggs" → "egg"), and common
   synonyms joined ("mealie meal" = "maize meal");
2. converts each item into the recipe's unit (`UnitConverter`): kg/g,
   l/ml/cup/tbsp/tsp, plus pieces ↔ grams for common produce
   (2 onions ≈ 300 g) and spoons ↔ grams for common staples (1 tsp salt ≈ 6 g);
3. adds them up and compares the total with what the recipe needs.

If even one ingredient is missing, short, or in a unit that cannot honestly be
compared (e.g. grams of cream against millilitres, where no density is known),
the recipe is **not** suggested. There is no fuzzy or partial matching, so
"coconut milk" never counts as "milk".

## Database: SQLite (on-device)

Data is stored with **SQLite** through `SQLiteOpenHelper` (`data/PantryDbHelper.java`).

```
pantry_items        (_id PK, name, quantity, unit, expiry_date)
recipes             (_id PK, name UNIQUE, description, prep_minutes, steps)
recipe_ingredients  (_id PK, recipe_id FK -> recipes._id ON DELETE CASCADE,
                     name, quantity, unit)
```

The 20 recipes are seeded on first run (`data/RecipeSeeder.java`). Pantry items
have full CRUD, and settings are kept in SharedPreferences.

**Why SQLite:**

- A pantry belongs to one person and one phone, so there is nothing to sync
  between users and no need for a server.
- It works fully offline, which matters in a kitchen with poor signal or no data.
- It is built into Android: no accounts, API keys, hosting or extra libraries,
  so the app runs on any emulator straight after cloning.
- The data is relational (a recipe has many ingredients), which suits SQL
  tables with a foreign key.
- It is the persistence approach covered in the module.

Data survives closing and reopening the app because it is written to the
`smart_pantry.db` file in the app's private storage.

## Setup and run

**Requirements:** Android Studio (a recent stable version), with the Android SDK
installed through its SDK Manager.

1. Clone the repository:
   ```
   git clone https://github.com/taydinnaidoo-commits/-Project-Smart-Pantry-Manager-.git
   ```
2. In Android Studio choose **File > Open** and select the cloned folder.
3. Let Gradle sync finish. Android Studio downloads the Gradle version, the JDK
   and the SDK platform the project needs.
4. Create or start an emulator (**Device Manager**), or plug in a phone with
   USB debugging on.
5. Press **Run ▶** on the `app` configuration.

To try it quickly, open **Settings > Load sample pantry**, then go to **Recipes**.

### Tests

- Unit tests for the matching rule, name normalisation, unit conversion and
  cooking deductions: `./gradlew testDebugUnitTest`
  (or right-click `app/src/test` > *Run tests*).
- Database tests (need an emulator or device): `./gradlew connectedDebugAndroidTest`

## Project structure

```
app/src/main/java/za/ac/richfield/smartpantry/
├── MainActivity.java              Pantry list (launcher)
├── AddEditItemActivity.java       Add / edit / delete form with validation
├── SuggestedRecipesActivity.java  Strict suggestions + "Almost there"
├── RecipeDetailActivity.java      Ingredients, method, "Cook this recipe"
├── SettingsActivity.java          Preferences and pantry data tools
├── BaseActivity.java              Toolbar, insets and bottom navigation
├── adapter/                       PantryAdapter, RecipeAdapter (RecyclerView)
├── data/                          PantryDbHelper, RecipeSeeder, AppPreferences, SampleData
├── logic/                         RecipeMatcher, IngredientNormalizer, UnitConverter, PantryDeduction
├── model/                         PantryItem, Recipe, RecipeIngredient
└── util/                          DateUtils, QuantityFormat
```

## Scope

The app does not use Google Maps, any mapping SDK, GPS or location, payments,
or the network. It asks for no permissions.
