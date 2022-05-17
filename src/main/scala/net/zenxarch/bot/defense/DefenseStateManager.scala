package net.zenxarch.bot.defense

import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.zenxarch.bot.defense.modules._
import net.zenxarch.bot.util.TargetUtil
import net.zenxarch.bot.ZenBot.mc

object DefenseStateManager {
  private var isDefenseActive = false
  private var isActionPerformed = false
  private var modules: List[Module] = null

  private var shouldCloseHandledScreen = false

  def setActiveStatus(status: Boolean) = {
    isDefenseActive = status
  }

  def getActiveStatus() = isDefenseActive

  def init() = {
    TargetUtil.init()
    modules = List(
      new WaterMLG(),
      new KillAura(),
      new AutoFire(),
      new ShieldBlock()
    )
  }

  private def checkTargets() = TargetUtil.getNearestEnemyPlayer() == null &&
    TargetUtil.getNearestHostile() == null &&
    TargetUtil.getNearestPassive() == null &&
    TargetUtil.getNearestProjectile() == null

  private def allInactive() = modules.filter(_.isActive()).length == 0

  def preTick(): Unit = {
    if (!isDefenseActive)
      return
    isActionPerformed = false

    if (allInactive())
      return

    TargetUtil.updateTargets()
    if (checkTargets() && !tryHandleMcScreen())
      return

    forEachModule(_.preTick())

    var projectileTarget = TargetUtil.getNearestProjectile()
    if (projectileTarget != null) {
      forEachModule(_.handleProjectile(projectileTarget))
      return
    }

    var hostileTarget = TargetUtil.getNearestHostile()
    if (hostileTarget != null) {
      forEachModule(_.handleHostile(hostileTarget))
      return
    }

    var playerTarget = TargetUtil.getNearestEnemyPlayer()
    if (playerTarget != null) {
      forEachModule(_.handlePlayer(playerTarget))
      return
    }

    var passiveTarget = TargetUtil.getNearestPassive()
    if (passiveTarget != null) {
      forEachModule(_.handlePassive(passiveTarget))
      return
    }

    forEachModule(_.handleNone())
  }

  private def forEachModule(exec: (Module) => Unit) = {
    for
      module <- modules
      if module.isActive()
    do exec(module)
  }

  def postTickCheck() = isActionPerformed

  def performAction(action: () => Boolean): Boolean = {
    if (isActionPerformed)
      return false
    isActionPerformed = action.apply()
    return isActionPerformed
  }

  def tryHandleMcScreen(): Boolean = {
    if (mc.currentScreen == null)
      return true
    if (mc.currentScreen.isInstanceOf[HandledScreen]) {
      if (shouldCloseHandledScreen)
        mc.player.closeHandledScreen()
      return shouldCloseHandledScreen
    }
    return true
  }
}
