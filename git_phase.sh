#!/usr/bin/env bash
# git_phase.sh — commit and push Smart Pantry Manager one phase at a time.
#
# Usage (run inside the project, any subfolder):
#   bash git_phase.sh init <github-repo-url>   # one-time: git init, branch main, add remote
#   bash git_phase.sh <1|2|3|4> [flags]        # commit that phase's files, then push
#
# Flags:
#   --dry-run    show what would be committed; change nothing
#   --no-push    commit locally, skip the push
#   --force      commit even if files still look like empty placeholders
#
# Each phase becomes several small commits (15 in total), so the history is
# meaningful and clears the "10+ commits" requirement. Nothing is backdated:
# commits get the real time you run the script.

set -euo pipefail

J="app/src/main/java/com/example/smartpantrymanager"
R="app/src/main/res"

# Step format: "commit message|space-separated files"
# @SCAFFOLD = everything not named in any other step (wizard files, gradle, .gitignore, ...)
P1=(
  "Initial Android Studio project scaffold|@SCAFFOLD"
  "Add model classes (PantryItem, Recipe, RecipeIngredient)|$J/PantryItem.java $J/Recipe.java $J/RecipeIngredient.java"
  "Implement DatabaseHelper with schema, pantry CRUD and recipe seeding|$J/DatabaseHelper.java"
)
P2=(
  "Add IngredientMatcher with normalisation, strict and almost-there matching|$J/IngredientMatcher.java"
  "Build Pantry list with RecyclerView and adapter|$J/PantryFragment.java $J/PantryAdapter.java"
  "Add ingredient form activity with input validation|$J/AddEditIngredientActivity.java"
  "Build Suggested Recipes fragment and adapter|$J/SuggestedRecipesFragment.java $J/RecipeAdapter.java"
  "Add recipe detail activity|$J/RecipeDetailActivity.java"
  "Add Settings fragment and bottom-navigation MainActivity|$J/SettingsFragment.java $J/MainActivity.java"
)
P3=(
  "Add pantry list layouts|$R/layout/fragment_pantry.xml $R/layout/item_pantry.xml"
  "Add ingredient form layout|$R/layout/activity_add_edit.xml"
  "Add suggested recipes layouts|$R/layout/fragment_suggested.xml $R/layout/item_recipe.xml"
  "Add recipe detail layout|$R/layout/activity_recipe_detail.xml"
  "Add settings and main layouts with bottom navigation menu|$R/layout/fragment_settings.xml $R/layout/activity_main.xml $R/menu/bottom_nav.xml"
)
P4=(
  "Write README with setup and database justification|README.md"
)

# Phase 1 config files must be real, since they ride along in the scaffold commit.
SCAFFOLD_CHECK="app/build.gradle app/src/main/AndroidManifest.xml $R/values/strings.xml $R/values/themes.xml"

die()  { echo "ERROR: $*" >&2; exit 1; }
info() { echo "$*"; }

usage() {
  sed -n '2,16p' "$0" | sed 's/^# \{0,1\}//'
  exit "${1:-0}"
}

# ---------- init subcommand ----------
if [ "${1:-}" = "init" ]; then
  url="${2:-}"
  [ -n "$url" ] || die "usage: bash git_phase.sh init <github-repo-url>"
  [ -d app ] || die "run this from the project root (the folder that contains app/)"
  [ -d .git ] || git init -q
  git branch -M main
  if git remote get-url origin >/dev/null 2>&1; then
    git remote set-url origin "$url"
  else
    git remote add origin "$url"
  fi
  info "Repo ready on branch 'main', origin -> $url"
  info "Next: bash git_phase.sh 1"
  exit 0
fi

# ---------- args ----------
[ $# -ge 1 ] || usage 1
PHASE="$1"; shift
DRY=0; PUSH=1; FORCE=0
for a in "$@"; do
  case "$a" in
    --dry-run) DRY=1 ;;
    --no-push) PUSH=0 ;;
    --force)   FORCE=1 ;;
    -h|--help) usage 0 ;;
    *) die "unknown flag: $a" ;;
  esac
done

case "$PHASE" in
  1) STEPS=("${P1[@]}") ;;
  2) STEPS=("${P2[@]}") ;;
  3) STEPS=("${P3[@]}") ;;
  4) STEPS=("${P4[@]}") ;;
  *) die "phase must be 1, 2, 3 or 4 (or 'init')" ;;
esac

# ---------- repo checks ----------
git rev-parse --is-inside-work-tree >/dev/null 2>&1 \
  || die "not a git repo. Run: bash git_phase.sh init <github-repo-url>"
