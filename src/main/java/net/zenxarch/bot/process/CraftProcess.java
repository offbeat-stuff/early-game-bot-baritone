package net.zenxarch.bot.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.client.gui.screen.ingame.FurnaceScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Recipe;
// import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.SmeltingRecipe;
// import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Pair;
import net.zenxarch.bot.util.RecipeUtil;

public final class CraftProcess {
  private static ArrayList<Pair<ShapedRecipe, Integer>>
      craftingQueue = new ArrayList<>();
  private static ArrayList<Pair<SmeltingRecipe, Integer>>
      smeltingQueue = new ArrayList<>();
  // private static ArrayList<BlastingRecipe> blastingQueue =
  //   new ArrayList<>();
  // private static ArrayList<SmokingRecipe> smokingQueue =
  //   new ArrayList<>();
  private static final MinecraftClient mc =
      MinecraftClient.getInstance();
  public static void preTick() {
    if (craftingQueue.size() > 0 && mc.currentScreen instanceof
                                        CraftingScreen) {
      craft();
    }
    if (smeltingQueue.size() > 0 && mc.currentScreen instanceof
                                        FurnaceScreen) {
      smelt();
    }
  }

  public static int checkMaxCraftable(Recipe<?> r) {
    HashMap<List<Item>, Integer> sr = RecipeUtil.simpleIngredient(r);
    var inv = mc.player.getInventory()
                  .main.stream()
                  .filter(Predicate.not(ItemStack::isEmpty))
                  .toList();
    var itemAvail = new HashMap<List<Item>, Integer>();
    for (var is : inv) {
      for (var k : sr.keySet()) {
        if (k.contains(is.getItem()))
          itemAvail.put(k,
                        itemAvail.getOrDefault(k, 0) + is.getCount());
      }
    }
    for (var e : sr.entrySet()) {
      itemAvail.put(e.getKey(),
                    itemAvail.get(e.getKey()) / e.getValue());
    }
    return itemAvail.values().stream().min(Integer::compare).get();
  }

  private static void craft() {
    var r = craftingQueue.get(0);
    var cs = (CraftingScreen)mc.currentScreen;
    var csh = cs.getScreenHandler();
    var im = mc.interactionManager;
    var os = csh.getCraftingResultSlotIndex();
    if (csh.getSlot(os).getStack().equals(r.getLeft().getOutput())) {
      im.clickSlot(csh.syncId, os, 0, SlotActionType.QUICK_MOVE,
                   mc.player);
      r.setRight(r.getRight() - 1);
      if (r.getRight() == 0) {
        craftingQueue.remove(0);
      }
      return;
    }
    im.clickRecipe(csh.syncId, r.getLeft(), false);
  }

  private static void smelt() {
    var r = smeltingQueue.get(0).getLeft();
    var cs = (FurnaceScreen)mc.currentScreen;
    var csh = cs.getScreenHandler();
    var os = 2;
    // var is = 0;
    // var fs = 1;
    var im = mc.interactionManager;
    if (csh.getSlot(0).getStack().equals(r.getOutput())) {
      im.clickSlot(csh.syncId, os, 0, SlotActionType.QUICK_MOVE,
                   mc.player);
      var q = smeltingQueue.get(0);
      q.setRight(q.getRight() - 1);
      if (q.getRight() == 0) {
        smeltingQueue.remove(0);
      }
      return;
    }
    if (!checkSmeltInput(csh, r)) {
      im.clickRecipe(csh.syncId, r, false);
    }
    if (csh.isBurning()) {
      return;
    }
    for (int i = csh.slots.size() - 9 - 27; i < csh.slots.size();
         i++) {
      if (!csh.getSlot(i).getStack().isOf(Items.COAL)) {
        continue;
      }
      im.clickSlot(csh.syncId, i, 0, SlotActionType.QUICK_MOVE,
                   mc.player);
      break;
    }
  }

  private static boolean checkSmeltInput(FurnaceScreenHandler csh,
                                         SmeltingRecipe r) {
    assert r.getIngredients().size() == 1;
    return r.getIngredients().get(0).test(csh.getSlot(0).getStack());
  }

  public static void postTick() {}

  public static void enqueue(ShapedRecipe r, Integer amt) {
    craftingQueue.add(new Pair<ShapedRecipe, Integer>(r, amt));
  }

  public static void enqueue(SmeltingRecipe r, Integer amt) {
    smeltingQueue.add(new Pair<SmeltingRecipe, Integer>(r, amt));
  }

  /*

  public static void enqueue(BlastingRecipe r){
    blastingQueue.add(r);
  }

  public static void enqueue(SmokingRecipe r){
    smokingQueue.add(r);
  }

  */
}
