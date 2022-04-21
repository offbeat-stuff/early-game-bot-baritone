package net.zenxarch.bot.defense;

import static net.zenxarch.bot.util.ClientPlayerHelper.*;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
// import net.minecraft.util.math.Direction.Axis;
// import net.minecraft.util.math.MathHelper;
import net.zenxarch.bot.KillAura;
import net.zenxarch.bot.util.BlockPlacementUtils;

public class AutoFire {
  private static final MinecraftClient mc = MinecraftClient.getInstance();
  private static BlockPos lastPos = null;

  public static void preTick() {
    if (KillAura.getAttacked() || tryExtinguish())
      return;
    lastPos = null;

    var target = KillAura.getTarget();
    if (target == null)
      return;

    var fluid = mc.world.getFluidState(target.getBlockPos());

    if (mc.player.getEyeY() < target.getY() || !fluid.isEmpty())
      return;

    if (target.isOnFire())
      return;

    if (target instanceof LivingEntity le)
      lastPos = simpleFlintAndSteel(le);
  }

  /* private static BlockPos tryFlintAndSteel(LivingEntity target) {
    var slot = findInInventory(Items.FLINT_AND_STEEL);
    if (slot == -1)
      return null;
    var minx = MathHelper.floor(target.getBoundingBox().getMin(Axis.X));
    var minz = MathHelper.floor(target.getBoundingBox().getMin(Axis.Z));
    var maxx = MathHelper.ceil(target.getBoundingBox().getMax(Axis.X));
    var maxz = MathHelper.ceil(target.getBoundingBox().getMax(Axis.Z));
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
      pickItemSlot(slot);
      BlockPlacementUtils.tryPlaceAt(bestPos);
    }
    return bestPos;
  }
  */

  private static BlockPos simpleFlintAndSteel(LivingEntity target) {
    if (!pickItem(Items.FLINT_AND_STEEL))
      return null;
    var pos = target.getBlockPos();
    BlockPlacementUtils.tryPlaceAt(pos);
    return pos;
  }

  private static boolean tryExtinguish() {
    if (lastPos == null)
      return false;
    var block = mc.world.getBlockState(lastPos).getBlock();
    if (block instanceof AbstractFireBlock) {
      mc.interactionManager.attackBlock(lastPos, Direction.UP);
      return true;
    }
    return false;
  }
  /*
  private static boolean canPlaceFireAt(BlockPos pos) {
    return pos != null &&
        mc.world.getBlockState(pos).getMaterial().isReplaceable() &&
        AbstractFireBlock.getState(mc.world, pos).canPlaceAt(mc.world, pos);
  }
  */
}
