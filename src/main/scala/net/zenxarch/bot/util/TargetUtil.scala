package net.zenxarch.bot.util

import net.zenxarch.bot.util.ProjectileEntitySimulator._

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.dragon.EnderDragonEntity
import net.minecraft.entity.mob.EndermanEntity
import net.minecraft.entity.mob.FlyingEntity
import net.minecraft.entity.mob.HoglinEntity
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.mob.SlimeEntity
import net.minecraft.entity.passive.AnimalEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.util.hit.HitResult
import net.minecraft.world.RaycastContext
import net.minecraft.world.RaycastContext.FluidHandling
import net.minecraft.world.RaycastContext.ShapeType
import net.zenxarch.bot.settings.Settings
import net.zenxarch.bot.mixin.ProjectileEntityAccessor
import net.zenxarch.bot.ZenBot.mc
import scala.jdk.CollectionConverters._
import scala.collection.mutable.ListBuffer

object TargetUtil {
  private var hostileTarget: MobEntity = null
  private var passiveTarget: MobEntity = null
  private var projectileTarget: ProjectileEntity = null
  private var playerTarget: AbstractClientPlayerEntity = null

  private val passiveTypes: List[EntityType[?]] = List(
    EntityType.COD,
    EntityType.SALMON,
    EntityType.COW,
    EntityType.SHEEP,
    EntityType.PIG,
    EntityType.CHICKEN,
    EntityType.RABBIT
  )

  private val nuetralTypes =
    List(EntityType.SPIDER, EntityType.PIGLIN, EntityType.ZOMBIFIED_PIGLIN)

  private val playerUsernameStrings = new ListBuffer[String]()

  def init() = {
    Settings.registerSetting(
      "targets.maxReach",
      4.5
    )
    Settings.registerSetting("targets.ignorePassive", false)
  }

  private def ignorePassive(): Boolean = {
    return Settings.getBoolean("targets.ignorePassive")
  }

  private def maxReach(): Double = {
    return Settings.getDouble("targets.maxReach")
  }

  def updateTargets() = {
    val reach = maxReach()
    hostileTarget = null
    passiveTarget = null
    projectileTarget = null
    var hostileDist = reach * reach
    var passiveDist = reach * reach
    var projectileTicks = 16
    for
      e <- mc.world.getEntities().asScala
      if e != null && e.isAlive()
    do
      e match {
        case pe: ProjectileEntity => {
          projectileTicks = handleProjectile(pe, projectileTicks)
        }
        case mob: MobEntity if !mob.isDead() => {
          if (checkHostile(mob)) {
            hostileDist = handleHostile(mob, hostileDist)
          }
          if (!ignorePassive() && checkPassive(mob)) {
            passiveDist = handlePassive(mob, passiveDist)
          }
        }
      }
    // find nearest enemy player
    playerTarget = null
    var playerDistance = reach * reach
    for
      p <- mc.world.getPlayers().asScala
      if checkPlayer(p)
    do
      val dist = checkSquaredDistanceTo(p)
      if (dist < playerDistance && checkVisibilty(p)) {
        playerDistance = dist
        playerTarget = p
      }
  }

  private def handleProjectile(e: ProjectileEntity, d: Int): Int = {
    e match {
      case arrow: ArrowEntity
          if !arrow.asInstanceOf[ProjectileEntityAccessor].getInGround() => {
        val ticks = wouldHitPlayer(arrow, d)
        if (ticks < d) {
          projectileTarget = arrow
          return ticks
        }
      }
    }
    return d
  }

  private def handleHostile(e: MobEntity, d: Double): Double = {
    val dist = checkSquaredDistanceTo(e)
    if (dist < d && checkVisibilty(e)) {
      hostileTarget = e
      return dist
    }
    return d
  }

  private def handlePassive(e: MobEntity, d: Double): Double = {
    val dist = checkSquaredDistanceTo(e)
    if (dist < d && checkVisibilty(e)) {
      passiveTarget = e
      return dist
    }
    return d
  }

  def getNearestHostile() = hostileTarget

  def getNearestPassive() = passiveTarget

  def getNearestProjectile() = projectileTarget

  def getUsernames() = playerUsernameStrings

  def handleUsername(s: String) = {
    if (playerUsernameStrings.contains(s)) {
      playerUsernameStrings -= s
    } else {
      playerUsernameStrings += s
    }
  }

  def getNearestEnemyPlayer() = playerTarget

  private def checkPlayer(p: AbstractClientPlayerEntity) =
    p != null && p.isAlive() && !p.isDead() &&
      !(p.isSpectator() || p.isCreative()) && playerUsernameStrings.contains(
        p.getEntityName()
      )

  private def checkHostile(e: MobEntity): Boolean = {
    e match {
      case eman: EndermanEntity => {
        return eman.isAngry()
      }
    }
    if (nuetralTypes.contains(e.getType()))
      return e.isAttacking()
    return e.isInstanceOf[EnderDragonEntity] || e.isInstanceOf[FlyingEntity] ||
      e.isInstanceOf[SlimeEntity] || e.isInstanceOf[HostileEntity] ||
      e.isInstanceOf[HoglinEntity]
  }

  private def checkPassive(e: MobEntity) = passiveTypes.contains(e.getType()) &&
    !(e.isInstanceOf[AnimalEntity] && (e.asInstanceOf[AnimalEntity]).isBaby())

  private def checkSquaredDistanceTo(e: Entity) =
    e.squaredDistanceTo(mc.player.getEyePos())

  private def checkVisibilty(e: LivingEntity): Boolean = {
    var start = mc.player.getEyePos()
    var end = e.getPos()
    return mc.world
      .raycast(
        new RaycastContext(
          start,
          end,
          ShapeType.COLLIDER,
          FluidHandling.NONE,
          mc.player
        )
      )
      .getType() == HitResult.Type.MISS
  }
}
