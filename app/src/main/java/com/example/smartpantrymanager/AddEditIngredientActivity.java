package com.example.smartpantrymanager;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class AddEditIngredientActivity extends AppCompatActivity {
    private EditText etName, etQty, etUnit, etExpiry;
    private DatabaseHelper db; private long editingId = -1;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_edit);
        db = DatabaseHelper.getInstance(this);
        etName   = findViewById(R.id.et_name);
        etQty    = findViewById(R.id.et_qty);
        etUnit   = findViewById(R.id.et_unit);
        etExpiry = findViewById(R.id.et_expiry);
        editingId = getIntent().getLongExtra("item_id", -1);
        if (editingId != -1) {
            PantryItem p = db.getPantryItem(editingId);
            if (p != null) {
                etName.setText(p.getName());
                etQty.setText(String.valueOf(p.getQuantity()));
                etUnit.setText(p.getUnit());
                etExpiry.setText(p.getExpiryDate());
                setTitle("Edit Ingredient");
            }
        } else setTitle("Add Ingredient");
        findViewById(R.id.btn_save).setOnClickListener(v -> save());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
    }
    private void save() {
        String name = etName.getText().toString().trim();
        String qtyStr = etQty.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();
        if (TextUtils.isEmpty(name)) { etName.setError("Name required"); etName.requestFocus(); return; }
        if (TextUtils.isEmpty(qtyStr)) { etQty.setError("Quantity required"); etQty.requestFocus(); return; }
        double qty;
        try { qty = Double.parseDouble(qtyStr); }
        catch (NumberFormatException e) { etQty.setError("Invalid number"); etQty.requestFocus(); return; }
        if (qty <= 0) { etQty.setError("Must be > 0"); etQty.requestFocus(); return; }
        if (TextUtils.isEmpty(unit)) { etUnit.setError("Unit required"); etUnit.requestFocus(); return; }
        if (!TextUtils.isEmpty(expiry) && !expiry.matches("\\d{4}-\\d{2}-\\d{2}")) {
            etExpiry.setError("Use yyyy-MM-dd"); etExpiry.requestFocus(); return;
        }
        PantryItem item = new PantryItem(editingId, name, qty, unit,
                TextUtils.isEmpty(expiry) ? null : expiry);
        if (editingId == -1) db.insertPantryItem(item);
        else db.updatePantryItem(item);
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}