package net.zenxarch.bot.defense;

import static net.zenxarch.bot.ZenBot.mc;
import static net.zenxarch.bot.defense.DefenseStateManager.performAction;
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
    performAction(this::tryExtinguish);
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
    if (performAction(this::tryExtinguish))
      return;
    lastPos = null;
    if (performAction(() -> simpleFlintAndSteel(target)))
      lastPos = target.getBlockPos();
  }

  private boolean canBurn(LivingEntity target) {
    if (target == null)
      return false;
    if (mc.player.getEyeY() < target.getBlockY())
      return false;
    return !(target.isFireImmune || target.isOnFire() || target.isWet() ||
             target.isInPowderSnow());
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

  private boolean simpleFlintAndSteel(LivingEntity target) {
    return canBurn(target) && pickItem(Items.FLINT_AND_STEEL) &&
        BlockPlacementUtils.tryPlaceAt(target.getBlockPos());
  }

  private boolean tryExtinguish() {
    if (lastPos != null && mc.world.getBlockState(lastPos).getBlock() instanceof
                               AbstractFireBlock) {
      mc.interactionManager.attackBlock(lastPos.down(), Direction.UP);
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
