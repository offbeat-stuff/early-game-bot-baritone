package net.zenxarch.bot;

import static net.zenxarch.bot.util.BaritoneUtils.*;

import java.util.ArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.zenxarch.bot.util.ClientPlayerHelper;
import net.zenxarch.bot.util.TargetUtil;

public final class KillAura {
  private static ClientPlayerEntity p;
  public static final MinecraftClient mc = MinecraftClient.getInstance();
  private static boolean isActive;
  private static boolean wasBlocking;
  private static Entity target;
  private static boolean shouldBlock;

  private static void updateTarget() {
    TargetUtil.updateTargets();
    shouldBlock = true;
    target = TargetUtil.getNearestProjectile();
    if (target != null)
      return;
    target = TargetUtil.getNearestEnemyPlayer();
    if (target != null)
      return;
    target = TargetUtil.getNearestHostile();
    if (target != null)
      return;
    target = TargetUtil.getNearestPassive();
    shouldBlock = false;
  }

  public static void setActive(boolean active) { isActive = active; }

  public static boolean needsControl() {
    if (target == null) {
      unblockShield();
      resumePathing();
      return false;
    }
    return true;
  }

  public static Entity getTarget() { return target; }

  public static void onTick() {
    target = null;
    p = mc.player;
    if (!isActive || p == null || p.isDead() || p.isSpectator() ||
        p.isSleeping())
      return;

    updateTarget();
    if (target == null)
      return;

    pausePathing();

    if (mc.currentScreen != null) {
      if (mc.currentScreen instanceof HandledScreen)
        p.closeHandledScreen();
      else
        return;
    }
    if (ClientPlayerHelper.lookingAt() != target) {
      ClientPlayerHelper.lookAt(target);
      ClientPlayerHelper.syncRotation();
    }

    if (handleCrit())
      attackTarget();
    if (shouldBlock)
      blockShield();
    else
      unblockShield();
  }

  private static boolean handleCrit() {
    if (!(target instanceof LivingEntity))
      return false;
    var canCrit = !(p.isSubmergedInWater() || p.isClimbing() || p.isInLava());
    var remainingTicks = ClientPlayerHelper.getRemainingAttackCooldownTicks();
    if (canCrit) {
      if (p.isOnGround() && target.getY() >= p.getY() - 1) {
        if (remainingTicks < 5)
          p.jump();
        return false;
      } else if (p.getVelocity().y > 0)
        return false;
    }
    return remainingTicks == 0;
  }

  private static void attackTarget() {
    switchItem();
    ClientPlayerHelper.hitEntity(target);
  }

  private static void blockShield() {
    if (!p.getOffHandStack().getItem().equals(Items.SHIELD))
      return;
    mc.options.useKey.setPressed(true);
    wasBlocking = true;
  }

  private static void unblockShield() {
    if (!wasBlocking)
      return;
    mc.options.useKey.setPressed(false);
    wasBlocking = false;
  }

  private static final ArrayList<Item> weapons = new ArrayList<Item>() {
    {
      add(Items.NETHERITE_AXE);
      add(Items.NETHERITE_SWORD);
      add(Items.DIAMOND_AXE);
      add(Items.DIAMOND_SWORD);
      add(Items.IRON_AXE);
      add(Items.IRON_SWORD);
      add(Items.STONE_AXE);
      add(Items.STONE_SWORD);
      add(Items.WOODEN_AXE);
      add(Items.WOODEN_SWORD);
    }
  };

  private static void switchItem() {
    for (Item weapon : weapons) {
      if (ClientPlayerHelper.pickItem(weapon))
        return;
    }
  }
}
