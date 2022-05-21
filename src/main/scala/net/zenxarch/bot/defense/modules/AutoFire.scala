package net.zenxarch.bot.defense.modules

import net.zenxarch.bot.util.ClientPlayerHelper._

import net.minecraft.block.AbstractFireBlock
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.RaycastContext.FluidHandling
import net.zenxarch.bot.settings.Settings
import net.zenxarch.bot.util.BlockPlacementUtils
import net.minecraft.util.math.BlockPos

class AutoFire extends BlockModule("AutoFire"):
  import Module.mc
  var lastPos = null
  Settings.registerSetting(this.name + ".complexmethod", false)

  override def shouldTargetNearestBlock() =
    Settings.getBoolean(this.name + ".complexmethod")

  override def canTarget(target: LivingEntity): Boolean =
    if findInInventory(Items.FLINT_AND_STEEL) == -1 then return false
    if target == null then return false
    if mc.player.getEyeY() < target.getBlockY() then return false
    return !(target.isFireImmune() || target.isOnFire() || target.isWet() ||
      target.inPowderSnow)

  override def handleBlock(pos: BlockPos): Boolean =
    var slot = findInInventory(Items.FLINT_AND_STEEL)
    if slot == -1 then return false
    var hit =
      BlockPlacementUtils.raycastToBlockForPlacement(
        pos,
        FluidHandling.ANY,
        _.equals(Direction.DOWN)
      )
    if hit == null then return false
    pickItemSlot(slot)
    if BlockPlacementUtils.place(hit, Hand.MAIN_HAND) then
      lastPos = pos
      return true
    return false

  override def handleLastBlock(lastPos: BlockPos): Boolean =
    if lastPos != null && mc.world
        .getBlockState(lastPos)
        .getBlock()
        .isInstanceOf[AbstractFireBlock]
    then
      mc.interactionManager.attackBlock(lastPos, Direction.UP)
      return true
    return false

  override def canUse(pos: BlockPos) =
    pos != null && mc.world.getFluidState(pos).isEmpty() &&
      mc.world.getBlockState(pos).getMaterial().isReplaceable() &&
      AbstractFireBlock.getState(mc.world, pos).canPlaceAt(mc.world, pos)
