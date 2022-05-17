package net.zenxarch.bot.defense.modules

import net.zenxarch.bot.util.ClientPlayerHelper._

import net.minecraft.block.Blocks
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.RaycastContext.FluidHandling
import net.minecraft.world.World
import net.zenxarch.bot.defense.DefenseStateManager
import net.zenxarch.bot.util.BlockPlacementUtils
import net.minecraft.item.ItemStack
import net.minecraft.item.BlockItem

class WaterMLG extends Module("WaterMlg"):
  import Module.mc

  override def preTick() =
    val saveItemSlot = findSaveItem()
    if saveItemSlot == -1 then return

    if mc.player.isOnGround() || mc.player.inPowderSnow ||
      mc.player.isTouchingWater()
    then return

    var blocks = getBlocksUntilLanding()
    if blocks < 0 || blocks > 5 then return
    if mc.player.fallDistance + blocks < 4 then return

    var pos = mc.player.getBlockPos().down(blocks - 1)
    var hit =
      BlockPlacementUtils.raycastToBlockForPlacement(pos, FluidHandling.NONE)
    if hit == null then return
    DefenseStateManager.performAction(() => {
      pickItemSlot(saveItemSlot)
      return BlockPlacementUtils.place(hit, Hand.MAIN_HAND)
    })

  private def getBlocksUntilLanding(): Int =
    val start = mc.player.getBlockPos()
    val end = Math.min(start.getY() - mc.world.getBottomY(), 5)
    for i <- 0 until end
    do
      val pos = start.down(i)
      if !checkAir(pos) then return if safeToLand(pos) then -1 else i
    return -1

  private def checkAir(pos: BlockPos) =
    mc.world.getBlockState(pos).getCollisionShape(mc.world, pos).isEmpty()

  private def safeToLand(pos: BlockPos) =
    !mc.world.getFluidState(pos).isEmpty() ||
      mc.world.getBlockState(pos).isOf(Blocks.POWDER_SNOW)

  private def findSaveItem(): Int =
    var water = findInInventory(Items.WATER_BUCKET)
    if water != -1 && !mc.player.world.getRegistryKey().equals(World.NETHER)
    then return water
    var powder = findInInventory(Items.POWDER_SNOW_BUCKET)
    if powder != -1 then return powder

    return findInInventory(is =>
      !is.isEmpty() && is.getItem().isInstanceOf[BlockItem]
    )
