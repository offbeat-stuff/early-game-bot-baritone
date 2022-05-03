package net.zenxarch.bot.defense;

import static net.zenxarch.bot.ZenBot.mc;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;
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
    modules.add(new ShieldBlock());
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
    if (checkTargets() && !tryHandleMcScreen())
      return;

    var projectileTarget = TargetUtil.getNearestProjectile();
    if (projectileTarget != null) {
      modules.forEach(m -> m.handleProjectile(projectileTarget));
      return;
    }

    var hostileTarget = TargetUtil.getNearestHostile();
    if (hostileTarget != null) {
      modules.forEach(m -> m.handleHostile(hostileTarget));
      return;
    }

    var playerTarget = TargetUtil.getNearestEnemyPlayer();
    if (playerTarget != null) {
      modules.forEach(m -> m.handlePlayer(playerTarget));
      return;
    }

    var passiveTarget = TargetUtil.getNearestPassive();
    if (passiveTarget != null) {
      modules.forEach(m -> m.handlePassive(passiveTarget));
      return;
    }

    modules.forEach(m -> m.handleNone());
  }

  public static ArrayList<EntityDefenseModule> getModules() { return modules; }

  public static EntityDefenseModule getModule(String name) {
    for (var module : modules) {
      if (module.getName() == name) {
        return module;
      }
    }
    return null;
  }

  public static boolean postTickCheck() { return isActionPerformed; }

  public static boolean performAction(BooleanSupplier action) {
    if (isActionPerformed)
      return false;
    isActionPerformed = action.getAsBoolean();
    return isActionPerformed;
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
