package net.zenxarch.bot.util;

import java.util.ArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.Pair;

public class TargetUtil {
  private static ArrayList<Entity> possibleTargets =
      new ArrayList<Entity>(0);
  private static final MinecraftClient mc =
      MinecraftClient.getInstance();

  public static void handleEntityLoad(Entity e) {
    if (e instanceof LivingEntity) {
      if (e == mc.player)
        return;
      possibleTargets.add(e);
    }
    if(e instanceof ProjectileEntity){
      possibleTargets.add(e);
    }
  }

  public static void handleEntityUnload(Entity e) {
    if (e instanceof LivingEntity le) {
      possibleTargets.remove(le);
    }
  }

  public static ArrayList<Pair<Double, Entity>>
  getNearbyTargets() {
    var result = new ArrayList<Pair<Double, Entity>>(0);
    var p = mc.player;
    possibleTargets.forEach(t -> {
      var d = dSq(t, p);
      if (d < 4.0 * 4.0)
        result.add(new Pair<Double, Entity>(d, t));
    });
    return result;
  }

  private static double dSq(Entity e, LivingEntity p) {
    var dx = e.getX() - p.getX();
    var dy = e.getY() - p.getY();
    var dz = e.getZ() - p.getZ();
    return dx * dx + dy * dy + dz * dz;
  }
}