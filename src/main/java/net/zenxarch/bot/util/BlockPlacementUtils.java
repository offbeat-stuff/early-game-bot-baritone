package net.zenxarch.bot.util;

import net.minecraft.client.MinecraftClient;
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
  private static final MinecraftClient mc = MinecraftClient.getInstance();

  public static boolean tryPlaceAt(BlockPos pos) {
    var vpos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    var hit = praycast(vpos.add(0.5, -1.1, 0.5), FluidHandling.ANY);
    if (hit.getType() != HitResult.Type.BLOCK)
      return false;
    var bhit = (BlockHitResult)hit;
    if (!bhit.getBlockPos().equals(pos.add(0, -1.1, 0)) ||
        !bhit.getSide().equals(Direction.UP))
      return false;
    var res = mc.interactionManager.interactBlock(mc.player, mc.world,
                                                  Hand.MAIN_HAND, bhit);
    if (res.shouldSwingHand())
      mc.player.swingHand(Hand.MAIN_HAND);
    return res.isAccepted();
  }

  public static boolean tryItemUseAt(BlockPos pos) {
    var vpos = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    var hit = praycast(vpos.add(0.5, -0.5, 0.5), FluidHandling.SOURCE_ONLY);
    if (hit.getType() != HitResult.Type.BLOCK)
      return false;
    var bhit = (BlockHitResult)hit;
    if (bhit.getBlockPos() != pos)
      return false;
    var res = mc.interactionManager.interactBlock(mc.player, mc.world,
                                                  Hand.MAIN_HAND, bhit);
    if (res.shouldSwingHand())
      mc.player.swingHand(Hand.MAIN_HAND);
    return res.isAccepted();
  }

  private static HitResult praycast(Vec3d v, FluidHandling f) {
    var start = mc.player.getPos().add(
        0, mc.player.getEyeHeight(mc.player.getPose()), 0);
    return mc.world.raycast(
        new RaycastContext(start, v, ShapeType.COLLIDER, f, mc.player));
  }
}