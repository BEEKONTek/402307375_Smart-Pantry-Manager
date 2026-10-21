package com.example.smartpantrymanager;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.VH> {
    public interface Listener { void onItem(Recipe r); }
    private final List<Recipe> data = new ArrayList<>();
    private final Listener listener;
    public RecipeAdapter(Listener l) { this.listener = l; }
    public void setData(List<Recipe> items) {
        data.clear(); data.addAll(items); notifyDataSetChanged();
    }
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_recipe, p, false));
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Recipe r = data.get(pos);
        h.name.setText(r.getName());
        h.itemView.setOnClickListener(v -> listener.onItem(r));
    }
    @Override public int getItemCount() { return data.size(); }
    static class VH extends RecyclerView.ViewHolder {
        TextView name;
        VH(@NonNull View v) { super(v); name = v.findViewById(R.id.tv_recipe_name); }
    }
}