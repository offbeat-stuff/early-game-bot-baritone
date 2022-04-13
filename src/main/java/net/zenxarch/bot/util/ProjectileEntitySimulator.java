package net.zenxarch.bot.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ProjectileEntitySimulator {
  private static final MinecraftClient mc = MinecraftClient.getInstance();

  public static boolean wouldHitPlayer(ArrowEntity arrow, int ticks) {
    var airDrag = 0.99;
    var gravity = 0.05000000074505806;
    var p = arrow.getPos();
    var vel = arrow.getVelocity();
    var pbox = mc.player.getBoundingBox()
                   .stretch(mc.player.getVelocity())
                   .expand(0.3F);
    for (int i = 0; i < ticks; i++) {
      var nextPos = p.add(vel);
      var hit = tickPos(p, vel);
      if (hit.getType() != HitResult.Type.MISS)
        nextPos = hit.getPos();
      var abox = new Box(p, nextPos).expand(0.5);
      if (abox.intersects(pbox))
        return true;
      if (hit.getType() != HitResult.Type.MISS)
        return false;
      if (nextPos.squaredDistanceTo(mc.player.getPos()) >= 16 * 16)
        return false;
      p = nextPos;
      vel = vel.multiply(airDrag).subtract(0, gravity, 0);
    }
    return false;
  }

  private static HitResult tickPos(Vec3d pos, Vec3d vel) {
    var p = pos;
    var np = p.add(vel);

    var hitResult = mc.world.raycast(
        new RaycastContext(p, np, RaycastContext.ShapeType.COLLIDER,
                           RaycastContext.FluidHandling.NONE, mc.player));
    if (hitResult.getType() != HitResult.Type.MISS) {
      np = hitResult.getPos();
    }

    var hitResult2 = ProjectileUtil.getEntityCollision(
        mc.world, mc.player, p, np, new Box(p, np),
        e -> { return e.isAlive() && !(e.isSpectator()) && e.collides(); });
    if (hitResult2 != null)
      return hitResult2;

    return hitResult;
  }
}
