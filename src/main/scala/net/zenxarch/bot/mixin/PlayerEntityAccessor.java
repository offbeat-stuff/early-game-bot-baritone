package net.zenxarch.bot.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
interface PlayerEntityAccessor {
  @Accessor Int getLastAttackedTicks();
}
