package net.zenxarch.bot.util;

import static net.zenxarch.bot.ZenBot.mc;

import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class BlockPlacementUtils {

  public static BlockHitResult raycastToBlockForPlacement(BlockPos pos,
                                                          FluidHandling f) {
    var vpos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    vpos = vpos.add(0.5, -0.02, 0.5);
    var result = praycast(vpos, f);
    if (result.getType() == HitResult.Type.BLOCK) {
      var bhit = (BlockHitResult)result;
      if (bhit.getBlockPos() == pos.down() && bhit.getSide() == Direction.UP) {
        return bhit;
      }
    }
    return null;
  }

  public static boolean place(BlockHitResult hit, Hand hand) {
    boolean wasSneaking = mc.player.input.sneaking;
    mc.player.input.sneaking = false;
    var res =
        mc.interactionManager.interactBlock(mc.player, mc.world, hand, hit);
    if (res.shouldSwingHand())
      mc.player.swingHand(hand);
    mc.player.input.sneaking = wasSneaking;
    return res.isAccepted();
  }

  private static HitResult praycast(Vec3d v, FluidHandling f) {
    var start = mc.player.getEyePos();
    return mc.world.raycast(
        new RaycastContext(start, v, ShapeType.COLLIDER, f, mc.player));
  }
}