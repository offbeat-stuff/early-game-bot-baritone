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

  private def toDegrees(a: Double, b: Double) =
    Math.toDegrees(Math.atan2(a, b)).toFloat

  def lookAt(x: Double, y: Double, z: Double) =
    val p = mc.player
    val dx = x - p.getX()
    val dy = y - p.getEyeY()
    val dz = z - p.getZ()
    val dh = Math.sqrt(dx * dx + dz * dz)
    p.setYaw(toDegrees(dz, dx) - 90)
    p.setPitch(-toDegrees(dy, dh))

  def lookAt(e: Entity): Unit = { lookAt(e.getX(), e.getEyeY(), e.getZ()) }

  def lookingAt(target: Entity): Boolean =
    if mc.crosshairTarget == null then return false
    if mc.crosshairTarget.getType() == Type.ENTITY then
      return (mc.crosshairTarget
        .asInstanceOf[EntityHitResult])
        .getEntity()
        .equals(target)
    return false

  def syncRotation() =
    val p = mc.player
    p.networkHandler.sendPacket(
      new PlayerMoveC2SPacket.LookAndOnGround(
        p.getYaw(),
        p.getPitch(),
        p.isOnGround()
      )
    )

  def setSelectedSlot(i: Int) =
    mc.player.getInventory().selectedSlot = i % 9
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
    val inv = mc.player.getInventory()
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
      i <- 0 until mc.player.getInventory().main.size()
      if test.apply(mc.player.getInventory().main.get(i))
    do return i
    if test.apply(mc.player.getInventory().offHand.get(0)) then
      return mc.player.getInventory().main.size()
    return -1

  def findInInventory(item: Item): Int = findInInventory(is => is.isOf(item))

  def pickItem(item: Item): Boolean =
    val slot = findInInventory(item)
    if slot == -1 then return false
    pickItemSlot(slot)
    return true

  def getRemainingAttackCooldownTicks(): Int =
    mc.player.getAttackCooldownProgressPerTick().toInt - (mc.player
      .asInstanceOf[PlayerEntityAccessor])
      .getLastAttackedTicks()

  def hitEntity(e: Entity) =
    val res = mc.interactionManager.attackEntity(mc.player, e)
    mc.player.swingHand(Hand.MAIN_HAND)
