package net.zenxarch.bot.defense;

import static net.zenxarch.bot.ZenBot.mc;

import java.util.ArrayList;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.zenxarch.bot.util.TargetUtil;

public class DefenseStateManager {
  private static boolean isDefenseActive;
  private static boolean isActionPerformed;
  private static final ArrayList<EntityDefenseModule> modules =
      new ArrayList<>();

  private static boolean shouldCloseHandledScreen;

  public static void setActiveStatus(boolean status) {
    isDefenseActive = status;
  }

  public static boolean getActiveStatus() { return isDefenseActive; }

  public static void init() {
    modules.add(new KillAura());
    modules.add(new AutoFire());
  }

  private static boolean checkTargets() {
    return TargetUtil.getNearestEnemyPlayer() == null &&
        TargetUtil.getNearestHostile() == null &&
        TargetUtil.getNearestPassive() == null &&
        TargetUtil.getNearestProjectile() == null;
  }

  public static void preTick() {
    if (!isDefenseActive)
      return;
    isActionPerformed = false;
    TargetUtil.updateTargets();
    if (checkTargets())
      return;
    if (!tryHandleMcScreen())
      return;

    var projectileTarget = TargetUtil.getNearestProjectile();
    if (projectileTarget != null) {
      for (var module : modules) {
        if (isActionPerformed)
          break;
        module.handleProjectile(projectileTarget);
      }
      return;
    }

    var hostileTarget = TargetUtil.getNearestHostile();
    if (hostileTarget != null) {
      for (var module : modules) {
        if (isActionPerformed)
          break;
        module.handleHostile(hostileTarget);
      }
      return;
    }

    var playerTarget = TargetUtil.getNearestEnemyPlayer();
    if (playerTarget != null) {
      for (var module : modules) {
        if (isActionPerformed)
          break;
        module.handlePlayer(playerTarget);
      }
      return;
    }

    var passiveTarget = TargetUtil.getNearestPassive();
    if (passiveTarget != null) {
      for (var module : modules) {
        if (isActionPerformed)
          break;
        module.handlePassive(passiveTarget);
      }
      return;
    }

    for (var module : modules) {
      module.handleNone();
    }
  }

  public static boolean postTickCheck() { return isActionPerformed; }

  public static boolean canPerformAction() {
    if (isActionPerformed)
      return false;
    isActionPerformed = true;
    return true;
  }

  public static boolean tryHandleMcScreen() {
    if (mc.currentScreen == null)
      return true;
    if (mc.currentScreen instanceof HandledScreen) {
      if (shouldCloseHandledScreen)
        mc.player.closeHandledScreen();
      return shouldCloseHandledScreen;
    }
    return true;
  }
}
