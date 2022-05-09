package net.zenxarch.bot.defense;

import static net.zenxarch.bot.ZenBot.mc;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.zenxarch.bot.defense.modules.*;
import net.zenxarch.bot.defense.modules.Module;
import net.zenxarch.bot.util.TargetUtil;

public class DefenseStateManager {
  private static boolean isDefenseActive;
  private static boolean isActionPerformed;
  private static final ArrayList<Module> modules = new ArrayList<>();

  private static boolean shouldCloseHandledScreen;

  public static void setActiveStatus(boolean status) {
    isDefenseActive = status;
  }

  public static boolean getActiveStatus() { return isDefenseActive; }

  public static void init() {
    TargetUtil.init();
    modules.add(new WaterMLG());
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

  private static boolean allInactive() {
    for (var module : modules) {
      if (module.isActive())
        return false;
    }
    return true;
  }

  public static void preTick() {
    if (!isDefenseActive)
      return;
    isActionPerformed = false;

    if (allInactive())
      return;

    TargetUtil.updateTargets();
    if (checkTargets() && !tryHandleMcScreen())
      return;

    forEachModule(m -> m.preTick());

    var projectileTarget = TargetUtil.getNearestProjectile();
    if (projectileTarget != null) {
      forEachModule(m -> m.handleProjectile(projectileTarget));
      return;
    }

    var hostileTarget = TargetUtil.getNearestHostile();
    if (hostileTarget != null) {
      forEachModule(m -> m.handleHostile(hostileTarget));
      return;
    }

    var playerTarget = TargetUtil.getNearestEnemyPlayer();
    if (playerTarget != null) {
      forEachModule(m -> m.handlePlayer(playerTarget));
      return;
    }

    var passiveTarget = TargetUtil.getNearestPassive();
    if (passiveTarget != null) {
      forEachModule(m -> m.handlePassive(passiveTarget));
      return;
    }

    forEachModule(m -> m.handleNone());
  }

  private static void forEachModule(Consumer<? super Module> exec) {
    for (var module : modules) {
      if (module.isActive()) {
        exec.accept(module);
      }
    }
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
