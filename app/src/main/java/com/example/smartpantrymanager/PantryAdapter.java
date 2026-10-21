package com.example.smartpantrymanager;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.VH> {
    public interface Listener { void onItem(PantryItem item); }
    private final List<PantryItem> data = new ArrayList<>();
    private final Listener onEdit, onDelete;
    public PantryAdapter(Listener onEdit, Listener onDelete) {
        this.onEdit = onEdit; this.onDelete = onDelete;
    }
    public void setData(List<PantryItem> items) {
        data.clear(); data.addAll(items); notifyDataSetChanged();
    }
    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_pantry, p, false));
    }
    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        PantryItem it = data.get(pos);
        h.name.setText(it.getName());
        h.qty.setText(String.format("%s %s", trim(it.getQuantity()), it.getUnit()));
        h.expiry.setText(it.getExpiryDate()==null||it.getExpiryDate().isEmpty()
                ? "" : "Expires " + it.getExpiryDate());
        h.itemView.setOnClickListener(v -> onEdit.onItem(it));
        h.btnDelete.setOnClickListener(v -> onDelete.onItem(it));
    }
    private static String trim(double d) {
        return d == Math.floor(d) ? String.valueOf((long)d) : String.valueOf(d);
    }
    @Override public int getItemCount() { return data.size(); }
    static class VH extends RecyclerView.ViewHolder {
        TextView name, qty, expiry, btnDelete;
        VH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            qty = v.findViewById(R.id.tv_qty);
            expiry = v.findViewById(R.id.tv_expiry);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}