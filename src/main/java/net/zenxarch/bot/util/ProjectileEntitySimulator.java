package net.zenxarch.bot.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ProjectileEntitySimulator {
  private static final MinecraftClient mc =
      MinecraftClient.getInstance();

  public static boolean wouldHitPlayer(ArrowEntity arrow, int ticks) {
    var airDrag = 0.99;
    var gravity = 0.05000000074505806;
    var p = arrow.getPos();
    var vel = arrow.getVelocity();
    var pbox = mc.player.getBoundingBox()
                   .stretch(mc.player.getVelocity())
                   .expand(0.3F);
    for (int i = 0; i < ticks; i++) {
      var nextPos = tickPos(p, vel);
      var abox = new Box(p, nextPos);
      if (abox.expand(1.0F).intersects(pbox))
        return true;
      p = nextPos;
      vel = vel.multiply(airDrag).subtract(0, gravity, 0);
    }
    return false;
  }

  private static Vec3d tickPos(Vec3d pos, Vec3d vel) {
    var p = pos;
    var np = p.add(vel);
    var hitResult = mc.world.raycast(new RaycastContext(
        p, np, RaycastContext.ShapeType.COLLIDER,
        RaycastContext.FluidHandling.NONE, mc.player));
    if (hitResult.getType() != HitResult.Type.MISS) {
      np = hitResult.getPos();
    }
    return np;
  }
}
