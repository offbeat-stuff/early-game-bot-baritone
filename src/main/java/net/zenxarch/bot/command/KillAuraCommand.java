package net.zenxarch.bot.command;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
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
import net.zenxarch.bot.defense.EntityDefenseModule;
import net.zenxarch.bot.defense.Settings.BooleanSetting;
import net.zenxarch.bot.defense.Settings.Setting;
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
    var m = "ModuleName";
    var s = "SettingName";
    var v = "SettingValue";
    var sv = argument(v, bool()).executes(ctx
                                          -> handleArgs(ctx, getString(ctx, m),
                                                        getString(ctx, s), true,
                                                        getBool(ctx, v)));
    var sc = argument(s, word())
                 .suggests((c, b) -> suggestMatching(getSettings(c, m), b))
                 .then(sv)
                 .executes(ctx
                           -> handleArgs(ctx, getString(ctx, m),
                                         getString(ctx, s), false, false));
    var mc =
        argument(m, word())
            .suggests((c, b) -> suggestMatching(getModules(), b))
            .then(sc)
            .executes(
                ctx -> handleArgs(ctx, getString(ctx, m), "", false, false));
    return literal("setting").then(mc).executes(ctx -> {
      for (var module : DefenseStateManager.getModules()) {
        echoModuleSettings(ctx, module);
      }
      return 0;
    });
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

  private static int handleArgs(CommandContext<FabricClientCommandSource> ctx,
                                String moduleName, String settingName,
                                boolean valueGiven, boolean value) {
    var module = DefenseStateManager.getModule(moduleName);
    if (module == null)
      return 1;
    if (settingName == "") {
      echoModuleSettings(ctx, module);
      return 0;
    }

    var setting = module.getSetting(settingName);
    if (!valueGiven) {
      echoSettingValue(ctx, setting);
      return 0;
    }

    if (setting instanceof BooleanSetting bs) {
      bs.set(value);
    }
    return 0;
  }

  private static void
  echoModuleSettings(CommandContext<FabricClientCommandSource> ctx,
                     EntityDefenseModule module) {
    for (var setting : module.getSettings().getSettings()) {
      echoSettingValue(ctx, module.getSetting(setting));
    }
  }

  private static void
  echoSettingValue(CommandContext<FabricClientCommandSource> ctx,
                   Setting<?> setting) {
    sendMessage(ctx, setting.getName() + " : " + setting.get());
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

  private static ArrayList<String> getSettings(CommandContext<?> source,
                                               String m) {
    var moduleName = getString(source, m);
    var module = DefenseStateManager.getModule(moduleName);
    if (module != null) {
      return module.getSettings().getSettings();
    }
    return new ArrayList<String>();
  }
}