package net.zenxarch.bot.defense.modules

import net.zenxarch.bot.defense.DefenseStateManager.performAction

import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.util.hit.HitResult
import net.zenxarch.bot.util.BaritoneUtils
import net.zenxarch.bot.util.ClientPlayerHelper

class ShieldBlock extends Module("ShieldBlock") {
  import Module.mc
  private var wasBlocking = false;

  override def handleNone() = {
    setBlocking(false)
  }

  override def handleProjectile(pe : ProjectileEntity) = {
    BaritoneUtils.pausePathing()
    if (tryBlock())
      ClientPlayerHelper.lookAt(pe)
  }

  override def handleHostile(me : MobEntity) = {
    tryBlock()
  }

  override def handlePlayer(pe : AbstractClientPlayerEntity) = {
    tryBlock()
  }

  override def handlePassive(me : MobEntity) = {
    setBlocking(false)
  }

  private def tryBlock() = setBlocking(shieldCheck() && performAction(() => true))

  private def shieldCheck() : Boolean = {
    if (mc.player.getOffHandStack().getItem() != Items.SHIELD)
      return false
    if (mc.player.getMainHandStack().isEmpty())
      return true
    val item = mc.player.getMainHandStack().getItem()
    if (item.isInstanceOf[BlockItem])
      return mc.crosshairTarget.getType() != HitResult.Type.BLOCK
    return !(item.isFood())
  }

  private def setBlocking(blocking : Boolean) : Boolean = {
    if (wasBlocking ^ blocking) {
      mc.options.useKey.setPressed(blocking)
      wasBlocking = blocking
    }
    return wasBlocking
  }
}
