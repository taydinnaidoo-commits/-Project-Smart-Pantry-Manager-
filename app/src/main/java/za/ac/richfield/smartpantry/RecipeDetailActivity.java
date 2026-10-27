package za.ac.richfield.smartpantry;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import za.ac.richfield.smartpantry.logic.PantryDeduction;
import za.ac.richfield.smartpantry.logic.RecipeMatcher;
import za.ac.richfield.smartpantry.logic.RecipeMatcher.IngredientCheck;
import za.ac.richfield.smartpantry.logic.RecipeMatcher.MatchResult;
import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.model.Recipe;
import za.ac.richfield.smartpantry.model.RecipeIngredient;
import za.ac.richfield.smartpantry.util.QuantityFormat;

/**
 * Recipe Detail screen. Shows the full ingredient list (each marked as in the
 * pantry or not) and the method. When the user has everything, "Cook this
 * recipe" takes the used quantities out of the pantry.
 */
public class RecipeDetailActivity extends BaseActivity {

    /** Intent extra: id of the recipe to show. Required. */
    public static final String EXTRA_RECIPE_ID = "za.ac.richfield.smartpantry.EXTRA_RECIPE_ID";

    private long recipeId;
    private TextView statusView;
    private LinearLayout ingredientsContainer;
    private LinearLayout stepsContainer;
    private MaterialButton cookButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        setUpScreen(true);

        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        statusView = findViewById(R.id.recipe_status);
        ingredientsContainer = findViewById(R.id.ingredients_container);
        stepsContainer = findViewById(R.id.steps_container);
        cookButton = findViewById(R.id.cook_button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        Recipe recipe = db.getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, R.string.recipe_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        List<PantryItem> pantry = db.getAllPantryItems();
        MatchResult match = RecipeMatcher.check(recipe, pantry);

        setTitle(recipe.getName());
        TextView description = findViewById(R.id.recipe_description);
        description.setText(recipe.getDescription());
        TextView meta = findViewById(R.id.recipe_meta);
        int count = recipe.getIngredients().size();
        meta.setText(getResources().getQuantityString(
                R.plurals.recipe_meta, count, count, recipe.getPrepMinutes()));

        if (match.canMake()) {
            statusView.setText(R.string.detail_ready);
            statusView.setTextColor(MaterialColors.getColor(statusView,
                    androidx.appcompat.R.attr.colorPrimary));
        } else {
            int missing = match.getMissingCount();
            statusView.setText(getResources().getQuantityString(
                    R.plurals.detail_missing, missing, missing));
            statusView.setTextColor(MaterialColors.getColor(statusView,
                    com.google.android.material.R.attr.colorTertiary));
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        ingredientsContainer.removeAllViews();
        for (IngredientCheck check : match.getChecks()) {
            ingredientsContainer.addView(ingredientRow(inflater, check));
        }

        stepsContainer.removeAllViews();
        List<String> steps = recipe.getSteps();
        for (int i = 0; i < steps.size(); i++) {
            View row = inflater.inflate(R.layout.item_step, stepsContainer, false);
            ((TextView) row.findViewById(R.id.step_number)).setText(String.valueOf(i + 1));
            ((TextView) row.findViewById(R.id.step_text)).setText(steps.get(i));
            stepsContainer.addView(row);
        }

        cookButton.setEnabled(match.canMake());
        cookButton.setText(match.canMake() ? R.string.cook_button : R.string.cook_button_disabled);
        cookButton.setOnClickListener(v -> confirmCook(recipe, pantry));
    }

    private View ingredientRow(LayoutInflater inflater, IngredientCheck check) {
        View row = inflater.inflate(R.layout.item_ingredient, ingredientsContainer, false);
        RecipeIngredient ingredient = check.getIngredient();
        ImageView icon = row.findViewById(R.id.ingredient_icon);
        TextView name = row.findViewById(R.id.ingredient_name);
        TextView status = row.findViewById(R.id.ingredient_status);

        name.setText(getString(R.string.ingredient_line,
                QuantityFormat.amount(ingredient.getQuantity(), ingredient.getUnit()),
                ingredient.getName()));

        int colorAttr;
        switch (check.getStatus()) {
            case HAVE:
                icon.setImageResource(R.drawable.ic_check);
                status.setText(R.string.ingredient_have);
                colorAttr = androidx.appcompat.R.attr.colorPrimary;
                break;
            case NOT_ENOUGH:
                icon.setImageResource(R.drawable.ic_warning);
                status.setText(getString(R.string.ingredient_not_enough,
                        QuantityFormat.amount(check.getAvailable(), ingredient.getUnit())));
                colorAttr = com.google.android.material.R.attr.colorTertiary;
                break;
            case INCOMPATIBLE_UNITS:
                icon.setImageResource(R.drawable.ic_warning);
                status.setText(R.string.ingredient_incompatible);
                colorAttr = com.google.android.material.R.attr.colorTertiary;
                break;
            default:
                icon.setImageResource(R.drawable.ic_close);
                status.setText(R.string.ingredient_missing);
                colorAttr = androidx.appcompat.R.attr.colorError;
                break;
        }
        int color = MaterialColors.getColor(row, colorAttr);
        icon.setColorFilter(color);
        status.setTextColor(color);
        return row;
    }

    /** Shows exactly what will change in the pantry before changing it. */
    private void confirmCook(Recipe recipe, List<PantryItem> pantry) {
        List<PantryDeduction.Change> changes = PantryDeduction.forRecipe(recipe, pantry);
        StringBuilder summary = new StringBuilder();
        for (PantryDeduction.Change change : changes) {
            PantryItem before = change.getBefore();
            String line = change.isExhausted()
                    ? getString(R.string.cook_line_used_up, before.getName())
                    : getString(R.string.cook_line, before.getName(),
                    QuantityFormat.amount(before.getQuantity(), before.getUnit()),
                    QuantityFormat.amount(change.getNewQuantity(), before.getUnit()));
            summary.append(line).append('\n');
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.cook_confirm_title, recipe.getName()))
                .setMessage(summary.toString().trim())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.cook_confirm, (dialog, which) -> {
                    db.applyDeduction(changes);
                    Toast.makeText(this, R.string.cook_done, Toast.LENGTH_SHORT).show();
                    render();
                })
                .show();
    }
}
