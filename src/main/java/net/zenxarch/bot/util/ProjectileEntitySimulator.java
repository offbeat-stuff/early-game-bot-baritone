package net.zenxarch.bot.util;

import java.util.function.Predicate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ProjectileEntitySimulator {
  private static final MinecraftClient mc =
      MinecraftClient.getInstance();

  public static HitResult simulateMovement(ArrowEntity arrow,
                                           int ticks) {
    var airDrag = 0.99;
    var gravity = 0.05000000074505806;
    var p = arrow.getPos();
    var vel = arrow.getVelocity();
    for (int i = 0; i < ticks; i++) {
      var h = getCollision(arrow, p, vel, e -> {
        return !e.isSpectator() && e.isAlive() && e.collides();
      });
      if (h != null) {
        return h;
      }
      p = p.add(vel);
      vel = vel.multiply(airDrag).subtract(0, gravity, 0);
    }
    return null;
  }

  // Vanilla copy
  private static HitResult getCollision(PersistentProjectileEntity e,
                                        Vec3d pos, Vec3d vel,
                                        Predicate<Entity> predicate) {
    var p = pos;
    var np = p.add(vel);
    var hitResult = mc.world.raycast(
        new RaycastContext(p, np, RaycastContext.ShapeType.COLLIDER,
                           RaycastContext.FluidHandling.NONE, e));
    if (hitResult.getType() != HitResult.Type.MISS) {
      np = hitResult.getPos();
    }
    var box =
        e.getType().getDimensions().getBoxAt(p).stretch(vel).expand(
            1.0D);
    var hitResult2 = ProjectileUtil.getEntityCollision(
        mc.world, e, p, np, box, predicate);
    if (hitResult2 != null) {
      return hitResult2;
    }
    return hitResult;
  }
}
