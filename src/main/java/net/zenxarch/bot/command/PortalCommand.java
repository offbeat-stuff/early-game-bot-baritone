package net.zenxarch.bot.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;

public class PortalCommand {

  public static void register(
      CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(ClientCommandManager.literal("zportal").then(
        ClientCommandManager.argument("pos", BlockPosArgumentType.blockPos())
            .executes(ctx -> execute(ctx))));
  }

  private static int execute(CommandContext<FabricClientCommandSource> ctx) {
    // BlockPos pos = ctx.getArgument("pos", PosArgument.class);
    // mod.portalProcess.activate(pos);
    return 0;
  }
}
