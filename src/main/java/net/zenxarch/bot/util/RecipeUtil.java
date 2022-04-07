package net.zenxarch.bot.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmokingRecipe;

public class RecipeUtil {

  private static ArrayList<ShapedRecipe> craftingRecipes =
      new ArrayList<>();
  private static ArrayList<BlastingRecipe> blastingRecipes =
      new ArrayList<>();
  private static ArrayList<SmokingRecipe> smokingRecipes =
      new ArrayList<>();
  private static ArrayList<SmeltingRecipe> smeltingRecipes =
      new ArrayList<>();

  private static final MinecraftClient mc =
      MinecraftClient.getInstance();

  public static void recache() {
    var rm = mc.getNetworkHandler().getRecipeManager();
    rm.values().forEach(r -> {
      if (r instanceof ShapedRecipe c)
        craftingRecipes.add(c);
      if (r instanceof BlastingRecipe b)
        blastingRecipes.add(b);
      if (r instanceof SmokingRecipe s)
        smokingRecipes.add(s);
      if (r instanceof SmeltingRecipe s)
        smeltingRecipes.add(s);
    });
  }

  public static ArrayList<Item> getIngredientItemIds(Ingredient ing) {
    var res = new ArrayList<Item>(0);
    for (var is : ing.getMatchingStacks()) {
      res.add(is.getItem());
    }
    return res;
  }

  public static HashMap<List<Item>, Integer>
  simpleIngredient(Recipe<?> r) {
    var res = new HashMap<List<Item>, Integer>();
    var iter = r.getIngredients().iterator();
    while (iter.hasNext()) {
      Ingredient ing = (Ingredient)iter.next();
      if (ing.isEmpty())
        continue;
      var simple = getIngredientItemIds(ing);
      res.put(simple, res.getOrDefault(simple, 0) + 1);
    }
    return res;
  }

  public static ArrayList<BlastingRecipe>
  findBlastingRecipes(Item item) {
    return recipeFilter(blastingRecipes, item);
  }

  public static ArrayList<SmokingRecipe>
  findSmokingRecipes(Item item) {
    return recipeFilter(smokingRecipes, item);
  }

  public static ArrayList<SmeltingRecipe>
  findSmeltingRecipes(Item item) {
    return recipeFilter(smeltingRecipes, item);
  }

  public static ArrayList<ShapedRecipe>
  findCraftingRecipes(Item item) {
    return recipeFilter(craftingRecipes, item);
  }

  private static <T extends Recipe<?>> ArrayList<T>
  recipeFilter(ArrayList<T> in, Item item) {
    var res = new ArrayList<T>();
    for (int i = 0; i < in.size(); i++) {
      if (in.get(i).getOutput().getItem().equals(item)) {
        res.add(in.get(i));
      }
    }
    return res;
  }
}