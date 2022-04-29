package net.zenxarch.bot.defense;

import static net.zenxarch.bot.ZenBot.mc;
import static net.zenxarch.bot.defense.DefenseStateManager.*;
import static net.zenxarch.bot.util.ClientPlayerHelper.*;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;

public final class KillAura extends EntityDefenseModule {

  @Override
  public void handleHostile(MobEntity me) {
    if (handleCrit())
      hitLiving(me);
  }

  @Override
  public void handlePlayer(AbstractClientPlayerEntity pe) {
    if (handleCrit())
      hitLiving(pe);
  }

  @Override
  public void handlePassive(MobEntity me) {
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
      var dmg = getAttackDamage(inv.main.get(i));
      if (dmg > bestDamage) {
        bestDamage = dmg;
        bestSlot = i;
      }
    }
    var dmg = getAttackDamage(inv.offHand.get(0));
    if (dmg > bestDamage) {
      bestSlot = inv.main.size();
    }
    return bestSlot;
  }

  private float getAttackDamage(ItemStack stack) {
    if (stack.getItem() instanceof SwordItem sword) {
      return sword.getAttackDamage();
    }
    if (stack.getItem() instanceof MiningToolItem mti) {
      return mti.getAttackDamage();
    }
    return 0.0f;
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
}
