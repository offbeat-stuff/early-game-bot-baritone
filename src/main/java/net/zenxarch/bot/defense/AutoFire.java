package net.zenxarch.bot.defense;

import static net.zenxarch.bot.util.ClientPlayerHelper.*;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.zenxarch.bot.util.BlockPlacementUtils;
import net.zenxarch.bot.util.ClientPlayerHelper;

public class AutoFire {
  private static final MinecraftClient mc = MinecraftClient.getInstance();

  public static void preTick(LivingEntity target) {
    var pos = target.getBlockPos();
    if (mc.player.getY() <
        (double)pos.getY() - mc.player.getEyeHeight(mc.player.getPose()))
      return;

    var blockState = mc.world.getBlockState(pos);
    var fluid = mc.world.getFluidState(pos).getFluid();

    if (fluid != null && fluid != Fluids.EMPTY) {
      if (fluid == Fluids.LAVA) {
        if (ClientPlayerHelper.pickItem(Items.BUCKET))
          BlockPlacementUtils.tryItemUseAt(pos);
      }
      return;
    }

    var block = blockState.getBlock();

    // TODO: Check if the upside is visible
    if (block instanceof AbstractFireBlock) {
      mc.interactionManager.attackBlock(pos, Direction.UP);
      return;
    }

    if (target.isOnFire())
      return;

    if (pickItem(Items.FLINT_AND_STEEL) && canPlaceFireAt(pos))
      if (BlockPlacementUtils.tryPlaceAt(pos))
        return;
    if (pickItem(Items.LAVA_BUCKET) && blockState.canBucketPlace(Fluids.LAVA))
      BlockPlacementUtils.tryPlaceAt(pos);
  }

  private static boolean canPlaceFireAt(BlockPos pos) {
    return pos != null &&
        mc.world.getBlockState(pos).getMaterial().isReplaceable() &&
        AbstractFireBlock.getState(mc.world, pos).canPlaceAt(mc.world, pos);
  }
}
