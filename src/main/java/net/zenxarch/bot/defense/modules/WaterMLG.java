package net.zenxarch.bot.defense.modules;

import static net.zenxarch.bot.util.ClientPlayerHelper.*;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.World;
import net.zenxarch.bot.defense.DefenseStateManager;
import net.zenxarch.bot.util.BlockPlacementUtils;

public class WaterMLG extends Module {
  public WaterMLG() { super("WaterMLG"); }

  @Override
  public void preTick() {
    var saveItemSlot = findSaveItem();
    if (saveItemSlot == -1)
      return;

    if (mc.player.isOnGround() || mc.player.inPowderSnow ||
        mc.player.isTouchingWater())
      return;

    var blocks = getBlocksUntilLanding();
    if (mc.player.fallDistance + blocks < 3)
      return;

    var pos = mc.player.getBlockPos().down(blocks - 1);
    var hit =
        BlockPlacementUtils.raycastToBlockForPlacement(pos, FluidHandling.NONE);
    if (hit == null)
      return;
    DefenseStateManager.performAction(() -> {
      pickItemSlot(saveItemSlot);
      return BlockPlacementUtils.place(hit, Hand.MAIN_HAND);
    });
  }

  private int getBlocksUntilLanding() {
    var start = mc.player.getBlockY();
    var end = start - Math.min(start - mc.world.getBottomY(), 10);
    int i;
    for (i = mc.player.getBlockY(); i > end; i--) {
      var pos = new BlockPos(mc.player.getBlockX(), i, mc.player.getBlockZ());
      if (checkAir(pos))
        continue;
      if (safeToLand(pos))
        return -1;
      break;
    }
    return start - i;
  }

  private boolean checkAir(BlockPos pos) {
    var state = mc.world.getBlockState(pos);
    return state.getCollisionShape(mc.world, pos).isEmpty();
  }

  private boolean safeToLand(BlockPos pos) {
    return !mc.world.getFluidState(pos).isEmpty() ||
        mc.world.getBlockState(pos).isOf(Blocks.POWDER_SNOW);
  }

  private int findSaveItem() {
    var water = findInInventory(Items.WATER_BUCKET);
    if (water != -1 && mc.player.world.getRegistryKey() != World.NETHER) {
      return water;
    }
    return findInInventory(Items.POWDER_SNOW_BUCKET);
  }
}
