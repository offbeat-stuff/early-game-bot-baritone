package net.zenxarch.bot.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
// import
// net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.zenxarch.bot.mixin.PlayerEntityAccessor;

public class ClientPlayerHelper {
  private static final MinecraftClient mc = MinecraftClient.getInstance();

  public static void lookAt(double x, double y, double z) {
    var p = mc.player;
    double dx = x - p.getX();
    double dy = y - (p.getY() + p.getEyeHeight(p.getPose()));
    double dz = z - p.getZ();
    double dh = Math.sqrt(dx * dx + dz * dz);
    p.setYaw((float)Math.toDegrees(Math.atan2(dz, dx)) - 90);
    p.setPitch((float)-Math.toDegrees(Math.atan2(dy, dh)));
  }

  public static void lookAt(Entity e) {
    lookAt(e.getX(), e.getEyeY(), e.getZ());
  }

  public static boolean lookingAt(Entity target) {
    if (mc.crosshairTarget == null)
      return false;
    if (mc.crosshairTarget.getType() == Type.ENTITY)
      return ((EntityHitResult)mc.crosshairTarget).getEntity() == target;
    return false;
  }

  public static void syncRotation() {
    var p = mc.player;
    p.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
        p.getYaw(), p.getPitch(), p.isOnGround()));
  }

  public static void setSelectedSlot(int i) {
    var p = mc.player;
    p.getInventory().selectedSlot = i % 9;
    // p.networkHandler.sendPacket(new
    // UpdateSelectedSlotC2SPacket(p.getInventory().selectedSlot));
  }

  private static void swapHands() {
    mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
        PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN,
        Direction.DOWN));
  }

  public static void pickItemSlot(int slot) {
    var inv = mc.player.getInventory();
    if (slot == inv.main.size()) {
      swapHands();
    }
    if (slot >= inv.main.size() || slot < 0)
      return;
    if (slot < 9) {
      setSelectedSlot(slot);
      return;
    }
    int swappable = inv.getSwappableHotbarSlot();
    if (swappable != inv.selectedSlot)
      setSelectedSlot(swappable);
    mc.interactionManager.pickFromInventory(slot);
  }

  public static int findInInventory(Item item) {
    var inv = mc.player.getInventory();
    for (int i = 0; i < inv.main.size(); i++) {
      if (inv.main.get(i).getItem().equals(item)) {
        return i;
      }
    }
    if (inv.offHand.get(0).getItem().equals(item)) {
      return inv.main.size();
    }
    return -1;
  }

  public static boolean pickItem(Item item) {
    int slot = findInInventory(item);
    if (slot == -1)
      return false;
    pickItemSlot(slot);
    return true;
  }

  public static int getRemainingAttackCooldownTicks() {
    return (int)mc.player.getAttackCooldownProgressPerTick() -
        ((PlayerEntityAccessor)mc.player).getLastAttackedTicks();
  }

  public static void hitEntity(Entity e) {
    mc.interactionManager.attackEntity(mc.player, e);
    mc.player.swingHand(Hand.MAIN_HAND);
  }
}