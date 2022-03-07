package net.zenxarch.bot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.zenxarch.bot.util.ClientPlayerHelper;
import net.zenxarch.bot.util.TargetUtil;

public final class KillAura {
  private static LivingEntity target;
  private static Double targetDistance;
  private static ClientPlayerEntity p;
  private static final MinecraftClient mc =
      MinecraftClient.getInstance();
  private static boolean wasBlocking;

  private static boolean isGood(LivingEntity e) {
    if (!e.isAlive() || e.isDead())
      return false;
    if (e instanceof AnimalEntity && e.isBaby())
      return false;
    if (e instanceof GolemEntity || e instanceof VillagerEntity ||
        e instanceof TameableEntity)
      return false;
    if (e instanceof PlayerEntity p && p.isCreative())
      return false;
    return p.canSee(e);
  }

  private static void updateTarget() {
    var nearbyTargets = TargetUtil.getNearbyTargets();
    targetDistance = Double.POSITIVE_INFINITY;
    target = null;
    for (int i = 0; i < nearbyTargets.size(); i++) {
      var t = nearbyTargets.get(i);
      if (isGood(t.getRight()) && targetDistance > t.getLeft()) {
        targetDistance = t.getLeft();
        target = t.getRight();
      }
    }
  }

  public static boolean needsControl() {
    if (target == null) {
      unblockShield();
      return false;
    }
    return true;
  }

  public static void onTick() {
    p = mc.player;
    if (p == null || p.isDead() || p.isSpectator() || p.isSleeping())
      return;
    if (mc.currentScreen != null &&
        !(mc.currentScreen instanceof HandledScreen))
      return;
    updateTarget();
    if (target == null)
      return;
    if (mc.currentScreen != null)
      p.closeHandledScreen();
    if (ClientPlayerHelper.lookingAt() != target) {
      ClientPlayerHelper.lookAt(target);
      ClientPlayerHelper.syncRotation();
    }
    if (handleCrit())
      attackTarget();
    else
      blockShield();
  }

  private static boolean handleCrit() {
    var canCrit =
        !(p.isSubmergedInWater() || p.isClimbing() || p.isInLava());
    if (canCrit) {
      if (p.isOnGround()) {
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
    mc.options.keyUse.setPressed(true);
    wasBlocking = true;
  }

  private static void unblockShield() {
    if (!wasBlocking)
      return;
    mc.options.keyUse.setPressed(false);
    wasBlocking = false;
  }

  private static void switchItem() {
    var items = new Item[] {
        Items.NETHERITE_AXE, Items.NETHERITE_SWORD, Items.DIAMOND_AXE,
        Items.DIAMOND_SWORD, Items.IRON_AXE,        Items.IRON_SWORD,
        Items.STONE_AXE,     Items.STONE_SWORD,     Items.WOODEN_AXE,
        Items.WOODEN_SWORD};
    for (int i = 0; i < items.length; i++) {
      if (ClientPlayerHelper.pickItem(items[i]))
        return;
    }
  }
}
