package net.zenxarch.bot.command;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.*;
import static net.minecraft.command.CommandSource.suggestMatching;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.LiteralText;
import net.zenxarch.bot.defense.DefenseStateManager;
import net.zenxarch.bot.defense.Settings;
import net.zenxarch.bot.util.TargetUtil;

public class KillAuraCommand {
  private static boolean active = false;
  public static void register(
      CommandDispatcher<FabricClientCommandSource> dispatcher) {
    dispatcher.register(literal("zaura")
                            .then(generatePlayerCommand())
                            .then(generateSettings())
                            .executes(KillAuraCommand::toggleDefense));
  }

  private static LiteralArgumentBuilder<FabricClientCommandSource>
  generateSettings() {
    return literal("setting").then(argument("setting", word())
                                       .suggests((c, b) -> Settings.suggest(b))
                                       .executes(ctx -> {
                                         Settings.execute(
                                             getString(ctx, "setting"));
                                         return 0;
                                       }));
  }

  private static LiteralArgumentBuilder<FabricClientCommandSource>
  generatePlayerCommand() {
    return literal("player").then(
        argument("PlayerName", string())
            .suggests((c, b) -> suggestMatching(getPlayers(c.getSource()), b))
            .executes(ctx -> {
              var username = getString(ctx, "PlayerName");
              TargetUtil.handleUsername(username);
              if (TargetUtil.getUsernames().contains(username)) {
                sendMessage(ctx, "Currently targeting " + username + ";");
              }
              return 0;
            }));
  }

  private static ArrayList<String>
  getPlayers(FabricClientCommandSource source) {
    var result = new ArrayList<String>();
    for (var player : source.getWorld().getPlayers()) {
      if (player == source.getPlayer())
        continue;
      result.add(player.getEntityName());
    }
    return result;
  }

  private static int
  toggleDefense(CommandContext<FabricClientCommandSource> ctx) {
    active = !active;
    String text = active ? "Defense Activated" : "Defense Deactived";
    sendMessage(ctx, text);
    DefenseStateManager.setActiveStatus(active);
    return 0;
  }

  private static void sendMessage(CommandContext<FabricClientCommandSource> ctx,
                                  String message) {
    ctx.getSource().sendFeedback(new LiteralText(message));
  }
}