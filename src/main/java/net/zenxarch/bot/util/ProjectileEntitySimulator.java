package net.zenxarch.bot.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ProjectileEntitySimulator {
  private static final MinecraftClient mc = MinecraftClient.getInstance();

  public static int wouldHitPlayer(ArrowEntity arrow, int ticks) {
    var airDrag = 0.99;
    var gravity = 0.05000000074505806;
    var p = arrow.getPos();
    var vel = arrow.getVelocity();
    for (int i = 0; i < ticks; i++) {
      var nextPos = p.add(vel);
      var hit = tickPos(arrow, p, vel);
      if (hit.getType() != HitResult.Type.MISS)
        nextPos = hit.getPos();
      if (hit.getType() == HitResult.Type.ENTITY) {
        var e = ((EntityHitResult)hit).getEntity();
        if (e.equals(mc.player)) {
          return i;
        }
      }
      if (nextPos.squaredDistanceTo(mc.player.getPos()) >= 16 * 16)
        return ticks;
      p = nextPos;
      vel = vel.multiply(airDrag).subtract(0, gravity, 0);
    }
    return ticks;
  }

  private static HitResult tickPos(ArrowEntity arrow, Vec3d pos, Vec3d vel) {
    var p = pos;
    var np = p.add(vel);

    var hitResult = mc.world.raycast(
        new RaycastContext(p, np, RaycastContext.ShapeType.COLLIDER,
                           RaycastContext.FluidHandling.NONE, mc.player));
    if (hitResult.getType() != HitResult.Type.MISS) {
      np = hitResult.getPos();
    }

    var hitResult2 = ProjectileUtil.getEntityCollision(
        mc.world, arrow, p, np, new Box(p, np),
        e -> { return e.isAlive() && !(e.isSpectator()) && e.collides(); });
    if (hitResult2 != null)
      return hitResult2;
    return hitResult;
  }
}
