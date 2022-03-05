package net.zenxarch.bot.util;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;

public class ClientPlayerHelper {
  private static final MinecraftClient mc =
      MinecraftClient.getInstance();

  public static void lookAt(double x, double y, double z) {
    var p = mc.player;
    double dx = x - p.getX();
    double dy = y - (p.getY() + p.getEyeHeight(p.getPose()));
    double dz = z - p.getZ();
    double dh = Math.sqrt(dx * dx + dz * dz);
    p.setYaw((float)Math.toDegrees(Math.atan2(dz, dx)) - 90);
    p.setPitch((float)-Math.toDegrees(Math.atan2(dy, dh)));
  }

  public static void lookAt(Entity e) {
    lookAt(e.getX(), e.getY() + e.getEyeHeight(e.getPose()),
           e.getZ());
  }

  public static LivingEntity lookingAt() {
    if (mc.crosshairTarget.getType() == Type.ENTITY) {
      if (((EntityHitResult)mc.crosshairTarget).getEntity() instanceof
          LivingEntity le) {
        return le;
      }
    }
    return null;
  }

  public static void syncRotation() {
    var p = mc.player;
    p.networkHandler.sendPacket(
        new PlayerMoveC2SPacket.LookAndOnGround(
            p.getYaw(), p.getPitch(), p.isOnGround()));
  }
}