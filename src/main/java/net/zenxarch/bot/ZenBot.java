package net.zenxarch.bot;

import baritone.k;
import java.util.ArrayList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.zenxarch.bot.task.MultiTask;
import net.zenxarch.bot.task.Task;
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
    var k = new KillAura();
    ClientTickEvents.END_CLIENT_TICK.register(mc -> {
      if (mc.world == null)
        return;
      if (k.needsControl())
        return;
      TASKQUEUE.onTick();
    });
    ClientTickEvents.START_CLIENT_TICK.register(mc -> {
      if (mc.world == null)
        return;
      k.onTick();
    });
  }

  public Logger getLogger() { return LOGGER; }
  public static ZenBot getInstance() { return getInstance(); };
  public static MultiTask getQueue() { return TASKQUEUE; }
}
