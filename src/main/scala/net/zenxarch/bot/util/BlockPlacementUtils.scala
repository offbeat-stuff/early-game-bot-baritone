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

  def raycastToBlockForPlacement(
      pos: BlockPos,
      f: FluidHandling
  ): BlockHitResult =
    var vpos = new Vec3d(pos.getX(), pos.getY(), pos.getZ())
    vpos = vpos.add(0.5, -0.02, 0.5)
    val result = praycast(vpos, f)
    if result.getType() == HitResult.Type.BLOCK then
      val bhit = result.asInstanceOf[BlockHitResult]
      if bhit.getBlockPos().equals(pos.down()) &&
        bhit.getSide() == Direction.UP
      then return bhit
    return null

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
