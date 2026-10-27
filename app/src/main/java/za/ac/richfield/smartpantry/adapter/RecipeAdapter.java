package za.ac.richfield.smartpantry.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;

import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.model.Recipe;

/**
 * Custom RecyclerView adapter for the Recipes screen. It shows three kinds of
 * row so the strict suggestions and the "Almost there" recipes can be kept in
 * clearly separate, labelled sections:
 * <ul>
 *     <li>{@link Row#header} - a section title;</li>
 *     <li>{@link Row#empty} - a message when a section has nothing in it;</li>
 *     <li>{@link Row#recipe} - a tappable recipe card.</li>
 * </ul>
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_EMPTY = 1;
    private static final int TYPE_RECIPE = 2;

    public interface Listener {
        void onRecipeClicked(Recipe recipe);
    }

    /** One row of the list. Built by the Activity, displayed by the adapter. */
    public static final class Row {
        final int type;
        final String text;
        final Recipe recipe;
        final boolean ready;

        private Row(int type, String text, Recipe recipe, boolean ready) {
            this.type = type;
            this.text = text;
            this.recipe = recipe;
            this.ready = ready;
        }

        public static Row header(String title) {
            return new Row(TYPE_HEADER, title, null, false);
        }

        public static Row empty(String message) {
            return new Row(TYPE_EMPTY, message, null, false);
        }

        /**
         * @param status a line under the name, e.g. "Missing: Butter"
         * @param ready  true if the user can cook it now (status shown in green)
         */
        public static Row recipe(Recipe recipe, String status, boolean ready) {
            return new Row(TYPE_RECIPE, status, recipe, ready);
        }
    }

    private final Listener listener;
    private final List<Row> rows = new ArrayList<>();

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setRows(List<Row> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new TextHolder(inflater.inflate(R.layout.item_section_header, parent, false));
        }
        if (viewType == TYPE_EMPTY) {
            return new TextHolder(inflater.inflate(R.layout.item_empty_state, parent, false));
        }
        return new RecipeHolder(inflater.inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof TextHolder) {
            ((TextHolder) holder).text.setText(row.text);
            return;
        }

        RecipeHolder recipeHolder = (RecipeHolder) holder;
        Recipe recipe = row.recipe;
        int ingredientCount = recipe.getIngredients().size();
        recipeHolder.name.setText(recipe.getName());
        recipeHolder.meta.setText(holder.itemView.getResources().getQuantityString(
                R.plurals.recipe_meta, ingredientCount, ingredientCount, recipe.getPrepMinutes()));
        recipeHolder.status.setText(row.text);
        int colorAttr = row.ready
                ? androidx.appcompat.R.attr.colorPrimary
                : com.google.android.material.R.attr.colorTertiary;
        recipeHolder.status.setTextColor(MaterialColors.getColor(recipeHolder.status, colorAttr));
        holder.itemView.setOnClickListener(v -> listener.onRecipeClicked(recipe));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class TextHolder extends RecyclerView.ViewHolder {
        final TextView text;

        TextHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.row_text);
        }
    }

    static class RecipeHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView meta;
        final TextView status;

        RecipeHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.recipe_name);
            meta = itemView.findViewById(R.id.recipe_meta);
            status = itemView.findViewById(R.id.recipe_status);
        }
    }
}
