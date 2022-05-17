package net.zenxarch.bot.mixin

import net.minecraft.entity.projectile.PersistentProjectileEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(Array(classOf[PersistentProjectileEntity]))
trait ProjectileEntityAccessor:
  @Accessor("field_7588") def getInGround(): Boolean
