package net.zenxarch.bot.defense.modules

import net.zenxarch.bot.defense.DefenseStateManager._
import net.zenxarch.bot.util.ClientPlayerHelper._

import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.mob.MobEntity
import net.minecraft.item.ItemStack

import net.minecraft.item.MiningToolItem
import net.minecraft.item.SwordItem
import net.zenxarch.bot.util.BaritoneUtils

class KillAura extends Module("KillAura") {
  import Module.mc

  override def handleHostile(me: MobEntity): Unit = {
    BaritoneUtils.pausePathing()
    if (handleCrit())
      hitLiving(me)
  }

  override def handlePlayer(pe: AbstractClientPlayerEntity): Unit = {
    BaritoneUtils.pausePathing()
    if (handleCrit())
      hitLiving(pe)
  }

  override def handlePassive(me: MobEntity): Unit = {
    BaritoneUtils.pausePathing()
    if (handleCrit())
      hitLiving(me)
  }

  override def handleNone(): Unit = {
    BaritoneUtils.resumePathing()
  }

  private def hitLiving(le: LivingEntity) = {
    performAction { () =>
      {
        if (!lookingAt(le))
          lookAt(le)
        pickItemSlot(findBestWeapon(le))
        hitEntity(le)
        true
      }
    }
  }

  private def findBestWeapon(target: LivingEntity): Int = {
    var bestSlot = -1
    var bestDamage = 0.0f
    var inv = mc.player.getInventory()
    for (i <- 0 until inv.main.size()) {
      var dmg = getAttackDamage(inv.main.get(i), target)
      if (dmg > bestDamage) {
        bestDamage = dmg
        bestSlot = i
      }
    }

    if (getAttackDamage(inv.offHand.get(0), target) > bestDamage) {
      bestSlot = inv.main.size()
    }
    return bestSlot
  }

  private def getAttackDamage(stack: ItemStack, target: LivingEntity): Float = {
    if (
      stack.getItem().isInstanceOf[SwordItem] || 
      stack.getItem().isInstanceOf[MiningToolItem]
    ) {
      return EnchantmentHelper.getAttackDamage(stack, target.getGroup())
    }
    return 0.0f
  }

  private def handleCrit(): Boolean = {
    var canCrit = !(mc.player.isTouchingWater() || mc.player.isClimbing() ||
      mc.player.isInLava() ||
      mc.player.hasStatusEffect(StatusEffects.BLINDNESS) ||
      mc.player.hasVehicle())
    var remainingTicks = getRemainingAttackCooldownTicks()
    if (canCrit) {
      if (mc.player.isOnGround()) {
        if (remainingTicks < 5)
          performAction(() => {
            var wasSneaking = mc.player.input.sneaking
            mc.player.input.sneaking = false
            mc.player.jump()
            mc.player.input.sneaking = wasSneaking
            return true
          })
        return false
      } else if (mc.player.getVelocity().y > 0) {
        return false
      };
    }
    return remainingTicks <= 1
  }
}
