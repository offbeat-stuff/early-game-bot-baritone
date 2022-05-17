package net.zenxarch.bot.mixin;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PersistentProjectileEntity.class)
interface ProjectileEntityAccessor {
  @Accessor("inGround") boolean getInGround();
}
