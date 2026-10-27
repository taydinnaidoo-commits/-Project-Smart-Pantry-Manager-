package za.ac.richfield.smartpantry;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

import za.ac.richfield.smartpantry.adapter.RecipeAdapter;
import za.ac.richfield.smartpantry.data.AppPreferences;
import za.ac.richfield.smartpantry.logic.RecipeMatcher;
import za.ac.richfield.smartpantry.logic.RecipeMatcher.MatchResult;
import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;

/**
 * Suggested Recipes screen. Runs the strict-matching rule against the current
 * pantry and lists ONLY the recipes the user can cook right now. Below that,
 * in its own labelled section, it lists recipes that are one ingredient short
 * ("Almost there"), unless that is switched off in Settings.
 *
 * <p>A toggle at the top switches to browsing the whole recipe collection,
 * with each recipe's status shown.
 */
public class SuggestedRecipesActivity extends BaseActivity implements RecipeAdapter.Listener {

    private static final String STATE_SHOW_ALL = "show_all";

    private RecipeAdapter adapter;
    private boolean showAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        setUpScreen(false);
        setUpBottomNavigation(R.id.nav_recipes);

        RecyclerView list = findViewById(R.id.recipe_list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this);
        list.setAdapter(adapter);

        if (savedInstanceState != null) {
            showAll = savedInstanceState.getBoolean(STATE_SHOW_ALL);
        }
        MaterialButtonToggleGroup modeToggle = findViewById(R.id.mode_toggle);
        modeToggle.check(showAll ? R.id.mode_all : R.id.mode_suggested);
        modeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                showAll = checkedId == R.id.mode_all;
                loadRecipes();
            }
        });
    }

    /** Re-run the match every time the screen is shown: the pantry may have changed. */
    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_SHOW_ALL, showAll);
    }

    private void loadRecipes() {
        List<Recipe> recipes = db.getAllRecipes();
        List<PantryItem> pantry = db.getAllPantryItems();
        List<MatchResult> suggested = RecipeMatcher.suggest(recipes, pantry);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(getString(R.string.recipes_subtitle,
                    suggested.size(), recipes.size()));
        }

        adapter.setRows(showAll
                ? buildAllRows(recipes, pantry)
                : buildSuggestedRows(suggested, recipes, pantry));
    }

    private List<RecipeAdapter.Row> buildSuggestedRows(List<MatchResult> suggested,
                                                       List<Recipe> recipes,
                                                       List<PantryItem> pantry) {
        List<RecipeAdapter.Row> rows = new ArrayList<>();
        rows.add(RecipeAdapter.Row.header(getString(R.string.section_ready, suggested.size())));
        if (suggested.isEmpty()) {
            rows.add(RecipeAdapter.Row.empty(getString(pantry.isEmpty()
                    ? R.string.no_matches_empty_pantry
                    : R.string.no_matches)));
        }
        for (MatchResult match : suggested) {
            rows.add(RecipeAdapter.Row.recipe(match.getRecipe(),
                    getString(R.string.status_ready), true));
        }

        // The optional bonus list, kept clearly apart from the strict list.
        if (new AppPreferences(this).isShowAlmostThere()) {
            List<MatchResult> almost = RecipeMatcher.almostThere(recipes, pantry);
            if (!almost.isEmpty()) {
                rows.add(RecipeAdapter.Row.header(
                        getString(R.string.section_almost_there, almost.size())));
                for (MatchResult match : almost) {
                    rows.add(RecipeAdapter.Row.recipe(match.getRecipe(),
                            missingText(match), false));
                }
            }
        }
        return rows;
    }

    private List<RecipeAdapter.Row> buildAllRows(List<Recipe> recipes, List<PantryItem> pantry) {
        List<RecipeAdapter.Row> rows = new ArrayList<>();
        rows.add(RecipeAdapter.Row.header(getString(R.string.section_all, recipes.size())));
        for (Recipe recipe : recipes) {
            MatchResult match = RecipeMatcher.check(recipe, pantry);
            rows.add(match.canMake()
                    ? RecipeAdapter.Row.recipe(recipe, getString(R.string.status_ready), true)
                    : RecipeAdapter.Row.recipe(recipe, missingText(match), false));
        }
        return rows;
    }

    /** "Missing: Butter, Sugar" */
    private String missingText(MatchResult match) {
        List<String> names = new ArrayList<>();
        for (RecipeIngredient ingredient : match.getMissingIngredients()) {
            names.add(ingredient.getName());
        }
        return getString(R.string.status_missing, String.join(", ", names));
    }

    /** Opens the detail screen, passing the recipe id in the Intent. */
    @Override
    public void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
