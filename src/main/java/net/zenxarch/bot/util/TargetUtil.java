package net.zenxarch.bot.util;

import java.util.ArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;

public class TargetUtil {
  private static ArrayList<LivingEntity> possibleTargets =
      new ArrayList<LivingEntity>(0);
  private static final MinecraftClient mc =
      MinecraftClient.getInstance();

  public static void handleEntityLoad(Entity e) {
    if (e instanceof LivingEntity le) {
      if (le == mc.player)
        return;
      possibleTargets.add(le);
    }
  }

  public static void handleEntityUnload(Entity e) {
    if (e instanceof LivingEntity le) {
      possibleTargets.remove(le);
    }
  }

  public static ArrayList<Pair<Double, LivingEntity>>
  getNearbyTargets() {
    var result = new ArrayList<Pair<Double, LivingEntity>>(0);
    var p = mc.player;
    possibleTargets.forEach(t -> {
      var d = dSq(t, p);
      if (d < 3.5 * 3.5)
        result.add(new Pair<Double, LivingEntity>(d, t));
    });
    return result;
  }

  private static double dSq(LivingEntity e, LivingEntity p) {
    var dx = e.getX() - p.getX();
    var dy = e.getY() - p.getY();
    var dz = e.getZ() - p.getZ();
    return dx * dx + dy * dy + dz * dz;
  }
}