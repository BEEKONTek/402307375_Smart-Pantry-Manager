package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import java.util.List;

public class PantryFragment extends Fragment {
    private RecyclerView rv; private TextView empty;
    private PantryAdapter adapter; private DatabaseHelper db;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c,
                             @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_pantry, c, false);
        db = DatabaseHelper.getInstance(requireContext());
        rv = v.findViewById(R.id.rv_pantry);
        empty = v.findViewById(R.id.tv_empty);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PantryAdapter(this::openEdit, this::confirmDelete);
        rv.setAdapter(adapter);
        v.findViewById(R.id.fab_add).setOnClickListener(x ->
                startActivity(new Intent(requireContext(), AddEditIngredientActivity.class)));
        return v;
    }
    @Override public void onResume() { super.onResume(); refresh(); }
    private void refresh() {
        List<PantryItem> items = db.getAllPantryItems();
        adapter.setData(items);
        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        rv.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
    }
    private void openEdit(PantryItem item) {
        Intent i = new Intent(requireContext(), AddEditIngredientActivity.class);
        i.putExtra("item_id", item.getId());
        startActivity(i);
    }
    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete " + item.getName() + "?")
                .setMessage("This will remove the item from your pantry.")
                .setPositiveButton("Delete", (d,w) -> {
                    db.deletePantryItem(item.getId()); refresh();
                })
                .setNegativeButton("Cancel", null).show();
    }
}