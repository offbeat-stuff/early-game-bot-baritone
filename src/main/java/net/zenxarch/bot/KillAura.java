package net.zenxarch.bot;

import baritone.api.BaritoneAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.zenxarch.bot.util.ClientPlayerHelper;
import net.zenxarch.bot.util.TargetUtil;

public final class KillAura {
  private static ClientPlayerEntity p;
  private static final MinecraftClient mc = MinecraftClient.getInstance();
  private static boolean isActive;
  private static boolean wasBlocking;
  private static boolean wasPathing;
  private static Entity target;
  private static boolean shouldBlock;

  private static void updateTarget() {
    shouldBlock = true;
    target = TargetUtil.getNearestProjectile();
    if (target != null)
      return;
    target = TargetUtil.getNearestHostile();
    if (target != null)
      return;
    target = TargetUtil.getNearestPassive();
    shouldBlock = false;
  }

  public static void toggle() { isActive = !isActive; }

  private static void pausePathing() {
    if (!wasPathing && BaritoneAPI.getProvider()
                           .getPrimaryBaritone()
                           .getPathingBehavior()
                           .isPathing()) {
      wasPathing = true;
      BaritoneAPI.getProvider()
          .getPrimaryBaritone()
          .getCommandManager()
          .execute("pause");
    }
  }

  private static void resumePathing() {
    if (wasPathing && BaritoneAPI.getProvider()
                          .getPrimaryBaritone()
                          .getPathingBehavior()
                          .isPathing()) {
      BaritoneAPI.getProvider()
          .getPrimaryBaritone()
          .getCommandManager()
          .execute("resume");
      wasPathing = false;
    }
  }

  public static boolean needsControl() {
    if (target == null) {
      unblockShield();
      resumePathing();
      return false;
    }
    return true;
  }

  public static void onTick() {
    if (!isActive) {
      target = null;
      return;
    }
    p = mc.player;
    if (p == null || p.isDead() || p.isSpectator() || p.isSleeping())
      return;
    updateTarget();
    if (target == null)
      return;
    pausePathing();
    if (mc.currentScreen != null && mc.currentScreen instanceof HandledScreen)
      p.closeHandledScreen();
    if (!wasPathing && BaritoneAPI.getProvider()
                           .getPrimaryBaritone()
                           .getPathingBehavior()
                           .isPathing()) {
      wasPathing = true;
      BaritoneAPI.getProvider()
          .getPrimaryBaritone()
          .getCommandManager()
          .execute("pause");
    }
    if (!ClientPlayerHelper.lookingAt().equals(target)) {
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
    if (canCrit) {
      if (p.isOnGround() && target.getY() >= p.getY() - 1) {
        p.jump();
        return false;
      } else if (p.getVelocity().y > 0)
        return false;
    }
    return p.getAttackCooldownProgress(0.0f) >= 1.0;
  }

  private static void attackTarget() {
    switchItem();
    mc.interactionManager.attackEntity(mc.player, target);
    p.swingHand(Hand.MAIN_HAND);
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

  private static void switchItem() {
    var items = new Item[] {Items.NETHERITE_AXE, Items.NETHERITE_SWORD,
                            Items.DIAMOND_AXE,   Items.DIAMOND_SWORD,
                            Items.IRON_AXE,      Items.IRON_SWORD,
                            Items.STONE_AXE,     Items.STONE_SWORD,
                            Items.WOODEN_AXE,    Items.WOODEN_SWORD};
    for (int i = 0; i < items.length; i++) {
      if (ClientPlayerHelper.pickItem(items[i]))
        return;
    }
  }
}
