package net.zenxarch.bot.defense.modules;

import static net.zenxarch.bot.defense.DefenseStateManager.*;
import static net.zenxarch.bot.util.ClientPlayerHelper.*;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.SwordItem;

public final class KillAura extends Module {

  public KillAura() { super("KillAura"); }

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
    performAction(() -> {
      if (!lookingAt(le))
        lookAt(le);
      pickItemSlot(findBestWeapon(le));
      hitEntity(le);
      return true;
    });
  }

  private int findBestWeapon(LivingEntity target) {
    var bestSlot = -1;
    var bestDamage = 0.0f;
    var inv = mc.player.getInventory();
    for (int i = 0; i < inv.main.size(); i++) {
      var dmg = getAttackDamage(inv.main.get(i), target);
      if (dmg > bestDamage) {
        bestDamage = dmg;
        bestSlot = i;
      }
    }

    if (getAttackDamage(inv.offHand.get(0), target) > bestDamage) {
      bestSlot = inv.main.size();
    }
    return bestSlot;
  }

  private float getAttackDamage(ItemStack stack, LivingEntity target) {
    if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof
                                                    MiningToolItem) {
      return EnchantmentHelper.getAttackDamage(stack, target.getGroup());
    }
    return 0.0f;
  }

  private boolean handleCrit() {
    var canCrit = !(mc.player.isTouchingWater() || mc.player.isClimbing() ||
                    mc.player.isInLava() ||
                    mc.player.hasStatusEffect(StatusEffects.BLINDNESS) ||
                    mc.player.hasVehicle());
    var remainingTicks = getRemainingAttackCooldownTicks();
    if (canCrit) {
      if (mc.player.isOnGround()) {
        if (remainingTicks < 5)
          performAction(() -> {
            mc.player.jump();
            return true;
          });
        return false;
      } else
        return mc.player.fallDistance > 0;
    }
    return remainingTicks <= 0;
  }
}