cd "$(git rev-parse --show-toplevel)"
[ -d app ] || die "git root has no app/ folder. The repo must be created in the project root."
[ -f .gitignore ] || die "no .gitignore. Without it, build/ and local.properties would be committed."

if [ "$DRY" -eq 0 ]; then
  [ -n "$(git config user.name || true)" ] && [ -n "$(git config user.email || true)" ] \
    || die "git identity not set. Run: git config --global user.name \"Your Name\" and user.email \"you@example.com\""
  if [ "$PUSH" -eq 1 ]; then
    git remote get-url origin >/dev/null 2>&1 \
      || die "no 'origin' remote. Run: bash git_phase.sh init <github-repo-url>"
  fi
fi

# ---------- helpers ----------
all_feature_files() {
  local s files
  for s in "${P1[@]}" "${P2[@]}" "${P3[@]}" "${P4[@]}"; do
    files="${s#*|}"
    [ "$files" = "@SCAFFOLD" ] || echo "$files"
  done | tr ' ' '\n'
}

# Placeholder = file with at most one non-blank line (e.g. just the "package ...;" stub).
is_stub() {
  local n
  n=$(grep -c '[^[:space:]]' "$1" || true)
  [ "${n:-0}" -le 1 ]
}

# ---------- pre-flight: every file for this phase exists and has real content ----------
problems=()
check_files=""
for step in "${STEPS[@]}"; do
  files="${step#*|}"
  if [ "$files" = "@SCAFFOLD" ]; then check_files="$check_files $SCAFFOLD_CHECK"
  else check_files="$check_files $files"; fi
done
for f in $check_files; do
  if   [ ! -f "$f" ];   then problems+=("MISSING      $f")
  elif is_stub "$f";    then problems+=("PLACEHOLDER  $f")
  fi
done

if [ ${#problems[@]} -gt 0 ]; then
  echo "Phase $PHASE has files that are missing or still empty placeholders:"
  printf '  %s\n' "${problems[@]}"
  if [ "$FORCE" -eq 1 ] || [ "$DRY" -eq 1 ]; then
    echo "(continuing because of --force / --dry-run)"
  else
    die "paste the real code into these files first, or re-run with --force"
  fi
fi

# ---------- commit each step ----------
total=${#STEPS[@]}
i=0
made=0
excl=()
while IFS= read -r f; do
  [ -n "$f" ] && excl+=(":(exclude)$f")
done < <(all_feature_files)

echo
for step in "${STEPS[@]}"; do
  i=$((i + 1))
  msg="${step%%|*}"
  files="${step#*|}"
  echo "[$i/$total] $msg"

  if [ "$DRY" -eq 1 ]; then
    if [ "$files" = "@SCAFFOLD" ]; then echo "      (all project files not listed in later steps)"
    else for f in $files; do echo "      $f"; done; fi
    continue
  fi

  if [ "$files" = "@SCAFFOLD" ]; then
    git add -- . "${excl[@]}"
  else
    # shellcheck disable=SC2086
    git add -- $files
  fi

  if git diff --cached --quiet; then
    echo "      nothing new to commit, skipped"
  else
    git commit -q -m "$msg"
    made=$((made + 1))
    echo "      committed"
  fi
done

if [ "$DRY" -eq 1 ]; then
  echo
  echo "Dry run: nothing was changed."
  exit 0
fi

echo
echo "Phase $PHASE: $made commit(s) made."
[ "$made" -gt 0 ] && git log --oneline -n "$made"

# ---------- push ----------
if [ "$PUSH" -eq 1 ]; then
  branch="$(git rev-parse --abbrev-ref HEAD)"
  echo
  echo "Pushing to origin/$branch ..."
  if git push -u origin "$branch"; then
    echo "Pushed. Check the commits on GitHub before moving to the next phase."
  else
    echo
    echo "Push failed. Your commits are safe locally. Common causes:"
    echo "  - wrong remote URL         -> bash git_phase.sh init <correct-url>"
    echo "  - not signed in to GitHub  -> sign in via the browser prompt, or run: gh auth login"
    echo "  - passwords are rejected   -> use a Personal Access Token as the password"
    echo "  - remote has a README/commit you don't have -> git pull --rebase origin $branch"
    echo "Then retry with: git push -u origin $branch"
    exit 1
  fi
else
  echo "Skipped push (--no-push). Run: git push -u origin $(git rev-parse --abbrev-ref HEAD)"
fi
