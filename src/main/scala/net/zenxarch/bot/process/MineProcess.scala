package net.zenxarch.bot.process

import net.zenxarch.bot.util.ClientPlayerHelper.findInInventory

import net.minecraft.block.Block
import net.minecraft.item.Items
import net.minecraft.tag.BlockTags
import net.minecraft.tag.TagKey
import net.minecraft.util.registry.Registry
import net.zenxarch.bot.util.BaritoneUtils

object MineProcess:
  def mineBlock(target: Block): Unit =
    if !canMine(target) then return
    BaritoneUtils.mine(target)

  private def canMine(target: Block): Boolean =
    if checkTag(target, BlockTags.PICKAXE_MINEABLE) then
      if findInInventory(Items.NETHERITE_PICKAXE) != -1 ||
        findInInventory(Items.DIAMOND_PICKAXE) != -1
      then return true
      if checkTag(target, BlockTags.NEEDS_DIAMOND_TOOL) then return false
      if findInInventory(Items.IRON_PICKAXE) != -1 then return true
      if checkTag(target, BlockTags.NEEDS_IRON_TOOL) then return false
      if findInInventory(Items.STONE_PICKAXE) != -1 then return true
      return !checkTag(target, BlockTags.NEEDS_STONE_TOOL) &&
        findInInventory(Items.WOODEN_PICKAXE) != -1
    return true

  private def checkTag(target: Block, tag: TagKey[Block]): Boolean =
    var optionalKey = Registry.BLOCK.getKey(target)
    if optionalKey.isEmpty() then return false
    var optionalEntry = Registry.BLOCK.getEntry(optionalKey.get())
    if optionalEntry.isEmpty() then return false
    return optionalEntry.get().isIn(tag)
