package net.zenxarch.bot.defense;

import static net.zenxarch.bot.ZenBot.mc;
import static net.zenxarch.bot.defense.DefenseStateManager.*;
import static net.zenxarch.bot.util.BaritoneUtils.*;
import static net.zenxarch.bot.util.ClientPlayerHelper.*;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.minecraft.item.MiningToolItem;

public final class KillAura extends EntityDefenseModule {
  private boolean wasBlocking;

  @Override
  public void handleNone() {
    unblockShield();
    resumePathing();
  }

  @Override
  public void handleProjectile(ProjectileEntity pe) {
    lookAt(pe);
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

  private void hitLiving(LivingEntity le) {
    if (!canPerformAction())
      return;
    if (!lookingAt(le))
      lookAt(le);
    pickItemSlot(findBestWeapon());
    hitEntity(le);
  }

  private int findBestWeapon() {
    var bestSlot = -1;
    var bestDamage = 0.0f;
    var inv = mc.player.getInventory();
    for (int i = 0; i < inv.main.size(); i++) {
      if (inv.main.get(i).getItem() instanceof MiningToolItem mti) {
        if (mti.getAttackDamage() > bestDamage) {
          bestDamage = mti.getAttackDamage();
          bestSlot = i;
        }
      }
    }
    if (inv.offHand.get(0).getItem() instanceof MiningToolItem mti &&
        mti.getAttackDamage() > bestDamage) {
      bestSlot = inv.main.size();
    }
    return bestSlot;
  }

  private boolean handleCrit() {
    var canCrit = !(mc.player.isSubmergedInWater() || mc.player.isClimbing() ||
                    mc.player.isInLava());
    var remainingTicks = getRemainingAttackCooldownTicks();
    if (canCrit) {
      if (mc.player.isOnGround()) {
        if (remainingTicks < 5)
          if (canPerformAction())
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
