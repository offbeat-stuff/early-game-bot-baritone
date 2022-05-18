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

object WeaponUtils:
  def getAttributeValueById(
      values: Iterator[EntityAttributeModifier],
      attribId: UUID
  ): Double =
    for
      attrib <- values
      if attrib.getId == attribId
    do return attrib.getValue
    return 0

  def getAttackDamage(item: Item) =
    getAttributeValueById(
      item
        .getAttributeModifiers(EquipmentSlot.MAINHAND)
        .values
        .iterator
        .asScala,
      Item.ATTACK_DAMAGE_MODIFIER_ID
    )

  def getAttackSpeed(item: Item) =
    getAttributeValueById(
      item
        .getAttributeModifiers(EquipmentSlot.MAINHAND)
        .values
        .iterator
        .asScala,
      Item.ATTACK_SPEED_MODIFIER_ID
    )

  def getAttackDamage(
      stack: ItemStack,
      group: EntityGroup = EntityGroup.DEFAULT
  ): Double =
    if stack.isEmpty then return 0
    var itemDmg = getAttackDamage(stack.getItem)
    if itemDmg == 0 then return 0
    itemDmg += mc.player.getAttributeBaseValue(
      EntityAttributes.GENERIC_ATTACK_DAMAGE
    )
    itemDmg += EnchantmentHelper.getAttackDamage(stack, group)
    return itemDmg

  def getAttackSpeed(stack: ItemStack): Double =
    if stack.isEmpty then return 1
    var speed = getAttackSpeed(stack.getItem)
    if speed == 0 then return 1
    speed += mc.player.getAttributeBaseValue(
      EntityAttributes.GENERIC_ATTACK_SPEED
    )
    return speed

  def getAttackDamagePerSec(stack: ItemStack, target: EntityGroup): Double =
    return getAttackDamage(stack, target) / getAttackSpeed(stack)
