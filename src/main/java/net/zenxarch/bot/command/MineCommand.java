package net.zenxarch.bot.command;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.*;

import com.mojang.brigadier.CommandDispatcher;
import dev.xpple.clientarguments.arguments.CBlockStateArgumentType;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.zenxarch.bot.process.MineProcess;

public class MineCommand {
  public static void register(
      CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(literal("zmine").then(
        argument("block", CBlockStateArgumentType.blockState())
            .executes(ctx -> {
              MineProcess.mineBlock(
                  CBlockStateArgumentType.getCBlockState(ctx, "block")
                      .getBlock());
              return 0;
            })));
  }
}
