package com.example.smartpantrymanager;

import android.content.*;
import android.os.Bundle;
import android.view.*;
import android.widget.Switch;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    private static final String PREFS = "settings";
    private static final String KEY = "expiry_alerts";
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c,
                             @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_settings, c, false);
        SharedPreferences p = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Switch sw = v.findViewById(R.id.switch_alerts);
        sw.setChecked(p.getBoolean(KEY, true));
        sw.setOnCheckedChangeListener((b, ch) -> p.edit().putBoolean(KEY, ch).apply());
        return v;
    }
}