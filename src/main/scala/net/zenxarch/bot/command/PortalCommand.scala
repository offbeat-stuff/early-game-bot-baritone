package net.zenxarch.bot.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager._
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource as fs
import net.minecraft.command.argument.BlockPosArgumentType

class PortalCommand extends ZenCommand:
  def register() = literal("zportal")
    .`then`(
      argument("pos", BlockPosArgumentType.blockPos())
        .executes(this.execute)
    )

  private def execute(ctx: CommandContext[fs]): Int =
    // BlockPos pos = ctx.getArgument("pos", PosArgument.class)
    // mod.portalProcess.activate(pos)
    return 0
