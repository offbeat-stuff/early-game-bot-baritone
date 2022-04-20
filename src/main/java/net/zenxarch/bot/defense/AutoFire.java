package net.zenxarch.bot.defense;

import static net.zenxarch.bot.util.ClientPlayerHelper.*;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.SoulFireBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.zenxarch.bot.KillAura;
import net.zenxarch.bot.util.BlockPlacementUtils;

public class AutoFire {
  private static final MinecraftClient mc = MinecraftClient.getInstance();
  private static BlockPos lastPos = null;

  public static void preTick() {
    if (tryExtinguish())
      return;
    lastPos = null;

    var target = KillAura.getTarget();
    if (target == null)
      return;
    var pos = target.getBlockPos();

    if (mc.player.getEyeY() < target.getY())
      return;
    if (!mc.world.getFluidState(pos).isEmpty())
      return;

    if (target.isOnFire())
      return;
    if (target instanceof LivingEntity le)
      lastPos = tryFlintAndSteel(le);
  }

  private static BlockPos tryFlintAndSteel(LivingEntity target) {
    if (!pickItem(Items.FLINT_AND_STEEL))
      return null;
    var minx = target.getBoundingBox().getMax(Axis.X);
    var minz = target.getBoundingBox().getMin(Axis.Z);
    var maxx = target.getBoundingBox().getMax(Axis.X);
    var maxz = target.getBoundingBox().getMax(Axis.Z);
    var y = target.getBlockY();

    var bestDist = 4.1 * 4.1;
    BlockPos bestPos = null;

    for (int x = (int)minx; x <= (int)maxx; x++) {
      for (int z = (int)minz; z <= (int)maxz; z++) {
        var dist = mc.player.squaredDistanceTo(x, y, z);
        var pos = new BlockPos(x, y, z);
        if (dist > bestDist)
          continue;
        if (mc.world.getFluidState(pos).isEmpty())
          continue;
        if (canPlaceFireAt(pos)) {
          bestDist = dist;
          bestPos = pos;
        }
      }
    }
    if (bestPos != null) {
      BlockPlacementUtils.tryPlaceAt(bestPos);
    }
    return bestPos;
  }

  private static boolean tryExtinguish() {
    if (lastPos == null)
      return false;
    var block = mc.world.getBlockState(lastPos).getBlock();
    if (block instanceof AbstractFireBlock) {
      mc.interactionManager.attackBlock(lastPos.down(), Direction.UP);
      return true;
    }
    return false;
  }

  private static boolean canPlaceFireAt(BlockPos pos) {
    if (pos == null ||
        !mc.world.getBlockState(pos).getMaterial().isReplaceable())
      return false;
    var down = mc.world.getBlockState(pos.down());
    if (SoulFireBlock.isSoulBase(down))
      return true;
    return FireBlock.canPlaceAt(mc.world, pos, Direction.UP);
  }
}
