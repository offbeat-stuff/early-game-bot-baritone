package net.zenxarch.bot.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.zenxarch.bot.EarlyGameBotMod;

public class ZCraftCommand {
  EarlyGameBotMod mod;

  public void register(
      CommandDispatcher<FabricClientCommandSource> Dispatcher,
      EarlyGameBotMod imod) {
    mod = imod;
    Dispatcher.register(ClientCommandManager.literal("zcraft").then(
        ClientCommandManager
            .argument("item", ItemStackArgumentType.itemStack())
            .executes(
                ctx
                -> this.execute(ctx.getSource(),
                                ItemStackArgumentType
                                    .getItemStackArgument(ctx, "item")
                                    .getItem()))));
  }

  private int execute(FabricClientCommandSource source, Item item) {
    mod.startCraftProcess(item);
    return 0;
  }
}
