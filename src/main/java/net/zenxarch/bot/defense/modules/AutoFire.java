package net.zenxarch.bot.defense.modules;

import static net.zenxarch.bot.defense.DefenseStateManager.performAction;
import static net.zenxarch.bot.util.ClientPlayerHelper.*;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.zenxarch.bot.defense.Settings;
import net.zenxarch.bot.util.BlockPlacementUtils;

public class AutoFire extends Module {
  private BlockPos lastPos = null;

  public AutoFire() {
    super("AutoFire");
    this.settings.addSetting(
        new Settings.BooleanSetting("shouldUseComplexMethod", true));
  }

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

    if (shouldUseComplexMethod())
      performAction(() -> canBurn(target) && complexFlintAndSteel(target));
    else if (performAction(() -> simpleFlintAndSteel(target)))
      lastPos = target.getBlockPos();
  }

  private boolean shouldUseComplexMethod() {
    if (settings.get("shouldUseComplexMethod") instanceof
        Settings.BooleanSetting bs) {
      return bs.get();
    }
    return false;
  }

  private boolean canBurn(LivingEntity target) {
    if (target == null)
      return false;
    if (mc.player.getEyeY() < target.getBlockY())
      return false;
    return !(target.isFireImmune() || target.isOnFire() || target.isWet() ||
             target.inPowderSnow);
  }

  private boolean complexFlintAndSteel(LivingEntity target) {
    var slot = findInInventory(Items.FLINT_AND_STEEL);
    if (slot == -1)
      return false;

    var minx = MathHelper.floor(target.getBoundingBox().minX);
    var minz = MathHelper.floor(target.getBoundingBox().minZ);
    var maxx = MathHelper.ceil(target.getBoundingBox().maxX);
    var maxz = MathHelper.ceil(target.getBoundingBox().maxZ);
    var y = target.getBlockY();

    var bestDist = 4.1 * 4.1;
    BlockPos bestPos = null;

    for (int x = (int)minx; x <= (int)maxx; x++) {
      for (int z = (int)minz; z <= (int)maxz; z++) {
        var dist = mc.player.squaredDistanceTo(x, y, z);
        var pos = new BlockPos(x, y, z);
        if (dist < bestDist && canPlaceFireAt(pos)) {
          bestDist = dist;
          bestPos = pos;
        }
      }
    }

    if (bestPos != null) {
      pickItemSlot(slot);
      if (simpleFlintAndSteel(bestPos)) {
        lastPos = bestPos;
        return true;
      };
    }

    return false;
  }

  private boolean simpleFlintAndSteel(LivingEntity target) {
    return canBurn(target) && pickItem(Items.FLINT_AND_STEEL) &&
        simpleFlintAndSteel(target.getBlockPos());
  }

  private boolean simpleFlintAndSteel(BlockPos pos) {
    var hit =
        BlockPlacementUtils.raycastToBlockForPlacement(pos, FluidHandling.ANY);
    if (hit == null)
      return false;
    return BlockPlacementUtils.place(hit, Hand.MAIN_HAND);
  }

  private boolean tryExtinguish() {
    if (lastPos != null && mc.world.getBlockState(lastPos).getBlock() instanceof
                               AbstractFireBlock) {
      mc.interactionManager.attackBlock(lastPos.down(), Direction.UP);
      return true;
    }
    return false;
  }

  private boolean canPlaceFireAt(BlockPos pos) {
    return pos != null && mc.world.getFluidState(pos).isEmpty() &&
        mc.world.getBlockState(pos).getMaterial().isReplaceable() &&
        AbstractFireBlock.getState(mc.world, pos).canPlaceAt(mc.world, pos);
  }
}
