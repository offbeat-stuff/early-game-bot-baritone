package net.zenxarch.bot;

import java.util.ArrayList;
import java.util.Arrays;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;

public final class KillAura {
  private static KillAura INSTANCE;

  private ArrayList<LivingEntity> targets;
  private ClientPlayerEntity p;
  private MinecraftClient mc;
  public KillAura() {
    INSTANCE = this;
    this.targets = new ArrayList<LivingEntity>(0);
    mc = MinecraftClient.getInstance();
  }

  public Double opD(LivingEntity e) {
    var xd = p.getX() - e.getX();
    var yd = p.getY() - e.getY();
    var zd = p.getZ() - e.getZ();
    return xd * xd + yd * yd + zd * zd;
  }

  private boolean isGood(LivingEntity e) {
    if(!e.isAlive() || e.isDead())
      return false;
    if(e == p)
      return false;
    if (opD(e) > 25)
      return false;
    if(e instanceof AnimalEntity && e.isBaby())
      return false;
    if(e instanceof EndermanEntity eman && !eman.isAngryAt(mc.player))
      return false;
    if(e instanceof PiglinEntity pig && !pig.isAngryAt(mc.player))
      return false;
    if(e instanceof GolemEntity || e instanceof VillagerEntity || e instanceof TameableEntity)
      return false;
    if(e instanceof PlayerEntity p && p.isCreative())
      return false;
    return true;
  }

  private void updateTargets() {
    targets = new ArrayList<LivingEntity>(0);
    mc.world.getEntities().forEach(e -> {
      if (e == null || !(e instanceof LivingEntity))
        return;
      if (isGood((LivingEntity)e)) {
        targets.add((LivingEntity)e);
      }
    });
    targets.sort((e1, e2) -> sort(e1, e2));
  }

  private void lookAt(double x, double y, double z) {
    double dx = x - p.getX();
    double dy = y - (p.getY() + p.getEyeHeight(p.getPose()));
    double dz = z - p.getZ();
    double dh = Math.sqrt(dx * dx + dz * dz);
    p.setYaw((float)Math.toDegrees(Math.atan2(dz, dx)) - 90);
    p.setPitch((float)-Math.toDegrees(Math.atan2(dy, dh)));
  }

  private void lookAt(LivingEntity e) {
    lookAt(e.getX(), e.getY() + e.getEyeHeight(e.getPose()),
           e.getZ());
  }

  public void syncRotation() {
    p.networkHandler.sendPacket(
        new PlayerMoveC2SPacket.LookAndOnGround(
            p.getYaw(), p.getPitch(), p.isOnGround()));
  }

  public boolean needsControl() {
    return targets.size() > 0;
  }

  public void onTick() {
    p = mc.player;
    if (p.isDead())
      return;
    if (targets.size() == 0)
      return;
    if (mc.currentScreen != null) {
      p.closeScreen();
    }
    updateTargets();
    var e = targets.get(0);
    if (lookingAt() == e) {
      handleTarget(e);
    } else {
      lookAt(targets.get(0));
      syncRotation();
    }
  }

  private void handleTarget(LivingEntity e){
    switchItem();
    if(e instanceof CreeperEntity && mc.player.getOffHandStack().getItem().equals(Items.SHIELD)){
      mc.interactionManager.interactItem(mc.player, mc.world , Hand.OFF_HAND);
      mc.player.swingHand(Hand.OFF_HAND);
      return;
    }
    mc.interactionManager.attackEntity(mc.player, e);
    mc.player.swingHand(Hand.OFF_HAND);
  }

  private void switchItem(){
    var items = new Item[]{
      Items.NETHERITE_AXE,Items.NETHERITE_SWORD,
      Items.DIAMOND_AXE,Items.DIAMOND_SWORD,
      Items.IRON_AXE,Items.IRON_SWORD,
      Items.STONE_AXE,Items.STONE_SWORD,
      Items.WOODEN_AXE,Items.WOODEN_SWORD
    };
    for(int i = 0;i < items.length;i++){
      for(int j = 0;j < 8;j++){
        if(p.getInventory().getStack(j).getItem() == items[i]){
          p.getInventory().selectedSlot = j;
        }
      }
    }
  }

  private LivingEntity lookingAt() {
    if (mc.crosshairTarget != null &&
        mc.crosshairTarget.getType() == Type.ENTITY) {
      var e = ((EntityHitResult)mc.crosshairTarget).getEntity();
      if (e instanceof LivingEntity) {
        return (LivingEntity)e;
      }
    }
    return null;
  }

  // TODO: remove recalc of distance
  private int sort(LivingEntity e1, LivingEntity e2) {
    return Double.compare(opD(e1), opD(e2));
  }

  public KillAura getInstance() { return INSTANCE; }
}