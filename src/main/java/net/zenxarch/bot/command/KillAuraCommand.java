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
                                .executes(ctx -> runModuleSettingFull(ctx)))
                      .executes(ctx -> runModuleSettingHalf(ctx)))
            .executes(ctx -> runModuleSettingEmpty(ctx)));
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
    sendMessage(ctx, text);
    DefenseStateManager.setActiveStatus(active);
    return 0;
  }

  private static int
  runModuleSettingEmpty(CommandContext<FabricClientCommandSource> ctx) {
    return handleArgs(ctx, StringArgumentType.getString(ctx, "ModuleName"), "",
                      false, false);
  }

  private static int
  runModuleSettingHalf(CommandContext<FabricClientCommandSource> ctx) {
    var moduleName = StringArgumentType.getString(ctx, "ModuleName");
    var settingName = StringArgumentType.getString(ctx, "SettingName");
    return handleArgs(ctx, moduleName, settingName, false, false);
  }

  private static int
  runModuleSettingFull(CommandContext<FabricClientCommandSource> ctx) {
    var moduleName = StringArgumentType.getString(ctx, "ModuleName");
    var settingName = StringArgumentType.getString(ctx, "SettingName");
    var settingValue = BoolArgumentType.getBool(ctx, "SettingValue");
    return handleArgs(ctx, moduleName, settingName, true, settingValue);
  }

  private static int handleArgs(CommandContext<FabricClientCommandSource> ctx,
                                String moduleName, String settingName,
                                boolean valueGiven, boolean value) {
    var module = DefenseStateManager.getModule(moduleName);
    if (module == null)
      return 1;
    if (settingName == "") {
      for (var setting : module.getSettings().getSettings()) {
        sendMessage(ctx, setting + " : " + module.getSetting(setting).get());
      }
      return 0;
    }

    var setting = module.getSetting(settingName);
    if (!valueGiven) {
      sendMessage(ctx,
                  settingName + " : " + module.getSetting(settingName).get());
      return 0;
    }

    if (setting instanceof BooleanSetting bs) {
      bs.set(value);
    }
    return 0;
  }

  private static void sendMessage(CommandContext<FabricClientCommandSource> ctx,
                                  String message) {
    ctx.getSource().sendFeedback(new LiteralText(message));
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