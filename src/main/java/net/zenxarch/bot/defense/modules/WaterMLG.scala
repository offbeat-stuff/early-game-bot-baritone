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

class WaterMLG extends Module("WaterMlg") {
  import Module.mc

  override def preTick() = {
    var saveItemSlot = findSaveItem()
    if (saveItemSlot == -1)
      return

    if (
      mc.player.isOnGround() || mc.player.inPowderSnow ||
      mc.player.isTouchingWater()
    )
      return

    var blocks = getBlocksUntilLanding()
    if (blocks == -1 || blocks == 10)
      return
    if (mc.player.fallDistance + blocks < 3)
      return

    var pos = mc.player.getBlockPos().down(blocks - 1)
    var hit =
      BlockPlacementUtils.raycastToBlockForPlacement(pos, FluidHandling.NONE)
    if (hit == null)
      return
    DefenseStateManager.performAction(() => {
      pickItemSlot(saveItemSlot)
      return BlockPlacementUtils.place(hit, Hand.MAIN_HAND)
    })
  }

  private def getBlocksUntilLanding(): Int = {
    val start = mc.player.getBlockPos()
    val end = Math.min(start.getY() - mc.world.getBottomY(), 10)
    for (i <- 0 until end) {
      val pos = start.down(i)
      if (!checkAir(pos)) {
        if (safeToLand(pos)) {
          return -1
        }
      } else return i
    }
    return -1
  }

  private def checkAir(pos: BlockPos) =
    mc.world.getBlockState(pos).getCollisionShape(mc.world, pos).isEmpty()

  private def safeToLand(pos: BlockPos) =
    !mc.world.getFluidState(pos).isEmpty() ||
      mc.world.getBlockState(pos).isOf(Blocks.POWDER_SNOW)

  private def findSaveItem(): Int = {
    var water = findInInventory(Items.WATER_BUCKET)
    if (water != -1 && mc.player.world.getRegistryKey() != World.NETHER) {
      return water
    }
    return findInInventory(Items.POWDER_SNOW_BUCKET)
  }
}
