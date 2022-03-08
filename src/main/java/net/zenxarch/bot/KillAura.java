package net.zenxarch.bot;

import baritone.api.BaritoneAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.zenxarch.bot.util.ClientPlayerHelper;
import net.zenxarch.bot.util.TargetUtil;

public final class KillAura {
  private static Entity target;
  private static Double targetDistance;
  private static ClientPlayerEntity p;
  private static final MinecraftClient mc =
      MinecraftClient.getInstance();
  private static boolean wasBlocking;
  private static boolean wasPathing;

  private static boolean isGood(Entity be) {
    if(!(be instanceof LivingEntity))
      return p.canSee(be);
    var e = (LivingEntity)be;
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
      if(wasPathing && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()){
        BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("resume");
        wasPathing = false;
      }
      return false;
    }
    return true;
  }

  public static void onTick() {
    p = mc.player;
    if (p == null || p.isDead() || p.isSpectator() || p.isSleeping())
      return;
    updateTarget();
    if (target == null)
      return;
    if (mc.currentScreen != null && mc.currentScreen instanceof HandledScreen)
      p.closeHandledScreen();
    if(!wasPathing && BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().isPathing()){
      wasPathing = true;
      BaritoneAPI.getProvider().getPrimaryBaritone().getCommandManager().execute("pause");
    }
    if (ClientPlayerHelper.lookingAt() != target) {
      ClientPlayerHelper.lookAt(target);
      ClientPlayerHelper.syncRotation();
    }
    if (handleCrit())
      attackTarget();
    if(needsBlock())
      blockShield();
    else unblockShield();
  }

  private static boolean needsBlock(){
    if(target instanceof PassiveEntity && !(target instanceof HoglinEntity))
      return false;
    if(target instanceof ProjectileEntity pe){
      return checkProjectile(pe);
    }
    return true;
  }

  private static boolean checkProjectile(ProjectileEntity pe){
    if(pe.getVelocity().lengthSquared() < 0.01)
      return false;
    var projTop = p.getPos().subtract(pe.getPos());
    var cosx = projTop.dotProduct(pe.getVelocity());
    if(cosx <= 0)
      return false;
    cosx /= projTop.length() * pe.getVelocity().length();
    return cosx > 0.5;
  }

  private static boolean handleCrit() {
    if(!(target instanceof LivingEntity))
      return false;
    var canCrit =
        !(p.isSubmergedInWater() || p.isClimbing() || p.isInLava());
    if (canCrit) {
      if (p.isOnGround() && target.getY() >= p.getY() - 1){
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
