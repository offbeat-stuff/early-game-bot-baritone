package net.zenxarch.bot.defense;

import static net.zenxarch.bot.util.ClientPlayerHelper.*;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.zenxarch.bot.util.BlockPlacementUtils;
import net.zenxarch.bot.util.ClientPlayerHelper;

public class AutoFire {
  private static final MinecraftClient mc = MinecraftClient.getInstance();

  public static void preTick(LivingEntity target) {
    if (!target.isOnGround())
      return;
    var pos = target.getBlockPos();
    if (mc.player.getY() <
        (double)pos.getY() - mc.player.getEyeHeight(mc.player.getPose()))
      return;

    var blockState = mc.world.getBlockState(pos);
    var fluid = mc.world.getFluidState(pos).getFluid();

    if (fluid == null || fluid != Fluids.EMPTY) {
      if (fluid == Fluids.LAVA) {
        if (ClientPlayerHelper.pickItem(Items.BUCKET))
          BlockPlacementUtils.tryItemUseAt(pos);
        return;
      }
    }

    var block = blockState.getBlock();

    // TODO: Check if the upside is visible
    if (block instanceof FireBlock) {
      mc.interactionManager.attackBlock(pos, Direction.UP);
      return;
    }

    if (pickItem(Items.FLINT_AND_STEEL) &&
        AbstractFireBlock.canPlaceAt(mc.world, pos, Direction.UP))
      if (BlockPlacementUtils.tryPlaceAt(pos))
        return;
    if (pickItem(Items.LAVA_BUCKET) && blockState.canBucketPlace(Fluids.LAVA))
      BlockPlacementUtils.tryPlaceAt(pos);
  }
}
