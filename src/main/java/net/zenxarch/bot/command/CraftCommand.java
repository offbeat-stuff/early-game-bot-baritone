package net.zenxarch.bot.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.zenxarch.bot.ZenBot;
import net.zenxarch.bot.task.CraftTask;
import net.zenxarch.bot.task.GetToScreenTask;
import net.zenxarch.bot.util.RecipeUtil;

public final class CraftCommand {

  public static void register(
      CommandDispatcher<FabricClientCommandSource> Dispatcher) {
    Dispatcher.register(ClientCommandManager.literal("zcraft").then(
        ClientCommandManager
            .argument("item", ItemStackArgumentType.itemStack())
            .executes(
                ctx
                -> execute(ctx.getSource(),
                           ItemStackArgumentType
                               .getItemStackArgument(ctx, "item")
                               .getItem()))));
  }

  private static int execute(FabricClientCommandSource source,
                             Item item) {
    var r = RecipeUtil.findRecipe(item);
    if (r == null) {
      return 1;
    }
    var rt = r.getType();
    Block b = Blocks.CRAFTING_TABLE;
    if (rt == RecipeType.CRAFTING) {
      b = Blocks.CRAFTING_TABLE;
    } else if (rt == RecipeType.BLASTING) {
      b = Blocks.BLAST_FURNACE;
    } else if (rt == RecipeType.SMOKING) {
      b = Blocks.SMOKER;
    } else if (rt == RecipeType.SMELTING) {
      b = Blocks.FURNACE;
    } else if (rt == RecipeType.STONECUTTING) {
      b = Blocks.STONECUTTER;
    } else if (rt == RecipeType.SMITHING) {
      b = Blocks.SMITHING_TABLE;
    }
    ZenBot.getQueue().addTask(new GetToScreenTask(b));
    ZenBot.getQueue().addTask(new CraftTask(r));
    return 0;
  }
}
