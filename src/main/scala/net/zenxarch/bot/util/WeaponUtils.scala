package net.zenxarch.bot.util

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.entity.EquipmentSlot
import java.util.UUID
import net.minecraft.entity.attribute.EntityAttributeModifier
import scala.jdk.CollectionConverters._
import net.minecraft.entity.EntityGroup
import net.zenxarch.bot.ZenBot.mc
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.TridentItem
import net.minecraft.item.SwordItem
import net.minecraft.item.MiningToolItem
import net.minecraft.item.ShovelItem
import net.minecraft.item.AxeItem
import net.minecraft.item.ToolMaterials._
import net.minecraft.item.PickaxeItem
import net.minecraft.item.HoeItem

object WeaponUtils:
  def getAttackDamageModifier(item: Item): Float =
    item match
      case t: TridentItem    => 8
      case s: SwordItem      => s.getAttackDamage
      case m: MiningToolItem => m.getAttackDamage
      case _                 => 0

  def getAttackSpeedModifierAxe(item: AxeItem): Float =
    item.getMaterial match
      case WOOD | STONE               => -3.2
      case GOLD | DIAMOND | NETHERITE => -3
      case IRON                       => -3.1
      case _                          => 0

  def getAttackSpeedModifierHoe(item: HoeItem): Float =
    item.getMaterial match
      case WOOD | GOLD => -3
      case STONE       => -2
      case IRON        => -1
      case _           => 0

  def getAttackSpeedModifier(item: Item): Float =
    item match
      case t: TridentItem => -2.9
      case s: SwordItem   => -2.4
      case s: ShovelItem  => -3
      case a: AxeItem     => getAttackSpeedModifierAxe(a)
      case p: PickaxeItem => -2.8
      case h: HoeItem     => getAttackSpeedModifierHoe(h)
      case _              => 0

  def getBaseAttackDamage(): Float = mc.player
    .getAttributeBaseValue(
      EntityAttributes.GENERIC_ATTACK_DAMAGE
    )
    .toFloat

  def getBaseAttackSpeed(): Float = mc.player
    .getAttributeBaseValue(
      EntityAttributes.GENERIC_ATTACK_SPEED
    )
    .toFloat

  def getAttackDamage(
      stack: ItemStack,
      group: EntityGroup = EntityGroup.DEFAULT
  ): Float =
    val baseDmg = getBaseAttackDamage()
    if stack.isEmpty then return baseDmg
    var itemDmg = getAttackDamageModifier(stack.getItem)
    if itemDmg == 0 then return baseDmg
    itemDmg += EnchantmentHelper.getAttackDamage(stack, group)
    return baseDmg + itemDmg

  def getAttackSpeed(stack: ItemStack): Float =
    val baseSpeed = getBaseAttackSpeed()
    if stack.isEmpty then return baseSpeed
    var speed = getAttackSpeedModifier(stack.getItem)
    return baseSpeed + speed

  def getBaseAttackDamagePerSec() = getBaseAttackDamage() / getBaseAttackSpeed()

  def getAttackDamagePerSec(stack: ItemStack, target: EntityGroup): Float =
    return getAttackDamage(stack, target) / getAttackSpeed(stack)
