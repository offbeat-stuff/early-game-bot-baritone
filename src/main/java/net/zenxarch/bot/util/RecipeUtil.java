package net.zenxarch.bot.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;

public class RecipeUtil {
  public static Recipe findRecipe(Item i) {
    var rm = MinecraftClient.getInstance()
                 .getNetworkHandler()
                 .getRecipeManager();
    var viableRecipes =
        rm.values()
            .stream()
            .filter(recipe -> recipe.getOutput().getItem() == i)
            .filter(r
                    -> r.getType() != RecipeType.CAMPFIRE_COOKING ||
                           r.getType() != RecipeType.STONECUTTING);
    return viableRecipes.findFirst().get();
  }
}