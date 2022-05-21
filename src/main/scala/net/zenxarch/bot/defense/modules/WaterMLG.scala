package net.zenxarch.bot.defense.modules

import net.zenxarch.bot.util.ClientPlayerHelper._

import net.minecraft.block.Blocks
import net.minecraft.item.Items
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.RaycastContext.FluidHandling
import net.minecraft.world.World
import net.zenxarch.bot.defense.DefenseStateManager
import net.zenxarch.bot.util.BlockPlacementUtils._
import net.minecraft.item.ItemStack
import net.minecraft.item.BlockItem
import net.minecraft.util.math.Direction
import net.zenxarch.bot.util.ClientPlayerHelper
import net.minecraft.util.hit.HitResult
import net.minecraft.util.hit.BlockHitResult

class WaterMLG extends Module("WaterMlg"):
  import Module.mc

  override def preTick() =
    val saveItemSlot = findSaveItem()
    if saveItemSlot == -1 then return

    if mc.player.isOnGround() || mc.player.inPowderSnow ||
      mc.player.isTouchingWater()
    then return

    var landPos = getLandingBlock(6)
    if landPos == null then return
    val blocks = mc.player.getPos.y.toFloat - landPos.getY.toFloat
    if mc.player.fallDistance + blocks < 4 then return

    landPos = landPos.up
    val side = getPlaceableSide(landPos, checkSide(landPos, _))
    if side == null then return
    val vec = getVecForBlockPlacement(landPos, side)

    val hit = praycast(vec, FluidHandling.NONE)
    if hit == null || !hit.getType().equals(HitResult.Type.BLOCK) then return
    val bhit = hit.asInstanceOf[BlockHitResult]
    if !bhit.getBlockPos().equals(landPos.offset(side)) || !bhit
        .getSide()
        .equals(side.getOpposite)
    then return
    DefenseStateManager.performAction(() => {
      pickItemSlot(saveItemSlot)
      ClientPlayerHelper.lookAt(vec.x, vec.y, vec.z)
      return place(bhit, Hand.MAIN_HAND)
    })

  private def checkSide(pos: BlockPos, side: Direction): Boolean =
    val block = mc.world.getBlockState(pos.offset(side))
    side != Direction.UP && block.isSolidBlock(mc.world, pos)

  private def getLandingBlock(upto: Int): BlockPos =
    val start = mc.player.getBlockPos()
    val end = Math.min(start.getY() - mc.world.getBottomY(), upto)
    val bb = mc.player.getBoundingBox()
    for
      y <- 1 until end
      x <- bb.minX.toInt to bb.maxX.toInt
      z <- bb.minZ.toInt to bb.maxZ.toInt
    do
      val pos = BlockPos(x, start.getY - y, z)
      if !checkAir(pos) then
        return if safeToLand(pos) then null
        else pos
    return null

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
