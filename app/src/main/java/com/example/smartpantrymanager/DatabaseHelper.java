package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public static final String T_PANTRY = "pantry";
    public static final String P_ID = "_id", P_NAME = "name", P_QTY = "quantity",
            P_UNIT = "unit", P_EXPIRY = "expiry_date";
    public static final String T_RECIPES = "recipes";
    public static final String R_ID = "_id", R_NAME = "name", R_STEPS = "steps";
    public static final String T_RECIPE_ING = "recipe_ingredients";
    public static final String RI_ID = "_id", RI_RECIPE_ID = "recipe_id",
            RI_NAME = "name", RI_QTY = "quantity", RI_UNIT = "unit";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context ctx) {
        if (instance == null) instance = new DatabaseHelper(ctx.getApplicationContext());
        return instance;
    }
    private DatabaseHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " (" +
                P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                P_NAME + " TEXT NOT NULL, " + P_QTY + " REAL NOT NULL, " +
                P_UNIT + " TEXT NOT NULL, " + P_EXPIRY + " TEXT);");
        db.execSQL("CREATE TABLE " + T_RECIPES + " (" +
                R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                R_NAME + " TEXT NOT NULL, " + R_STEPS + " TEXT NOT NULL);");
        db.execSQL("CREATE TABLE " + T_RECIPE_ING + " (" +
                RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RI_RECIPE_ID + " INTEGER NOT NULL, " + RI_NAME + " TEXT NOT NULL, " +
                RI_QTY + " REAL NOT NULL, " + RI_UNIT + " TEXT NOT NULL);");
        seedRecipes(db);
    }
    @Override public void onUpgrade(SQLiteDatabase db, int o, int n) {
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    public long insertPantryItem(PantryItem i) {
        ContentValues cv = new ContentValues();
        cv.put(P_NAME, i.getName()); cv.put(P_QTY, i.getQuantity());
        cv.put(P_UNIT, i.getUnit()); cv.put(P_EXPIRY, i.getExpiryDate());
        return getWritableDatabase().insert(T_PANTRY, null, cv);
    }
    public int updatePantryItem(PantryItem i) {
        ContentValues cv = new ContentValues();
        cv.put(P_NAME, i.getName()); cv.put(P_QTY, i.getQuantity());
        cv.put(P_UNIT, i.getUnit()); cv.put(P_EXPIRY, i.getExpiryDate());
        return getWritableDatabase().update(T_PANTRY, cv, P_ID + "=?",
                new String[]{String.valueOf(i.getId())});
    }
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(T_PANTRY, P_ID + "=?",
                new String[]{String.valueOf(id)});
    }
    public PantryItem getPantryItem(long id) {
        Cursor c = getReadableDatabase().query(T_PANTRY, null, P_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (c.moveToFirst()) item = readPantry(c);
        c.close(); return item;
    }
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(T_PANTRY, null, null, null, null, null,
                P_NAME + " COLLATE NOCASE ASC");
        while (c.moveToNext()) list.add(readPantry(c));
        c.close(); return list;
    }
    private PantryItem readPantry(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(P_ID)),
                c.getString(c.getColumnIndexOrThrow(P_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(P_QTY)),
                c.getString(c.getColumnIndexOrThrow(P_UNIT)),
                c.getString(c.getColumnIndexOrThrow(P_EXPIRY)));
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(T_RECIPES, null, null, null, null, null, R_NAME);
        while (c.moveToNext()) {
            list.add(new Recipe(
                    c.getLong(c.getColumnIndexOrThrow(R_ID)),
                    c.getString(c.getColumnIndexOrThrow(R_NAME)),
                    c.getString(c.getColumnIndexOrThrow(R_STEPS))));
        }
        c.close(); return list;
    }
    public List<RecipeIngredient> getIngredientsForRecipe(long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(T_RECIPE_ING, null,
                RI_RECIPE_ID + "=?", new String[]{String.valueOf(recipeId)}, null, null, null);
        while (c.moveToNext()) {
            list.add(new RecipeIngredient(
                    c.getString(c.getColumnIndexOrThrow(RI_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(RI_QTY)),
                    c.getString(c.getColumnIndexOrThrow(RI_UNIT))));
        }
        c.close(); return list;
    }

    private void addRecipe(SQLiteDatabase db, String name, String steps, Object[][] ings) {
        ContentValues cv = new ContentValues();
        cv.put(R_NAME, name); cv.put(R_STEPS, steps);
        long rid = db.insert(T_RECIPES, null, cv);
        for (Object[] ing : ings) {
            ContentValues ic = new ContentValues();
            ic.put(RI_RECIPE_ID, rid); ic.put(RI_NAME, (String) ing[0]);
            ic.put(RI_QTY, ((Number) ing[1]).doubleValue());
            ic.put(RI_UNIT, (String) ing[2]);
            db.insert(T_RECIPE_ING, null, ic);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs", "Whisk eggs, milk, salt. Melt butter. Stir eggs until set.",
                new Object[][]{{"eggs",2,"each"},{"milk",50,"ml"},{"butter",10,"g"},{"salt",1,"tsp"}});
        addRecipe(db, "Tomato Omelette", "Beat eggs. Fry tomato and onion. Add eggs, cook through.",
                new Object[][]{{"eggs",2,"each"},{"tomato",1,"each"},{"onion",1,"each"},{"oil",1,"tbsp"}});
        addRecipe(db, "Pasta with Tomato Sauce", "Boil pasta. Fry garlic in oil. Add tomatoes, simmer, toss.",
                new Object[][]{{"pasta",200,"g"},{"tomato",2,"each"},{"garlic",2,"each"},{"olive oil",2,"tbsp"}});
        addRecipe(db, "Garlic Butter Pasta", "Boil pasta. Melt butter, fry garlic. Toss pasta.",
                new Object[][]{{"pasta",200,"g"},{"butter",20,"g"},{"garlic",3,"each"}});
        addRecipe(db, "Cheese Toastie", "Butter bread. Add cheese between slices. Toast.",
                new Object[][]{{"bread",2,"each"},{"cheese",50,"g"},{"butter",10,"g"}});
        addRecipe(db, "Egg Fried Rice", "Cook rice. Fry egg and veg. Add rice and soy sauce.",
                new Object[][]{{"rice",200,"g"},{"egg",1,"each"},{"soy sauce",2,"tbsp"},
                        {"carrot",1,"each"},{"onion",1,"each"}});
        addRecipe(db, "Chicken Stir Fry", "Slice chicken. Fry garlic in oil. Add chicken and soy sauce.",
                new Object[][]{{"chicken",200,"g"},{"soy sauce",2,"tbsp"},
                        {"garlic",2,"each"},{"oil",1,"tbsp"}});
        addRecipe(db, "Pancakes", "Whisk flour, milk, egg, sugar. Fry small ladles until bubbly.",
                new Object[][]{{"flour",200,"g"},{"milk",250,"ml"},
                        {"egg",1,"each"},{"sugar",2,"tbsp"}});
        addRecipe(db, "French Toast", "Beat egg with milk and cinnamon. Dip bread. Fry.",
                new Object[][]{{"bread",2,"each"},{"egg",1,"each"},
                        {"milk",50,"ml"},{"cinnamon",1,"tsp"}});
        addRecipe(db, "Tomato Soup", "Fry onion and garlic. Add tomatoes and stock. Blend.",
                new Object[][]{{"tomato",4,"each"},{"onion",1,"each"},
                        {"garlic",2,"each"},{"stock",500,"ml"}});
        addRecipe(db, "Grilled Cheese Sandwich", "Butter bread. Fill with cheese. Grill.",
                new Object[][]{{"bread",2,"each"},{"cheese",50,"g"}});
        addRecipe(db, "Banana Smoothie", "Blend banana, milk, honey. Serve chilled.",
                new Object[][]{{"banana",2,"each"},{"milk",200,"ml"},{"honey",1,"tbsp"}});
        addRecipe(db, "Veggie Salad", "Chop lettuce, tomato, cucumber. Drizzle olive oil.",
                new Object[][]{{"lettuce",100,"g"},{"tomato",1,"each"},
                        {"cucumber",1,"each"},{"olive oil",1,"tbsp"}});
        addRecipe(db, "Tuna Pasta", "Boil pasta. Mix tuna with mayo. Combine.",
                new Object[][]{{"pasta",200,"g"},{"tuna",100,"g"},{"mayonnaise",2,"tbsp"}});
        addRecipe(db, "Mashed Potatoes", "Boil potatoes. Mash with butter and milk. Season.",
                new Object[][]{{"potato",500,"g"},{"butter",20,"g"},{"milk",50,"ml"}});
        addRecipe(db, "Chicken Sandwich", "Cook chicken. Layer chicken, lettuce, mayo on bread.",
                new Object[][]{{"bread",2,"each"},{"chicken",100,"g"},
                        {"lettuce",50,"g"},{"mayonnaise",1,"tbsp"}});
        addRecipe(db, "Mushroom Risotto", "Fry onion and mushrooms. Add rice and stock. Simmer.",
                new Object[][]{{"rice",200,"g"},{"mushroom",200,"g"},
                        {"stock",500,"ml"},{"onion",1,"each"}});
        addRecipe(db, "Vegetable Curry", "Fry onion. Add potato, carrot, curry. Add coconut milk.",
                new Object[][]{{"potato",200,"g"},{"carrot",1,"each"},{"onion",1,"each"},
                        {"curry powder",2,"tsp"},{"coconut milk",200,"ml"}});
        addRecipe(db, "Oatmeal", "Simmer oats in milk. Stir in honey.",
                new Object[][]{{"oats",100,"g"},{"milk",200,"ml"},{"honey",1,"tbsp"}});
        addRecipe(db, "Egg Fried Noodles", "Boil noodles. Fry egg in oil. Add noodles and soy sauce.",
                new Object[][]{{"noodles",200,"g"},{"egg",1,"each"},
                        {"soy sauce",1,"tbsp"},{"oil",1,"tbsp"}});
    }
}