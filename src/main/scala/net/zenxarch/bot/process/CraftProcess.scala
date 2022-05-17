package net.zenxarch.bot.process

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.CraftingScreen
import net.minecraft.client.gui.screen.ingame.FurnaceScreen
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Recipe
// import net.minecraft.recipe.BlastingRecipe
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.recipe.SmeltingRecipe
// import net.minecraft.recipe.SmokingRecipe
import net.minecraft.screen.FurnaceScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Pair
import net.zenxarch.bot.util.RecipeUtil
import net.zenxarch.bot.ZenBot.mc
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
import net.minecraft.client.network.ClientPlayerEntity
import scala.jdk.CollectionConverters._
import baritone.aa

object CraftProcess {
  private var craftingQueue =
    new ListBuffer[(ShapedRecipe, Int)]()
  private var smeltingQueue =
    new ListBuffer[(SmeltingRecipe, Int)]()
  // private static ArrayList<BlastingRecipe> blastingQueue =
  //   new ArrayList<>()
  // private static ArrayList<SmokingRecipe> smokingQueue =
  //   new ArrayList<>()
  def preTick() = {
    if (
      craftingQueue.length > 0 && mc.currentScreen.isInstanceOf[CraftingScreen]
    ) {
      craft()
    }
    if (
      smeltingQueue.length > 0 && mc.currentScreen.isInstanceOf[FurnaceScreen]
    ) {
      smelt()
    }
  }

  def checkMaxCraftable(r: Recipe[?]): Int = {
    val sr = RecipeUtil.simpleIngredient(r)
    val p: ClientPlayerEntity = mc.player
    var inv = p
      .getInventory()
      .main
      .stream()
      .filter(s => !s.isEmpty())
      .toList()
      .asScala
    var itemAvail = new HashMap[List[Item], Integer]()
    for (is <- inv) {
      for (k <- sr.keys) {
        if (k.contains(is.getItem()))
          itemAvail.put(
            k,
            itemAvail.getOrElse(k, 0).asInstanceOf[Int] + is.getCount()
          )
      }
    }
    for ((k, v) <- sr) {
      itemAvail.put(k, itemAvail.getOrElse(k, 0).asInstanceOf[Int] / v)
    }
    return itemAvail.reduceLeft((a, b) => if (a(1) < b(1)) a else b)(1)
  }

  private def craft(): Unit = {
    var r = craftingQueue(0)
    if (checkMaxCraftable(r(0)) == 0) {
      return
    }
    var cs = mc.currentScreen.asInstanceOf[CraftingScreen]
    var csh = cs.getScreenHandler()
    var im = mc.interactionManager
    var os = csh.getCraftingResultSlotIndex()
    if (
      csh.getSlot(os).getStack().getItem() ==
        r(0).getOutput().getItem()
    ) {
      im.clickSlot(csh.syncId, os, 0, SlotActionType.QUICK_MOVE, mc.player)
      craftingQueue(0) = r.copy(_2 = r(1) - 1)
      if (r(1) < 2) {
        craftingQueue.remove(0)
      }
      return
    }
    im.clickRecipe(csh.syncId, r(0), false)
  }

  private def smelt(): Unit = {
    var r = smeltingQueue(0)(0)
    var cs = mc.currentScreen.asInstanceOf[FurnaceScreen]
    var csh = cs.getScreenHandler()
    var os = 2
    // var is = 0
    // var fs = 1
    var im = mc.interactionManager
    if (csh.getSlot(0).getStack().equals(r.getOutput())) {
      im.clickSlot(csh.syncId, os, 0, SlotActionType.QUICK_MOVE, mc.player)
      val q = smeltingQueue(0)
      smeltingQueue(0) = q.copy(_2 = q(1) - 1)
      if (q(1) < 2) {
        smeltingQueue.remove(0)
      }
      return
    }
    if (!checkSmeltInput(csh, r)) {
      im.clickRecipe(csh.syncId, r, false)
    }
    if (csh.isBurning()) {
      return
    }
    for
      i <- csh.slots.size() - 9 - 27 until csh.slots.size()
      if csh.getSlot(i).getStack().isOf(Items.COAL)
    do
      im.clickSlot(csh.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player)
      return
  }

  private def checkSmeltInput(
      csh: FurnaceScreenHandler,
      r: SmeltingRecipe
  ): Boolean = {
    assert(r.getIngredients().size() == 1)
    return r.getIngredients().get(0).test(csh.getSlot(0).getStack())
  }

  def postTick() = {}

  def enqueue(r: ShapedRecipe, amt: Int) = {
    craftingQueue += ((r, amt))
  }

  def enqueue(r: SmeltingRecipe, amt: Int) = {
    smeltingQueue += ((r, amt))
  }

  /*

   static void enqueue(BlastingRecipe r){
    blastingQueue.add(r)
  }

   static void enqueue(SmokingRecipe r){
    smokingQueue.add(r)
  }

   */
}
