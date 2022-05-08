package net.zenxarch.bot.defense.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.zenxarch.bot.defense.Settings;

public class Module {
  public static final MinecraftClient mc = MinecraftClient.getInstance();
  private String name;

  public Module(String name) {
    Settings.registerModule(name);
    Settings.registerBoolSetting(name, "enabled", true);
    this.name = name;
  }

  public void preTick(){};
  public void handleNone(){};
  public void handleProjectile(ProjectileEntity pe){};
  public void handleHostile(MobEntity me){};
  public void handlePlayer(AbstractClientPlayerEntity pe){};
  public void handlePassive(MobEntity me){};

  public boolean isActive() {
    return Settings.getBoolean(this.name + ".enabled");
  }

  public String getName() { return name; }
}
