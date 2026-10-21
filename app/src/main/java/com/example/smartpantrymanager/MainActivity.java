package com.example.smartpantrymanager;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView nav;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_main);
        nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) { swap(new PantryFragment()); return true; }
            if (id == R.id.nav_recipes) { swap(new SuggestedRecipesFragment()); return true; }
            if (id == R.id.nav_settings) { swap(new SettingsFragment()); return true; }
            return false;
        });
        if (s == null) nav.setSelectedItemId(R.id.nav_pantry);
    }
    private void swap(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f).commit();
    }
}