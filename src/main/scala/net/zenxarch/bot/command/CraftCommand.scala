package net.zenxarch.bot.command

import baritone.api.BaritoneAPI
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType._
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager._
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.command.argument.ItemStackArgumentType._
import net.minecraft.item.Item
import net.zenxarch.bot.process.CraftProcess
import net.zenxarch.bot.util.RecipeUtil

class CraftCommand extends ZenCommand:

  override def register() = literal("zcraft").`then`(
    argument("item", itemStack())
      .`then`(
        argument("amount", integer())
          .executes(ctx =>
            this.execute(
              ctx.getSource(),
              getItemStackArgument(ctx, "item").getItem(),
              getInteger(ctx, "amount")
            )
          )
      )
  )

  private def execute(
      source: FabricClientCommandSource,
      item: Item,
      amt: Integer
  ): Int =
    RecipeUtil.recache()
    var b =
      if trySmelt(item, amt) then Blocks.FURNACE
      else if tryCraft(item, amt) then Blocks.CRAFTING_TABLE
      else return -1
    BaritoneAPI
      .getProvider()
      .getPrimaryBaritone()
      .getGetToBlockProcess()
      .getToBlock(b)
    return 0

  def trySmelt(item: Item, amt: Integer): Boolean =
    for
      r <- RecipeUtil.findSmeltingRecipes(item)
      if CraftProcess.checkMaxCraftable(r) >= amt
    do
      CraftProcess.enqueue(r, amt)
      return true
    return false

  private def tryCraft(item: Item, amt: Integer): Boolean =
    for
      r <- RecipeUtil.findCraftingRecipes(item)
      if CraftProcess.checkMaxCraftable(r) >= amt
    do
      CraftProcess.enqueue(r, amt)
      return true
    return false
