#!/usr/bin/env bash
# setup_structure.sh
# Creates the Smart Pantry Manager file structure (package com.example.smartpantrymanager).
#
# Usage:
#   ./setup_structure.sh                 # auto-detects the project root (see below)
#   ./setup_structure.sh path/to/root    # explicit project root
#
# Root detection when no argument is given:
#   - if the current folder looks like an Android project (has app/ and settings.gradle), use it
#   - otherwise create ./SmartPantryManager
#
# Safe to re-run: existing files are NEVER overwritten or deleted.

set -euo pipefail

PKG="com.example.smartpantrymanager"
PKG_DIR="${PKG//.//}"

# ---------- Decide project root ----------
if [ $# -ge 1 ]; then
  ROOT="$1"
elif [ -d "app" ] && { [ -f "settings.gradle" ] || [ -f "settings.gradle.kts" ]; }; then
  ROOT="."
else
  ROOT="SmartPantryManager"
fi

APP="$ROOT/app"
MAIN="$APP/src/main"
JAVA="$MAIN/java/$PKG_DIR"
RES="$MAIN/res"

created=0
skipped=0

# make_file <path> [content]
make_file() {
  local path="$1"
  local content="${2:-}"
  if [ -e "$path" ]; then
    echo "  skip     ${path#./}"
    skipped=$((skipped + 1))
  else
    mkdir -p "$(dirname "$path")"
    printf '%s' "$content" > "$path"
    echo "  created  ${path#./}"
    created=$((created + 1))
  fi
}

echo "Project root: $ROOT"
echo

# ---------- Directories ----------
mkdir -p "$JAVA" "$RES/layout" "$RES/menu" "$RES/values"

# ---------- Java sources (package line only; paste the real code over these) ----------
JAVA_FILES=(
  MainActivity
  PantryFragment
  PantryAdapter
  AddEditIngredientActivity
  SuggestedRecipesFragment
  RecipeAdapter
  RecipeDetailActivity
  SettingsFragment
  IngredientMatcher
  DatabaseHelper
  PantryItem
  Recipe
  RecipeIngredient
)
echo "Java sources:"
for name in "${JAVA_FILES[@]}"; do
  make_file "$JAVA/$name.java" "package $PKG;"$'\n\n'
done

# ---------- Layouts ----------
LAYOUTS=(
  activity_main
  fragment_pantry
  item_pantry
  fragment_suggested
  item_recipe
  activity_add_edit
  activity_recipe_detail
  fragment_settings
)
echo
echo "Layouts:"
for name in "${LAYOUTS[@]}"; do
  make_file "$RES/layout/$name.xml"
done

# ---------- Menu, values, manifest, gradle, README ----------
echo
echo "Menu, values and config:"
make_file "$RES/menu/bottom_nav.xml"
make_file "$RES/values/strings.xml"
make_file "$RES/values/themes.xml"
make_file "$MAIN/AndroidManifest.xml"
make_file "$APP/build.gradle"
make_file "$ROOT/README.md"

# ---------- .gitignore (only if the Android Studio wizard didn't make one) ----------
echo
echo "Git:"
make_file "$ROOT/.gitignore" "$(cat <<'EOF'
*.iml
.gradle/
/local.properties
/.idea/
.DS_Store
/build/
/app/build/
/captures/
.externalNativeBuild/
.cxx/
EOF
)
"

# ---------- Warnings about common wizard leftovers ----------
echo
if [ -f "$APP/build.gradle.kts" ]; then
  echo "WARNING: app/build.gradle.kts exists. The spec needs Groovy (app/build.gradle)."
  echo "         Recreate the project with 'Groovy DSL' as the build configuration language."
fi
if [ -d "$RES/values-night" ]; then
  echo "WARNING: $RES/values-night exists. Delete it so it doesn't override themes.xml."
fi

echo "Done. Created $created file(s), skipped $skipped existing."
echo "Files are placeholders. Paste the code from Phases 1-3 into them."
