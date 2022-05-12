package net.zenxarch.bot.command

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager._

import com.mojang.brigadier.CommandDispatcher
import dev.xpple.clientarguments.arguments.CBlockStateArgumentType._
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource
import net.zenxarch.bot.process.MineProcess

class MineCommand extends ZenCommand {
  def register() = literal("zmine").`then`(
    argument("block", blockState())
      .executes((ctx) => {
        MineProcess.mineBlock(
          getCBlockState(ctx, "block")
            .getBlock()
        )
        0
      })
  )
}
