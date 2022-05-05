package net.zenxarch.bot.defense.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.zenxarch.bot.defense.Settings;

public class Module {
  protected Settings settings;
  public static final MinecraftClient mc = MinecraftClient.getInstance();
  private String name;

  public Module(String name) {
    this.settings = new Settings();
    this.name = name;
  }

  public void preTick(){};
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
