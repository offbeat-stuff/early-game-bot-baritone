package net.zenxarch.bot.task;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class CraftTask extends Task {
  private boolean running;
  private Recipe goal;
  public CraftTask(Recipe r) {
    super("Craft Task");
    this.goal = r;
    this.running = true;
  }

  @Override
  public void onTick() {
    MinecraftClient mc = MinecraftClient.getInstance();
    if (!isCurrentScreenOkay(mc.currentScreen)) {
      this.running = false;
      return;
    }
    var im = mc.interactionManager;
    if (goal.getType() == RecipeType.CRAFTING) {
      var s = (CraftingScreenHandler)mc.player.currentScreenHandler;
      im.clickRecipe(s.syncId, goal, false);
      if (s.getSlot(0).getStack().equals(goal.getOutput())) {
        im.clickSlot(s.syncId, 0, 0, SlotActionType.QUICK_MOVE,
                     mc.player);
      }
    }
  }

  private boolean isCurrentScreenOkay(Screen s) {
    var rt = goal.getType();
    if (rt == RecipeType.CRAFTING) {
      return s instanceof CraftingScreen;
    } else if (rt == RecipeType.SMOKING) {
      return s instanceof SmokerScreen || s instanceof FurnaceScreen;
    } else if (rt == RecipeType.BLASTING) {
      return s instanceof BlastFurnaceScreen || s instanceof
                                                    FurnaceScreen;
    } else if (rt == RecipeType.SMELTING) {
      return s instanceof FurnaceScreen;
    } else if (rt == RecipeType.SMITHING) {
      return s instanceof SmithingScreen;
    } else if (rt == RecipeType.STONECUTTING) {
      return s instanceof StonecutterScreen;
    }
    return false;
  }
}
