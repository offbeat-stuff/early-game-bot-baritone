package net.zenxarch.bot.command;

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;

public class ZenCommandManager {
  public static void registerCommands() {
    CraftCommand.register(ClientCommandManager.DISPATCHER);
    PortalCommand.register(ClientCommandManager.DISPATCHER);
  }
}