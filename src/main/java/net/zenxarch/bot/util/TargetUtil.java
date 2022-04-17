package net.zenxarch.bot.util;

import static net.zenxarch.bot.util.ProjectileEntitySimulator.*;

import java.util.ArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.zenxarch.bot.mixin.ProjectileEntityAccessor;

public class TargetUtil {
  private static MobEntity hostileTarget;
  private static MobEntity passiveTarget;
  private static ProjectileEntity projectileTarget;

  private static final MinecraftClient mc = MinecraftClient.getInstance();
  private static final ArrayList<EntityType> passiveTypes = new ArrayList<>() {
    {
      add(EntityType.COD);
      add(EntityType.SALMON);
      add(EntityType.COW);
      add(EntityType.SHEEP);
      add(EntityType.PIG);
      add(EntityType.CHICKEN);
      add(EntityType.RABBIT);
    }
  };

  private static ArrayList<String> playerUsernameStrings =
      new ArrayList<String>();

  public static void updateTargets() {
    hostileTarget = null;
    passiveTarget = null;
    projectileTarget = null;
    double hostileDist = 4.1 * 4.1;
    double passiveDist = 4.1 * 4.1;
    int projectileTicks = 16;
    for (Entity e : mc.world.getEntities()) {
      if (e == null || !e.isAlive())
        continue;
      if (e instanceof ProjectileEntity pe) {
        projectileTicks = handleProjectile(pe, projectileTicks);
        continue;
      }
      if (e instanceof MobEntity mob && !mob.isDead()) {
        if (checkHostile(mob)) {
          hostileDist = handleHostile(mob, hostileDist);
        }
        if (checkPassive(mob)) {
          passiveDist = handlePassive(mob, passiveDist);
        }
      }
    }
  }

  private static int handleProjectile(ProjectileEntity e, int d) {
    if (e instanceof ArrowEntity arrow &&
        !((ProjectileEntityAccessor)arrow).getInGround()) {
      var ticks = wouldHitPlayer(arrow, d);
      if (ticks < d) {
        projectileTarget = arrow;
        return ticks;
      }
    }
    return d;
  }

  private static double handleHostile(MobEntity e, double d) {
    var dist = mc.player.squaredDistanceTo(e);
    if (dist < d && checkVisibilty(e)) {
      hostileTarget = e;
      return dist;
    }
    return d;
  }

  private static double handlePassive(MobEntity e, double d) {
    var dist = mc.player.squaredDistanceTo(e);
    if (dist < d && checkVisibilty(e)) {
      passiveTarget = e;
      return dist;
    }
    return d;
  }

  public static MobEntity getNearestHostile() { return hostileTarget; }

  public static MobEntity getNearestPassive() { return passiveTarget; }

  public static ProjectileEntity getNearestProjectile() {
    return projectileTarget;
  }

  public static ArrayList<String> getUsernames() {
    return playerUsernameStrings;
  }

  public static void handleUsername(String s) {
    if (playerUsernameStrings.contains(s)) {
      playerUsernameStrings.remove(s);
    } else {
      playerUsernameStrings.add(s);
    }
  }

  public static AbstractClientPlayerEntity getNearestEnemyPlayer() {
    AbstractClientPlayerEntity result = null;
    var playerDistance = 4.1 * 4.1;
    for (AbstractClientPlayerEntity p : mc.world.getPlayers()) {
      if (!checkPlayer(p))
        continue;
      var dist = mc.player.squaredDistanceTo(p);
      if (dist < playerDistance && checkVisibilty(p)) {
        playerDistance = dist;
        result = p;
      }
    }
    return result;
  }

  private static boolean checkPlayer(AbstractClientPlayerEntity p) {
    if (p == null || !p.isAlive() || p.isDead())
      return false;
    if (p.isSpectator() || p.isCreative())
      return false;
    return playerUsernameStrings.contains(p.getEntityName());
  }

  private static boolean checkHostile(MobEntity e) {
    /* if (e instanceof EndermanEntity eman && !eman.isAngry())
      return false;
    if (e.isAttacking() && !(e instanceof PhantomEntity))
      return false; */
    if (e instanceof PiglinEntity)
      return false;
    return e instanceof EnderDragonEntity || e instanceof FlyingEntity ||
        e instanceof SlimeEntity || e instanceof HostileEntity ||
        e instanceof HoglinEntity;
  }

  private static boolean checkPassive(MobEntity e) {
    return passiveTypes.contains(e.getType()) &&
        !(e instanceof AnimalEntity animal && animal.isBaby());
  }

  private static boolean checkVisibilty(LivingEntity e) {
    return mc.player.canSee(e);
  }
}
