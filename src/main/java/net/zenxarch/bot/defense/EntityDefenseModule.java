package net.zenxarch.bot.defense;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;

public class EntityDefenseModule {
  public void handleNone(){};
  public void handleProjectile(ProjectileEntity pe){};
  public void handleHostile(MobEntity me){};
  public void handlePlayer(AbstractClientPlayerEntity pe){};
  public void handlePassive(MobEntity me){};
}
