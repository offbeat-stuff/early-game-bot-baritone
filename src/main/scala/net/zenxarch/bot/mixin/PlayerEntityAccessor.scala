package net.zenxarch.bot.mixin

import net.minecraft.entity.LivingEntity
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.gen.Accessor

@Mixin(Array(classOf[LivingEntity]))
trait PlayerEntityAccessor {
  @Accessor("field_6273") def getLastAttackedTicks(): Int
}
