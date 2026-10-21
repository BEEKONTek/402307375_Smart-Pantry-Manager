package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import java.util.List;

public class SuggestedRecipesFragment extends Fragment {
    private RecyclerView rvStrict, rvAlmost;
    private TextView tvEmpty, tvAlmostHeader;
    private RecipeAdapter strictAdapter, almostAdapter;
    private DatabaseHelper db;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c,
                             @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_suggested, c, false);
        db = DatabaseHelper.getInstance(requireContext());
        rvStrict = v.findViewById(R.id.rv_strict);
        rvAlmost = v.findViewById(R.id.rv_almost);
        tvEmpty = v.findViewById(R.id.tv_empty);
        tvAlmostHeader = v.findViewById(R.id.tv_almost_header);
        rvStrict.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAlmost.setLayoutManager(new LinearLayoutManager(requireContext()));
        strictAdapter = new RecipeAdapter(this::openDetail);
        almostAdapter = new RecipeAdapter(this::openDetail);
        rvStrict.setAdapter(strictAdapter);
        rvAlmost.setAdapter(almostAdapter);
        return v;
    }
    @Override public void onResume() { super.onResume(); refresh(); }
    private void refresh() {
        List<Recipe> all = db.getAllRecipes();
        List<PantryItem> pantry = db.getAllPantryItems();
        List<Recipe> strict = IngredientMatcher.strictMatches(all, pantry, db);
        List<Recipe> almost = IngredientMatcher.almostThere(all, pantry, db);
        strictAdapter.setData(strict);
        almostAdapter.setData(almost);
        tvEmpty.setVisibility(strict.isEmpty() ? View.VISIBLE : View.GONE);
        rvStrict.setVisibility(strict.isEmpty() ? View.GONE : View.VISIBLE);
        tvAlmostHeader.setVisibility(almost.isEmpty() ? View.GONE : View.VISIBLE);
        rvAlmost.setVisibility(almost.isEmpty() ? View.GONE : View.VISIBLE);
    }
    private void openDetail(Recipe r) {
        Intent i = new Intent(requireContext(), RecipeDetailActivity.class);
        i.putExtra("recipe_id", r.getId());
        startActivity(i);
    }
}