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
import net.zenxarch.bot.util.WeaponUtils

class KillAura extends Module("KillAura"):
  import Module.mc

  override def handleHostile(me: MobEntity): Unit =
    BaritoneUtils.pausePathing()
    handleLiving(me)

  override def handlePlayer(pe: AbstractClientPlayerEntity): Unit =
    BaritoneUtils.pausePathing()
    handleLiving(pe)

  override def handlePassive(me: MobEntity): Unit =
    BaritoneUtils.pausePathing()
    handleLiving(me)

  override def handleNone(): Unit =
    BaritoneUtils.resumePathing()

  private def handleLiving(le: LivingEntity): Unit =
    val bestWeapon = findBestWeapon(le)
    if bestWeapon != -1 && !handleCrit() then return
    performAction { () =>
      {
        if !lookingAt(le) then lookAt(le)
        pickItemSlot(bestWeapon)
        hitEntity(le)
        true
      }
    }

  private def findBestWeapon(target: LivingEntity): Int =
    return findBestInInventory(
      WeaponUtils.getAttackDamagePerSec(_, target.getGroup())
    )

  private def handleCrit(): Boolean =
    val canCrit = !(mc.player.isTouchingWater() || mc.player.isClimbing() ||
      mc.player.isInLava() ||
      mc.player.hasStatusEffect(StatusEffects.BLINDNESS) ||
      mc.player.hasVehicle())
    lazy val remainingTicks = getRemainingAttackCooldownTicks()
    if canCrit then
      if mc.player.isOnGround() then
        if remainingTicks < 5 then
          performAction(() => {
            var wasSneaking = mc.player.input.sneaking
            mc.player.input.sneaking = false
            mc.player.jump()
            mc.player.input.sneaking = wasSneaking
            return true
          })
        return false
      else if mc.player.fallDistance <= 0 then return false
    return remainingTicks < 1
