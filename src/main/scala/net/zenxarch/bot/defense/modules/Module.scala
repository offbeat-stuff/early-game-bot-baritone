package net.zenxarch.bot.defense.modules

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.zenxarch.bot.settings.Settings

abstract class Module(val name: String):

  Settings.registerSetting(name + ".enabled", true)

  def preTick() = {}
  def handleNone() = {}
  def handleProjectile(projectile: ProjectileEntity) = {}
  def handleHostile(mob: MobEntity) = {}
  def handlePlayer(player: AbstractClientPlayerEntity) = {}
  def handlePassive(mob: MobEntity) = {}

  def isActive() = Settings.getBoolean(this.name + ".enabled")

object Module:
  val mc = MinecraftClient.getInstance()
