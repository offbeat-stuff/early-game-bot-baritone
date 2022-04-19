package net.zenxarch.bot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.zenxarch.bot.command.ZenCommandManager;
import net.zenxarch.bot.defense.AutoFire;
import net.zenxarch.bot.process.CraftProcess;
import org.apache.logging.log4j.*;

@Environment(EnvType.CLIENT)
public class ZenBot implements ClientModInitializer {
  // This logger is used to write text to the console and the log
  // file. It is considered best practice to use your mod id as the
  // logger's name. That way, it's clear which mod wrote info,
  // warnings, and errors.
  public static final Logger LOGGER = LogManager.getLogger("zenbot");

  private static ZenBot __instance;

  @Override
  public void onInitializeClient() {
    __instance = this;
    // This code runs as soon as Minecraft is in a mod-load-ready
    // state. However, some things (like resources) may still be
    // uninitialized. Proceed with mild caution.
    LOGGER.info("ZenBot loaded have fun botting.");
    ZenCommandManager.registerCommands();
    LOGGER.info("registered commands");
    ClientTickEvents.START_CLIENT_TICK.register(mc -> handleTickStart(mc));
    ClientTickEvents.END_CLIENT_TICK.register(mc -> handleTickEnd(mc));
  }

  private void handleTickStart(MinecraftClient mc) {
    if (mc.world == null)
      return;
    KillAura.onTick();
    AutoFire.preTick();
    CraftProcess.preTick();
  }

  private void handleTickEnd(MinecraftClient mc) {
    if (mc.world == null)
      return;
    if (KillAura.needsControl())
      return;
    CraftProcess.postTick();
  }

  public Logger getLogger() { return LOGGER; }
  public static ZenBot getInstance() { return __instance; };
}
