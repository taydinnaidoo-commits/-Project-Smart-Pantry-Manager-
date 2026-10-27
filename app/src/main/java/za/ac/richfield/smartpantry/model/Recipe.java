package za.ac.richfield.smartpantry.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A recipe from the seeded collection: its ingredients and its method steps. */
public class Recipe {

    private final long id;
    private final String name;
    private final String description;
    private final int prepMinutes;
    private final List<RecipeIngredient> ingredients;
    private final List<String> steps;

    public Recipe(long id, String name, String description, int prepMinutes,
                  List<RecipeIngredient> ingredients, List<String> steps) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.prepMinutes = prepMinutes;
        this.ingredients = Collections.unmodifiableList(new ArrayList<>(ingredients));
        this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPrepMinutes() {
        return prepMinutes;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public List<String> getSteps() {
        return steps;
    }
}
