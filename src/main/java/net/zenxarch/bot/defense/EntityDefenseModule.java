package net.zenxarch.bot.defense;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;

public class EntityDefenseModule {
  private Settings settings;
  private String name;

  public EntityDefenseModule() { this.settings = new Settings(); }

  public void handleNone(){};
  public void handleProjectile(ProjectileEntity pe){};
  public void handleHostile(MobEntity me){};
  public void handlePlayer(AbstractClientPlayerEntity pe){};
  public void handlePassive(MobEntity me){};

  public Settings.Setting getSetting(String name) { this.settings.get(name); }

  public Settings getSettings() { return settings; }
}
