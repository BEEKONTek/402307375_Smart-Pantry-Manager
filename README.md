# Smart Pantry Manager

A Java Android application that helps users cut food waste by tracking the
ingredients they actually have and suggesting recipes they can cook using
ONLY those ingredients.

## Features
- Pantry CRUD (add / edit / delete / list) with name, quantity, unit, expiry
- 20 pre-seeded recipes on first launch
- Strict matching: a recipe is suggested only when every ingredient is
  present in at least the required quantity
- Plural + unit normalisation (tomato/tomatoes, kg/g, l/ml, tsp/tbsp)
- Bonus "Almost There" list (recipes missing exactly one ingredient)
- Recipe detail screen; settings screen with expiry-alert toggle
- Bottom navigation between Pantry / Recipes / Settings

## Database choice: SQLite (SQLiteOpenHelper)
1. Fully offline: a pantry manager must work without internet
2. SQLiteOpenHelper is the pattern taught in the module
3. No cloud credentials or backend to maintain
4. Easiest to justify in the video defence

Schema: pantry, recipes, recipe_ingredients (recipe_id FK).

## Setup / run
1. Open in Android Studio Hedgehog or newer
2. Sync Gradle
3. Run on API 24+ emulator or device
4. 20 recipes seed automatically on first launch

## Project structure
Standard Android Studio layout under
`app/src/main/java/com/example/smartpantrymanager/`.
