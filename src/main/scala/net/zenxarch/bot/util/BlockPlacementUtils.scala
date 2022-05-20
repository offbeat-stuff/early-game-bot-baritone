package net.zenxarch.bot.util

import net.minecraft.client.MinecraftClient
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.minecraft.world.RaycastContext.FluidHandling
import net.minecraft.world.RaycastContext.ShapeType
import net.zenxarch.bot.ZenBot.mc

object BlockPlacementUtils:
  def getVecForBlockPlacement(
      target: BlockPos,
      side: Direction
  ): Vec3d =
    Vec3d(
      target.getX.toDouble + 0.5 + side.getOffsetX.toDouble * 0.52,
      target.getY.toDouble + 0.5 + side.getOffsetY.toDouble * 0.52,
      target.getZ.toDouble + 0.5 + side.getOffsetZ.toDouble * 0.52
    )

  def isSideOkayForPlacement(target: BlockPos, side: Direction): Boolean =
    mc.world.getBlockState(target).isSideSolidFullSquare(mc.world, target, side)

  def getPlaceableSide(target: BlockPos): Direction =
    for
      i <- Array(
        Direction.DOWN,
        Direction.EAST,
        Direction.WEST,
        Direction.SOUTH,
        Direction.NORTH
      )
      if isSideOkayForPlacement(target.offset(i), i.getOpposite)
    do return i
    return Direction.UP

  def raycastToBlockForPlacement(
      target: BlockPos,
      f: FluidHandling
  ): BlockHitResult =
    val side = getPlaceableSide(target)
    if side.equals(Direction.UP) then return null
    val vpos = getVecForBlockPlacement(target, side)
    val result = praycast(vpos, f)
    return result.getType() match
      case HitResult.Type.BLOCK =>
        val bhit = result.asInstanceOf[BlockHitResult]
        if bhit.getBlockPos().equals(target.offset(side)) &&
          bhit.getSide().equals(side.getOpposite)
        then bhit
        else null
      case _ => null

  def place(hit: BlockHitResult, hand: Hand): Boolean =
    val res =
      mc.interactionManager.interactBlock(mc.player, mc.world, hand, hit)
    if res.shouldSwingHand() then mc.player.swingHand(hand)
    return res.isAccepted()

  private def praycast(v: Vec3d, f: FluidHandling): HitResult =
    mc.world.raycast(
      new RaycastContext(
        mc.player.getEyePos(),
        v,
        ShapeType.COLLIDER,
        f,
        mc.player
      )
    )
