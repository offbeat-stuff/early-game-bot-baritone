package net.zenxarch.bot.defense;

import static net.zenxarch.bot.ZenBot.mc;
import static net.zenxarch.bot.defense.DefenseStateManager.*;
import static net.zenxarch.bot.util.BaritoneUtils.*;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.zenxarch.bot.util.ClientPlayerHelper;

public final class KillAura extends EntityDefenseModule {
  private boolean wasBlocking;

  @Override
  public void handleNone() {
    unblockShield();
    resumePathing();
  }

  @Override
  public void handleProjectile(ProjectileEntity pe) {
    ClientPlayerHelper.lookAt(pe);
    blockShield();
  }

  @Override
  public void handleHostile(MobEntity me) {
    blockShield();
    if (handleCrit())
      hitLiving(me);
  }

  @Override
  public void handlePlayer(AbstractClientPlayerEntity pe) {
    blockShield();
    if (handleCrit())
      hitLiving(pe);
  }

  @Override
  public void handlePassive(MobEntity me) {
    unblockShield();
    if (handleCrit())
      hitLiving(me);
  }

  private boolean handleCrit() {
    var canCrit = !(mc.player.isSubmergedInWater() || mc.player.isClimbing() ||
                    mc.player.isInLava());
    var remainingTicks = ClientPlayerHelper.getRemainingAttackCooldownTicks();
    if (canCrit) {
      if (mc.player.isOnGround()) {
        if (remainingTicks < 5)
          mc.player.jump();
        return false;
      } else if (mc.player.getVelocity().y > 0)
        return false;
    }
    return remainingTicks <= 0;
  }

  private void blockShield() {
    if (!mc.player.getOffHandStack().getItem().equals(Items.SHIELD))
      return;
    mc.options.useKey.setPressed(true);
    wasBlocking = true;
  }

  private void unblockShield() {
    if (!wasBlocking)
      return;
    mc.options.useKey.setPressed(false);
    wasBlocking = false;
  }
}
