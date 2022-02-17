package net.zenxarch.bot;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.item.Item;
import net.zenxarch.bot.command.ZCraftCommand;
import net.zenxarch.bot.process.ICraftProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class EarlyGameBotMod implements ClientModInitializer {
  // This logger is used to write text to the console and the log
  // file. It is considered best practice to use your mod id as the
  // logger's name. That way, it's clear which mod wrote info,
  // warnings, and errors.
  public static final Logger LOGGER =
      LoggerFactory.getLogger("zenbot");

  ICraftProcess craftProcess;
  ZCraftCommand zcommand;

  @Override
  public void onInitializeClient() {
    // This code runs as soon as Minecraft is in a mod-load-ready
    // state. However, some things (like resources) may still be
    // uninitialized. Proceed with mild caution.
    LOGGER.info("ZenBot loaded have fun botting.");
    zcommand = new ZCraftCommand();
    zcommand.register(ClientCommandManager.DISPATCHER, this);
    LOGGER.info("registered command zcraft");
    craftProcess = new ICraftProcess();
    ClientTickEvents.END_CLIENT_TICK.register(
        mc -> { craftProcess.tick(mc); });
  }

  public void startCraftProcess(Item item) {
    craftProcess.activate(item);
  }

  public static Logger getLogger() { return LOGGER; }
}
