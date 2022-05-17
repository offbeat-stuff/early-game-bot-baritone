package net.zenxarch.bot.util

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.zenxarch.bot.ZenBot.mc

object ProjectileEntitySimulator:
  def wouldHitPlayer(arrow: ArrowEntity, ticks: Int): Int =
    val airDrag = 0.99
    val gravity = 0.05000000074505806
    var p = arrow.getPos()
    var vel = arrow.getVelocity()
    for i <- 0 until ticks do
      var nextPos = p.add(vel)
      var hit = tickPos(arrow, p, vel)
      if hit.getType() != HitResult.Type.MISS then nextPos = hit.getPos()
      if checkPlayerCollision(p, nextPos) then return i
      if nextPos.squaredDistanceTo(mc.player.getPos()) >= 16 * 16 then
        return ticks
      p = nextPos
      vel = vel.multiply(airDrag).subtract(0, gravity, 0)
    return ticks

  private def checkPlayerCollision(a: Vec3d, b: Vec3d): Boolean =
    var abox = new Box(a, b)
    abox = abox.expand(0.3)
    val pbox =
      mc.player.getBoundingBox().stretch(mc.player.getVelocity()).expand(0.1)
    return abox.intersects(pbox)

  private def tickPos(arrow: ArrowEntity, pos: Vec3d, vel: Vec3d): HitResult =
    var p = pos
    var np = p.add(vel)

    var hitResult = mc.world.raycast(
      new RaycastContext(
        p,
        np,
        RaycastContext.ShapeType.COLLIDER,
        RaycastContext.FluidHandling.NONE,
        mc.player
      )
    )
    if hitResult.getType() != HitResult.Type.MISS then np = hitResult.getPos()

    var hitResult2 = ProjectileUtil.getEntityCollision(
      mc.world,
      mc.player,
      p,
      np,
      new Box(p, np),
      e => { e.isAlive() && !(e.isSpectator()) && e.collides() }
    )
    return if hitResult2 != null then hitResult2 else hitResult
