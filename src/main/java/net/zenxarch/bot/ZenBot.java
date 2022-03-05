package net.zenxarch.bot;

import java.util.ArrayList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.zenxarch.bot.command.ZenCommandManager;
import net.zenxarch.bot.task.MultiTask;
import net.zenxarch.bot.task.Task;
import net.zenxarch.bot.util.TargetUtil;
import org.apache.http.impl.client.TargetAuthenticationStrategy;
import org.apache.logging.log4j.*;

@Environment(EnvType.CLIENT)
public class ZenBot implements ClientModInitializer {
  // This logger is used to write text to the console and the log
  // file. It is considered best practice to use your mod id as the
  // logger's name. That way, it's clear which mod wrote info,
  // warnings, and errors.
  public static final Logger LOGGER = LogManager.getLogger("zenbot");

  private static MultiTask TASKQUEUE =
      new MultiTask("Task Queue", new ArrayList<Task>());

  private static ZenBot INSTANCE;

  @Override
  public void onInitializeClient() {
    INSTANCE = this;
    // This code runs as soon as Minecraft is in a mod-load-ready
    // state. However, some things (like resources) may still be
    // uninitialized. Proceed with mild caution.
    LOGGER.info("ZenBot loaded have fun botting.");
    ZenCommandManager.registerCommands();
    LOGGER.info("registered commands");
    ClientEntityEvents.ENTITY_LOAD.register(
        (e, w) -> handleEntityLoad(e, w));
    ClientEntityEvents.ENTITY_UNLOAD.register(
        (e, w) -> handleEntityUnload(e, w));
    ClientTickEvents.START_CLIENT_TICK.register(
        mc -> handleTickStart(mc));
    ClientTickEvents.END_CLIENT_TICK.register(
        mc -> handleTickEnd(mc));
  }

  private void handleTickStart(MinecraftClient mc) {
    if (mc.world == null)
      return;
    KillAura.onTick();
  }

  private void handleTickEnd(MinecraftClient mc) {
    if (mc.world == null)
      return;
    if (KillAura.needsControl())
      return;
    TASKQUEUE.onTick();
  }

  private void handleEntityLoad(Entity e, World w) {
    TargetUtil.handleEntityLoad(e);
  }

  private void handleEntityUnload(Entity e, World w) {
    TargetUtil.handleEntityUnload(e);
  }

  public Logger getLogger() { return LOGGER; }
  public static ZenBot getInstance() { return getInstance(); };
  public static MultiTask getQueue() { return TASKQUEUE; }
}
