package net.zenxarch.bot.defense;

import static net.zenxarch.bot.ZenBot.mc;
import static net.zenxarch.bot.util.ClientPlayerHelper.*;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.zenxarch.bot.util.BlockPlacementUtils;

public class AutoFire extends EntityDefenseModule {
  private BlockPos lastPos = null;

  @Override
  public void handleNone() {
    tryExtinguish();
  }

  @Override
  public void handleHostile(MobEntity me) {
    handleTarget(me);
  }

  @Override
  public void handlePlayer(AbstractClientPlayerEntity pe) {
    handleTarget(pe);
  }

  @Override
  public void handlePassive(MobEntity me) {
    handleTarget(me);
  }

  private void handleTarget(LivingEntity target) {
    if (tryExtinguish())
      return;
    lastPos = null;

    if (target == null)
      return;

    var fluid = mc.world.getFluidState(target.getBlockPos());

    if (mc.player.getEyeY() < target.getY() || !fluid.isEmpty() ||
        target.isOnFire())
      return;

    lastPos = simpleFlintAndSteel(target);
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

  private BlockPos simpleFlintAndSteel(LivingEntity target) {
    if (!pickItem(Items.FLINT_AND_STEEL))
      return null;
    if (!DefenseStateManager.canPerformAction())
      return null;
    var pos = target.getBlockPos();
    BlockPlacementUtils.tryPlaceAt(pos);
    return pos;
  }

  private boolean tryExtinguish() {
    if (lastPos == null)
      return false;
    var block = mc.world.getBlockState(lastPos).getBlock();
    if (block instanceof AbstractFireBlock &&
        DefenseStateManager.canPerformAction()) {
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
