package net.zenxarch.bot.command;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.*;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.zenxarch.bot.KillAura;
import net.zenxarch.bot.util.TargetUtil;

public class KillAuraCommand {
  public static void register(
      CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        literal("zuara")
            .then(argument("Username", StringArgumentType.string())
                      .executes(ctx -> {
                        TargetUtil.handleUsername(
                            StringArgumentType.getString(ctx, "Username"));
                        return 0;
                      }))
            .executes(ctx -> {
              KillAura.toggle();
              return 0;
            }));
  }
}
