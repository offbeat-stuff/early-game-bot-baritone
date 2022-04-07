package net.zenxarch.bot.command;

import baritone.api.BaritoneAPI;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.zenxarch.bot.process.CraftProcess;
import net.zenxarch.bot.util.RecipeUtil;

public final class CraftCommand {

  public static void register(
      CommandDispatcher<FabricClientCommandSource> Dispatcher) {
    Dispatcher.register(ClientCommandManager.literal("zcraft").then(
        ClientCommandManager
            .argument("item", ItemStackArgumentType.itemStack())
            .then(
                ClientCommandManager
                    .argument("amount", IntegerArgumentType.integer())
                    .executes(
                        ctx
                        -> execute(
                            ctx.getSource(),
                            ItemStackArgumentType
                                .getItemStackArgument(ctx, "item")
                                .getItem(),
                            IntegerArgumentType.getInteger(
                                ctx, "amount"))))));
  }

  private static int execute(FabricClientCommandSource source,
                             Item item, Integer amt) {
    Block b = Blocks.CRAFTING_TABLE;
    RecipeUtil.recache();
    if (trySmelt(item, amt)) {
      b = Blocks.FURNACE;
    } else if (tryCraft(item, amt)) {
      b = Blocks.CRAFTING_TABLE;
    }
    BaritoneAPI.getProvider()
        .getPrimaryBaritone()
        .getGetToBlockProcess()
        .getToBlock(b);
    return 0;
  }

  private static boolean trySmelt(Item item, Integer amt) {
    var r = RecipeUtil.findSmeltingRecipes(item);
    if (r.size() == 0)
      return false;
    for (var rp : r) {
      if (CraftProcess.checkMaxCraftable(rp) >= amt) {
        CraftProcess.enqueue(rp, amt);
      }
    }
    return false;
  }

  private static boolean tryCraft(Item item, Integer amt) {
    var r = RecipeUtil.findCraftingRecipes(item);
    if (r.size() == 0)
      return false;
    for (var rp : r) {
      if (CraftProcess.checkMaxCraftable(rp) >= amt) {
        CraftProcess.enqueue(rp, amt);
        return true;
      }
    }
    return false;
  }
}
