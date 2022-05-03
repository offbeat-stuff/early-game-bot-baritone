package net.zenxarch.bot.command;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.*;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.ArrayList;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.LiteralText;
import net.zenxarch.bot.defense.DefenseStateManager;
import net.zenxarch.bot.util.TargetUtil;

public class KillAuraCommand {
  private static boolean active = false;
  public static void register(
      CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(
        literal("zaura")
            .then(argument("Username", StringArgumentType.string())
                      .executes(ctx -> {
                        var username =
                            StringArgumentType.getString(ctx, "Username");
                        TargetUtil.handleUsername(username);
                        if (TargetUtil.getUsernames().contains(username)) {
                          ctx.getSource().sendFeedback(new LiteralText(
                              "Currently targeting " + username + ";"));
                        }
                        return 0;
                      }))
            .executes(ctx -> {
              active = !active;
              if (active) {
                ctx.getSource().sendFeedback(
                    new LiteralText("KillAura activated;"));
              } else {
                ctx.getSource().sendFeedback(
                    new LiteralText("KillAura deactivated;"));
              }
              DefenseStateManager.setActiveStatus(active);
              return 0;
            }));
  }

  private ArrayList<LiteralArgumentBuilder> generateSettings() {
    var result = new ArrayList<LiteralArgumentBuilder>();
    return result;
  }
}
