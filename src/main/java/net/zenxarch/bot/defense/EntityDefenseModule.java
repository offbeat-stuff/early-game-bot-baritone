package net.zenxarch.bot.defense;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;

public class EntityDefenseModule {
  protected Settings settings;
  private String name;

  public EntityDefenseModule(String name) {
    this.settings = new Settings();
    this.name = name;
  }

  public void handleNone(){};
  public void handleProjectile(ProjectileEntity pe){};
  public void handleHostile(MobEntity me){};
  public void handlePlayer(AbstractClientPlayerEntity pe){};
  public void handlePassive(MobEntity me){};

  public Settings.Setting<?> getSetting(String name) {
    return settings.get(name);
  }

  public Settings getSettings() { return settings; }

  public String getName() { return name; }
}
