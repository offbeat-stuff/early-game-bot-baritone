package net.zenxarch.bot.process;

import static net.zenxarch.bot.util.ClientPlayerHelper.findInInventory;

import net.minecraft.block.Block;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.zenxarch.bot.util.BaritoneUtils;

public class MineProcess {
  public static void mineBlock(Block target) {
    if (!canMine(target))
      return;
    BaritoneUtils.mine(target);
  }

  private static boolean canMine(Block target) {
    if (checkTag(target, BlockTags.PICKAXE_MINEABLE)) {
      if (findInInventory(Items.NETHERITE_PICKAXE) != -1 ||
          findInInventory(Items.DIAMOND_PICKAXE) != -1)
        return true;
      if (checkTag(target, BlockTags.NEEDS_DIAMOND_TOOL))
        return false;
      if (findInInventory(Items.IRON_PICKAXE) != -1)
        return true;
      if (checkTag(target, BlockTags.NEEDS_IRON_TOOL))
        return false;
      if (findInInventory(Items.STONE_PICKAXE) != -1)
        return true;
      return !checkTag(target, BlockTags.NEEDS_STONE_TOOL) &&
          findInInventory(Items.WOODEN_PICKAXE) != -1;
    }
    return true;
  }

  private static boolean checkTag(Block target, TagKey<Block> tag) {
    return target.getRegistryEntry().isIn(tag);
  }
}
