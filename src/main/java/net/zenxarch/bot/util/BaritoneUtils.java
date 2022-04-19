package net.zenxarch.bot.util;

import baritone.api.BaritoneAPI;

public class BaritoneUtils {
  private static boolean wasPathing = false;

  public static void pausePathing() {
    if (!wasPathing && BaritoneAPI.getProvider()
                           .getPrimaryBaritone()
                           .getPathingBehavior()
                           .isPathing()) {
      wasPathing = true;
      BaritoneAPI.getProvider()
          .getPrimaryBaritone()
          .getCommandManager()
          .execute("pause");
    }
  }

  public static void resumePathing() {
    if (wasPathing && BaritoneAPI.getProvider()
                          .getPrimaryBaritone()
                          .getPathingBehavior()
                          .isPathing()) {
      BaritoneAPI.getProvider()
          .getPrimaryBaritone()
          .getCommandManager()
          .execute("resume");
      wasPathing = false;
    }
  }
}
