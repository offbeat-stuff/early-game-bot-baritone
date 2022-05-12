package net.zenxarch.bot

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.zenxarch.bot.command.ZenCommandManager
import net.zenxarch.bot.defense.DefenseStateManager
import net.zenxarch.bot.process.CraftProcess
import org.apache.logging.log4j._

@Environment(EnvType.CLIENT)
object ZenBot extends ClientModInitializer {
  val LOGGER = LogManager.getLogger("zenbot");
  val mc = MinecraftClient.getInstance();

  override def onInitializeClient(): Unit = {
    LOGGER.info("ZenBot loaded have fun botting.");
    DefenseStateManager.init();
    ZenCommandManager.registerCommands();
    LOGGER.info("registered commands");
    ClientTickEvents.START_CLIENT_TICK.register(mc => handleTickStart());
    ClientTickEvents.END_CLIENT_TICK.register(mc => handleTickEnd());
  }

  private def handleTickStart(): Unit = {
    if (isPlayerUnsafeToControl())
      return;
    DefenseStateManager.preTick();
    CraftProcess.preTick();
  }

  private def isPlayerUnsafeToControl(): Boolean = {
    mc.world == null || mc.player == null || mc.player.isDead() ||
    mc.player.isSpectator() || mc.player.isSleeping() ||
    mc.player.isRiding();
  }

  private def handleTickEnd(): Unit = {
    if (isPlayerUnsafeToControl() || DefenseStateManager.postTickCheck())
      return;
    CraftProcess.postTick();
  }
}
