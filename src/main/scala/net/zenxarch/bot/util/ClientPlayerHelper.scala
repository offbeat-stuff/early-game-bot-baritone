package net.zenxarch.bot.util

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.Hand
// import
// net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult.Type
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.zenxarch.bot.mixin.PlayerEntityAccessor

import net.zenxarch.bot.ZenBot.mc
import net.minecraft.item.ItemStack

object ClientPlayerHelper:

  @inline private def p = mc.player
  @inline private def inv = p.getInventory()
  @inline private def cross = mc.crosshairTarget

  @inline private def toDegrees(a: Double, b: Double) =
    Math.toDegrees(Math.atan2(a, b)).toFloat

  def lookAt(x: Double, y: Double, z: Double) =
    val dx = x - p.getX()
    val dy = y - p.getEyeY()
    val dz = z - p.getZ()
    val dh = Math.sqrt(dx * dx + dz * dz)
    p.setYaw(toDegrees(dz, dx) - 90)
    p.setPitch(-toDegrees(dy, dh))

  def lookAt(e: Entity): Unit =
    lookAt(e.getX(), e.getEyeY(), e.getZ())

  def lookingAt(target: Entity): Boolean =
    if cross == null then return false
    if cross.getType() == Type.ENTITY then
      return (cross
        .asInstanceOf[EntityHitResult])
        .getEntity()
        .equals(target)
    return false

  def syncRotation() =
    p.networkHandler.sendPacket(
      new PlayerMoveC2SPacket.LookAndOnGround(
        p.getYaw(),
        p.getPitch(),
        p.isOnGround()
      )
    )

  def setSelectedSlot(i: Int) =
    inv.selectedSlot = i % 9
    // p.networkHandler.sendPacket(new
    // UpdateSelectedSlotC2SPacket(p.getInventory().selectedSlot))

  private def swapHands() =
    mc.getNetworkHandler()
      .sendPacket(
        new PlayerActionC2SPacket(
          PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
          BlockPos.ORIGIN,
          Direction.DOWN
        )
      )

  def pickItemSlot(slot: Int): Unit =
    if slot == inv.main.size() then
      swapHands()
      return
    if slot >= inv.main.size() || slot < 0 then return
    if slot < 9 then
      setSelectedSlot(slot)
      return
    val swappable = inv.getSwappableHotbarSlot()
    if swappable != inv.selectedSlot then setSelectedSlot(swappable)
    mc.interactionManager.pickFromInventory(slot)

  def findInInventory(test: (ItemStack) => Boolean): Int =
    for
      i <- 0 until inv.main.size()
      if test.apply(inv.main.get(i))
    do return i
    if test.apply(inv.offHand.get(0)) then return inv.main.size()
    return -1

  def findBestInInventory(pref: (ItemStack) => Double): Int =
    var bestSlot = -1
    var best = 0.0
    for i <- 0 until inv.main.size
    do
      val preference = pref(inv.main.get(i))
      if preference > best then
        best = preference
        bestSlot = i
    if pref(inv.offHand.get(0)) > best then return inv.main.size
    return bestSlot

  def findInInventory(item: Item): Int = findInInventory(is => is.isOf(item))

  def pickItem(item: Item): Boolean =
    val slot = findInInventory(item)
    if slot == -1 then return false
    pickItemSlot(slot)
    return true

  def getRemainingAttackCooldownTicks(): Float =
    p.getAttackCooldownProgressPerTick() - (mc.player
      .asInstanceOf[PlayerEntityAccessor])
      .getLastAttackedTicks()
      .toFloat

  def hitEntity(e: Entity) =
    val res = mc.interactionManager.attackEntity(mc.player, e)
    mc.player.swingHand(Hand.MAIN_HAND)
