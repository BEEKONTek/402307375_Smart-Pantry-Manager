package com.example.smartpantrymanager;

import java.util.*;
import java.util.Locale;

public final class IngredientMatcher {
    private IngredientMatcher() {}

    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ").trim();
        if (s.endsWith("ies") && s.length() > 4) return s.substring(0, s.length() - 3) + "y";
        if (s.endsWith("oes") && s.length() > 4) return s.substring(0, s.length() - 2);
        if (s.endsWith("ches") || s.endsWith("shes") || s.endsWith("xes"))
            return s.substring(0, s.length() - 2);
        if (s.endsWith("sses")) return s.substring(0, s.length() - 2);
        if (s.endsWith("s") && !s.endsWith("ss") && !s.endsWith("us") && s.length() > 2)
            return s.substring(0, s.length() - 1);
        return s;
    }

    public static String unitCategory(String unit) {
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "g": case "gram": case "grams": case "kg":
            case "kilogram": case "kilograms": return "mass";
            case "ml": case "millilitre": case "milliliter": case "milliliters":
            case "l": case "litre": case "liter":
            case "tsp": case "teaspoon": case "teaspoons":
            case "tbsp": case "tablespoon": case "tablespoons":
            case "cup": case "cups": return "volume";
            default: return "count";
        }
    }

    public static double toBase(double qty, String unit) {
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "kg": case "kilogram": case "kilograms": return qty * 1000;
            case "g": case "gram": case "grams": return qty;
            case "l": case "litre": case "liter": return qty * 1000;
            case "ml": case "millilitre": case "milliliter": case "milliliters": return qty;
            case "tsp": case "teaspoon": case "teaspoons": return qty * 5;
            case "tbsp": case "tablespoon": case "tablespoons": return qty * 15;
            case "cup": case "cups": return qty * 240;
            default: return qty;
        }
    }

    private static String key(String name, String unit) {
        return normalizeName(name) + "|" + unitCategory(unit);
    }

    private static Map<String, Double> aggregatePantry(List<PantryItem> pantry) {
        Map<String, Double> avail = new HashMap<>();
        for (PantryItem p : pantry) {
            double base = toBase(p.getQuantity(), p.getUnit());
            avail.merge(key(p.getName(), p.getUnit()), base, Double::sum);
        }
        return avail;
    }

    public static List<Recipe> strictMatches(List<Recipe> recipes,
                                             List<PantryItem> pantry, DatabaseHelper db) {
        Map<String, Double> avail = aggregatePantry(pantry);
        List<Recipe> result = new ArrayList<>();
        for (Recipe r : recipes) {
            List<RecipeIngredient> required = db.getIngredientsForRecipe(r.getId());
            boolean ok = true;
            for (RecipeIngredient ri : required) {
                double need = toBase(ri.getQuantity(), ri.getUnit());
                Double have = avail.get(key(ri.getName(), ri.getUnit()));
                if (have == null || have + 1e-9 < need) { ok = false; break; }
            }
            if (ok) result.add(r);
        }
        return result;
    }

    public static List<Recipe> almostThere(List<Recipe> recipes,
                                           List<PantryItem> pantry, DatabaseHelper db) {
        Map<String, Double> avail = aggregatePantry(pantry);
        List<Recipe> result = new ArrayList<>();
        for (Recipe r : recipes) {
            List<RecipeIngredient> required = db.getIngredientsForRecipe(r.getId());
            int missing = 0;
            for (RecipeIngredient ri : required) {
                double need = toBase(ri.getQuantity(), ri.getUnit());
                Double have = avail.get(key(ri.getName(), ri.getUnit()));
                if (have == null || have + 1e-9 < need) missing++;
                if (missing > 1) break;
            }
            if (missing == 1) result.add(r);
        }
        return result;
    }
}
