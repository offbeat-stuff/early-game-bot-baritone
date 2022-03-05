package net.zenxarch.bot;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.PiglinEntity;
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

  private static boolean isGood(LivingEntity e) {
    if (!e.isAlive() || e.isDead())
      return false;
    if (e instanceof AnimalEntity && e.isBaby())
      return false;
    if (e instanceof EndermanEntity eman && !eman.isAngryAt(p))
      return false;
    if (e instanceof PiglinEntity pig && !pig.isAngryAt(p))
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

  public static boolean needsControl() { return target != null; }

  public static void onTick() {
    p = mc.player;
    if (p.isDead())
      return;
    if (p.getAttackCooldownProgress(0.0f) < 1.0f)
      return;
    if (mc.currentScreen != null &&
        !(mc.currentScreen instanceof HandledScreen))
      return;
    updateTarget();
    if (target == null)
      return;
    if (mc.currentScreen != null)
      p.closeScreen();
    var shouldCrit =
        target.getHealth() > 4.0 &&
        !(p.isSubmergedInWater() || p.isInLava() || p.isClimbing());
    if (p.isOnGround() && shouldCrit &&
        target.getY() >= p.getY() - 0.5) {
      p.jump();
      return;
    }
    if (shouldCrit && p.getVelocity().y > 0)
      return;
    if (ClientPlayerHelper.lookingAt() == target) {
      handleTarget(target);
    } else {
      ClientPlayerHelper.lookAt(target);
      ClientPlayerHelper.syncRotation();
    }
  }

  private static void handleTarget(LivingEntity e) {
    switchItem();
    if (e instanceof CreeperEntity c && c.isIgnited() &&
        mc.player.getOffHandStack().getItem().equals(Items.SHIELD)) {
      mc.interactionManager.interactItem(mc.player, mc.world,
                                         Hand.OFF_HAND);
      mc.player.swingHand(Hand.OFF_HAND);
      return;
    }
    mc.interactionManager.attackEntity(mc.player, e);
    mc.player.swingHand(Hand.OFF_HAND);
  }

  private static void switchItem() {
    var items = new Item[] {
        Items.NETHERITE_AXE, Items.NETHERITE_SWORD, Items.DIAMOND_AXE,
        Items.DIAMOND_SWORD, Items.IRON_AXE,        Items.IRON_SWORD,
        Items.STONE_AXE,     Items.STONE_SWORD,     Items.WOODEN_AXE,
        Items.WOODEN_SWORD};
    var inv = p.getInventory();
    int j = 0;
    boolean found = false;
    for (int i = 0; i < items.length; i++) {
      for (j = 0; j < inv.main.size(); j++) {
        if (p.getInventory().getStack(j).getItem() == items[i]) {
          found = true;
          break;
        }
      }
      if (found)
        break;
    }
    if (!found)
      return;
    if (j >= 0 && j < 9) {
      inv.selectedSlot = j;
      return;
    }
    for (int i = 0; i < 9; i++) {
      if (inv.main.get(i).isEmpty()) {
        inv.selectedSlot = i;
        mc.interactionManager.pickFromInventory(j);
        return;
      }
    }
  }
}