package net.zenxarch.bot.defense.modules

import net.zenxarch.bot.defense.DefenseStateManager
import net.minecraft.util.math.BlockPos
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.LivingEntity
import net.zenxarch.bot.settings.Settings
import net.minecraft.client.network.AbstractClientPlayerEntity

abstract class BlockModule(name: String) extends Module(name):
  import Module.mc
  var lastPos: BlockPos

  override def handleNone() =
    internalHandleLastPos()

  override def handlePlayer(player: AbstractClientPlayerEntity) = handleLiving(
    player
  )
  override def handleHostile(mob: MobEntity) = handleLiving(mob)
  override def handlePassive(mob: MobEntity) = handleLiving(mob)

  def handleLiving(target: LivingEntity): Unit =
    if internalHandleLastPos() then return
    lastPos = null
    if canTarget(target) then
      if shouldTargetNearestBlock() then
        val nearest = findNearestBlockPos(target)
        DefenseStateManager.performAction(() =>
          nearest != null && handleBlock(nearest)
        )
      else if canUse(target.getBlockPos) then
        DefenseStateManager.performAction(() =>
          handleBlock(target.getBlockPos())
        )

  private def findNearestBlockPos(target: LivingEntity): BlockPos =
    val bb = target.getBoundingBox()
    val y = target.getBlockPos().getY()
    var bestDist = 4.5 * 4.5
    var bestPos: BlockPos = null
    for
      x <- Range(bb.minX.toInt, bb.maxX.toInt)
      z <- Range(bb.minZ.toInt, bb.maxZ.toInt)
    do
      val pos = new BlockPos(x, y, z)
      val dist = mc.player.squaredDistanceTo(x, y, z)
      if dist < bestDist && canUse(pos) then
        bestDist = dist
        bestPos = pos
    return bestPos

  private def internalHandleLastPos(): Boolean =
    if lastPos != null then
      DefenseStateManager.performAction(() => handleLastBlock(lastPos))
    lastPos != null

  def handleLastBlock(pos: BlockPos): Boolean

  def canTarget(target: LivingEntity): Boolean
  def canUse(pos: BlockPos): Boolean

  def shouldTargetNearestBlock(): Boolean

  def handleBlock(pos: BlockPos): Boolean
