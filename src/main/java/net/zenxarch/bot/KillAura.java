package net.zenxarch.bot;

import java.util.ArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

public final class KillAura {
  private static KillAura INSTANCE;

  private ArrayList<LivingEntity> targets;
  private ClientPlayerEntity p;
  private MinecraftClient mc;
  public KillAura() {
    INSTANCE = this;
    this.targets = new ArrayList<LivingEntity>(0);
  }

  public Double opD(LivingEntity e) {
    var xd = p.getX() - e.getX();
    var yd = p.getY() - e.getY();
    var zd = p.getZ() - e.getZ();
    return xd * xd + yd * yd + zd * zd;
  }

  private boolean isGood(LivingEntity e) {
    if (opD(e) > 25)
      return false;
    if (e instanceof PassiveEntity)
      return false;
    if (e.isBlocking() || e.isInvulnerable())
      return false;
    // if(e.hurtTime == 0) return true;
    // if(e.isAttackable()) return true;
    return true;
  }

  private void updateTargets() {
    targets = new ArrayList<LivingEntity>(0);
    mc.world.getEntities().forEach(e -> {
      if (!(e instanceof LivingEntity) || e == null)
        return;
      if (isGood((LivingEntity)e)) {
        targets.add((LivingEntity)e);
      }
    });
  }

  private void lookAt(double x, double y, double z) {
    double dx = x - p.getX();
    double dy = y - (p.getY() + p.getEyeHeight(p.getPose()));
    double dz = z - p.getZ();
    double dh = Math.sqrt(dx * dx + dz * dz);
    p.setYaw((float)Math.toDegrees(Math.atan2(dz, dx)) - 90);
    p.setPitch((float)-Math.toDegrees(Math.atan2(dy, dh)));
  }

  private void lookAt(LivingEntity e) {
    lookAt(e.getX(), e.getY() + e.getEyeHeight(e.getPose()),
           e.getZ());
  }

  public void syncRotation() {
    p.networkHandler.sendPacket(
        new PlayerMoveC2SPacket.LookAndOnGround(
            p.getYaw(), p.getPitch(), p.isOnGround()));
  }

  public boolean needsControl() {
    updateTargets();
    return targets.size() > 0;
  }

  public void onTick() {
    mc = MinecraftClient.getInstance();
    p = mc.player;
    if (p.isDead())
      return;
    if (targets.size() == 0)
      return;
    if (mc.currentScreen != null) {
      p.closeScreen();
    }
    targets.sort((e1, e2) -> sort(e1, e2));
    var e = targets.get(0);
    if (lookingAt() == e) {
      if (e instanceof CreeperEntity &&
          mc.player.getOffHandStack().getItem().equals(
              Items.SHIELD)) {
        if (opD(e) < 4) {
          p.swingHand(Hand.OFF_HAND);
        } else {
          p.swingHand(Hand.MAIN_HAND);
        }
      } else {
        p.swingHand(Hand.MAIN_HAND);
      }
    } else {
      lookAt(targets.get(0));
      syncRotation();
    }
  }

  private LivingEntity lookingAt() {
    if (mc.crosshairTarget != null &&
        mc.crosshairTarget.getType() == Type.ENTITY) {
      var e = ((EntityHitResult)mc.crosshairTarget).getEntity();
      if (e instanceof LivingEntity) {
        return (LivingEntity)e;
      }
    }
    return null;
  }

  // TODO: remove recalc of distance
  private int sort(LivingEntity e1, LivingEntity e2) {
    return Double.compare(opD(e1), opD(e2));
  }

  public KillAura getInstance() { return INSTANCE; }
}