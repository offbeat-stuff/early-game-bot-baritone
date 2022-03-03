package net.zenxarch.bot;

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.zenxarch.bot.command.*;

public class ZenCommandManager {
  public static void registerCommands() {
    CraftCommand.register(ClientCommandManager.DISPATCHER);
    PortalCommand.register(ClientCommandManager.DISPATCHER);
  }
}