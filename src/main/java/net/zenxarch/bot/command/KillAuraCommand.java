package net.zenxarch.bot.command;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.*;
import static net.minecraft.command.CommandSource.suggestMatching;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.ArrayList;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.LiteralText;
import net.zenxarch.bot.defense.DefenseStateManager;
import net.zenxarch.bot.defense.Settings.BooleanSetting;
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
    return literal("setting").then(
        argument("ModuleName", StringArgumentType.string())
            .suggests((c, b) -> suggestMatching(getModules(), b))
            .then(argument("SettingName", StringArgumentType.string())
                      .suggests((c, b) -> suggestMatching(getSettings(c), b))
                      .then(argument("SettingValue", BoolArgumentType.bool())
                                .executes(ctx -> runCommand(ctx)))));
  }

  private static LiteralArgumentBuilder<FabricClientCommandSource>
  generatePlayerCommand() {
    return literal("player").then(
        argument("PlayerName", StringArgumentType.string())
            .suggests((c, b) -> suggestMatching(getPlayers(c.getSource()), b))
            .executes(ctx -> {
              var username = StringArgumentType.getString(ctx, "PlayerName");
              TargetUtil.handleUsername(username);
              if (TargetUtil.getUsernames().contains(username)) {
                ctx.getSource().sendFeedback(
                    new LiteralText("Currently targeting " + username + ";"));
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
    ctx.getSource().sendFeedback(new LiteralText(text));
    DefenseStateManager.setActiveStatus(active);
    return 0;
  }

  private static int runCommand(CommandContext<?> ctx) {
    var moduleName = StringArgumentType.getString(ctx, "ModuleName");
    var settingName = StringArgumentType.getString(ctx, "SettingName");
    var settingValue = BoolArgumentType.getBool(ctx, "SettingValue");
    var setting =
        DefenseStateManager.getModule(moduleName).getSetting(settingName);
    if (setting instanceof BooleanSetting bs) {
      bs.set(settingValue);
    }
    return 1;
  }

  private static ArrayList<String> getModules() {
    var result = new ArrayList<String>();
    for (var module : DefenseStateManager.getModules()) {
      result.add(module.getName());
    }
    return result;
  }

  private static ArrayList<String> getSettings(CommandContext<?> source) {
    var moduleName = StringArgumentType.getString(source, "ModuleName");
    var module = DefenseStateManager.getModule(moduleName);
    if (module != null) {
      return module.getSettings().getSettings();
    }
    return new ArrayList<String>();
  }
}