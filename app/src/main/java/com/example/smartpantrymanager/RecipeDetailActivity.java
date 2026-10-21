package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_recipe_detail);
        long id = getIntent().getLongExtra("recipe_id", -1);
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        Recipe recipe = null;
        for (Recipe r : db.getAllRecipes()) if (r.getId() == id) { recipe = r; break; }
        if (recipe == null) { finish(); return; }
        ((TextView) findViewById(R.id.tv_title)).setText(recipe.getName());
        StringBuilder ings = new StringBuilder();
        for (RecipeIngredient ri : db.getIngredientsForRecipe(id)) {
            ings.append("• ").append(ri.getName()).append(" — ")
                    .append(ri.getQuantity()).append(" ").append(ri.getUnit()).append("\n");
        }
        ((TextView) findViewById(R.id.tv_ingredients)).setText(ings.toString());
        ((TextView) findViewById(R.id.tv_steps)).setText(recipe.getSteps());
    }
}