package net.zenxarch.bot.command

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource

object ZenCommandManager:
  val commands = List[ZenCommand](
    new CraftCommand(),
    new PortalCommand(),
    new KillAuraCommand(),
    new MineCommand()
  )
  def registerCommands() =
    commands.foreach(r =>
      ClientCommandManager.DISPATCHER.register(r.register())
    )

abstract class ZenCommand:
  def register(): LiteralArgumentBuilder[FabricClientCommandSource]
