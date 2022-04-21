package net.zenxarch.bot.util;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import net.minecraft.block.Block;

public class BaritoneUtils {
  private static boolean wasPathing = false;

  private static IBaritone getPrimaryBaritone() {
    return BaritoneAPI.getProvider().getPrimaryBaritone();
  }

  private static void execute(String arg0) {
    getPrimaryBaritone().getCommandManager().execute(arg0);
  }

  public static void pausePathing() {
    if (!wasPathing && getPrimaryBaritone().getPathingBehavior().isPathing()) {
      wasPathing = true;
      execute("pause");
    }
  }

  public static void resumePathing() {
    if (wasPathing && getPrimaryBaritone().getPathingBehavior().isPathing()) {
      execute("resume");
      wasPathing = false;
    }
  }

  public static void stopPathing() {
    if (getPrimaryBaritone().getPathingBehavior().hasPath()) {
      execute("stop");
    }
  }

  public static void mine(Block target) {
    stopPathing();
    getPrimaryBaritone().getMineProcess().mine(target);
  }
}
