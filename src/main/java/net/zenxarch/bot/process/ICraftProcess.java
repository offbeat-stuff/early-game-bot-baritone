package net.zenxarch.bot.process;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

public class ICraftProcess {
  private boolean _isActive;
  private Item itemToCraft = Items.WOODEN_PICKAXE;
  private IBaritone baritone;
  private Recipe itemRecipe = null;

  private int waitTicks;
  /* Stage 0 -> start baritone
   * Stage 1 -> wait for baritone, when finished wait 2 ticks
   * Stage 2 -> find the recipe of the item and click it
   * Stage 3 -> Quick move the item slot
   *
   * */
  private int stage;
  private int nextStage = -1;
  private boolean stageCompleted = false;

  public ICraftProcess() {}

  public void activate(Item item) {
    baritone = BaritoneAPI.getProvider().getPrimaryBaritone();
    _isActive = true;
    itemToCraft = item;
  }

  private Recipe findRecipe(MinecraftClient mc) {
    RecipeManager rm = mc.getNetworkHandler().getRecipeManager();
    for (Recipe recipe : rm.values()) {
      if (recipe.getOutput().getItem().equals(itemToCraft) &&
          recipe.getType().equals(RecipeType.CRAFTING)) {
        return recipe;
      }
    }
    return null;
  }

  void stage0(MinecraftClient mc) {
    itemRecipe = findRecipe(mc);
    if (itemRecipe == null) {
      mc.player.sendMessage(Text.of("Uncraftable item\n"), false);
      _isActive = false;
      return;
    }
    baritone.getGetToBlockProcess().getToBlock(Blocks.CRAFTING_TABLE);
    stageCompleted = true;
  }

  void stage1(MinecraftClient mc) {
    if (!baritone.getGetToBlockProcess().isActive()) {
      stageCompleted = true;
      waitTicks = 2;
    }
  }

  void stage2(MinecraftClient mc) {
    if (mc.currentScreen instanceof CraftingScreen) {
      RecipeManager rm = mc.getNetworkHandler().getRecipeManager();
      for (Recipe recipe : rm.values()) {
        if (recipe.getOutput().getItem().equals(itemToCraft)) {
          int syncId = mc.player.currentScreenHandler.syncId;
          mc.interactionManager.clickRecipe(syncId, recipe, false);
          break;
        }
      }
    }
    waitTicks = 3;
    stageCompleted = true;
  }

  void stage3(MinecraftClient mc) {
    if (mc.currentScreen instanceof CraftingScreen) {
      if (mc.player.currentScreenHandler.getSlot(0)
              .getStack()
              .getItem() == itemToCraft) {
        int syncId = mc.player.currentScreenHandler.syncId;
        mc.interactionManager.clickSlot(
            syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
      }
    }
    stageCompleted = true;
    _isActive = false;
  }

  public void tick(MinecraftClient mc) {
    if (!_isActive) {
      return;
    }
    if (waitTicks > 0) {
      waitTicks--;
      return;
    }
    stageCompleted = false;
    switch (stage) {
    case 0:
      stage0(mc);
      break;
    case 1:
      stage1(mc);
      break;
    case 2:
      stage2(mc);
      break;
    case 3:
      stage3(mc);
      break;
    default:
      stage = 0;
    }
    if (stageCompleted) {
      stage++;
    }
    if (!_isActive) {
      stage = 0;
    }
  }
}
