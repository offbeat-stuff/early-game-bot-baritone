package net.zenxarch.bot.util;

import java.util.ArrayList;
import java.util.function.Predicate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.zenxarch.bot.mixin.ProjectileEntityAccessor;

public class TargetUtil {
  private static final ArrayList<MobEntity> hostiles =
      new ArrayList<MobEntity>();
  private static final ArrayList<PassiveEntity> passives =
      new ArrayList<PassiveEntity>();
  private static final ArrayList<ProjectileEntity> projectiles =
      new ArrayList<ProjectileEntity>();
  private static final ArrayList<OtherClientPlayerEntity> players =
      new ArrayList<OtherClientPlayerEntity>();
  private static final MinecraftClient mc = MinecraftClient.getInstance();
  private static final EntityType[] __passiveTypes = {
      EntityType.COD, EntityType.SALMON,  EntityType.COW,   EntityType.SHEEP,
      EntityType.PIG, EntityType.CHICKEN, EntityType.RABBIT};

  private static ArrayList<String> playerUsernameStrings =
      new ArrayList<String>();

  public static void handleEntityLoad(Entity e) {
    if (e instanceof LivingEntity le) {
      if (e == mc.player)
        return;
      handleLiving(le);
    }
    if (e instanceof ArrowEntity pe) {
      projectiles.add(pe);
    }
  }

  private static void handleLiving(LivingEntity e) {
    if (e instanceof OtherClientPlayerEntity other)
      players.add(other);
    if (!(e instanceof MobEntity mob))
      return;
    if (e instanceof HostileEntity || e instanceof HoglinEntity ||
        e instanceof SlimeEntity) {
      hostiles.add(mob);
    }
    if (!(e instanceof PassiveEntity pe))
      return;
    for (int i = 0; i < __passiveTypes.length; i++) {
      if (e.getType().equals(__passiveTypes[i])) {
        passives.add(pe);
      }
    }
  }

  public static void handleEntityUnload(Entity e) {
    if (e instanceof MobEntity)
      hostiles.remove(e);
    if (e instanceof PassiveEntity)
      passives.remove(e);
    if (e instanceof ProjectileEntity)
      projectiles.remove(e);
    if (e instanceof OtherClientPlayerEntity)
      players.remove(e);
  }

  public static MobEntity getNearestHostile() {
    hostiles.removeIf(e -> e == null || !e.isAlive() || e.isDead());
    return findNearest(hostiles, 4.0,
                       e -> checkHostile(e), e -> mc.player.canSee(e));
  }

  public static PassiveEntity getNearestPassive() {
    passives.removeIf(e -> e == null || !e.isAlive() || e.isDead());
    return findNearest(passives, 4.0, e -> {
      if (e instanceof AnimalEntity a) {
        return !a.isBaby();
      }
      return true;
    }, e -> mc.player.canSee(e));
  }

  public static ProjectileEntity getNearestProjectile() {
    // projectiles.removeIf(p -> p == null);
    return findNearest(
        projectiles, 12,
        e
        -> { return !(((ProjectileEntityAccessor)e).getInGround()); },
        e -> {
          if (!(e instanceof ArrowEntity arrow))
            return false;
          return ProjectileEntitySimulator.wouldHitPlayer(arrow, 10);
        });
  }

  public static void handleUsername(String s) {
    if (playerUsernameStrings.contains(s)) {
      playerUsernameStrings.remove(s);
    } else {
      playerUsernameStrings.add(s);
    }
  }

  public static OtherClientPlayerEntity getNearestEnemyPlayer() {
    return findNearest(
        players, 4 * 4,
        e
        -> { return playerUsernameStrings.contains(e.getEntityName()); },
        e -> { return checkPlayer(e); });
  }

  public static boolean checkPlayer(OtherClientPlayerEntity p) {
    if (p.isSpectator() || p.isCreative())
      return false;
    if (p.isUsingItem() && p.getOffHandStack().getItem().equals(Items.SHIELD))
      return false;
    return mc.player.canSee(p);
  }

  private static boolean checkHostile(MobEntity e) {
    /* if (e instanceof EndermanEntity eman && !eman.isAngry())
      return false;
    if (e.isAttacking() && !(e instanceof PhantomEntity))
      return false; */
    if (e instanceof PiglinEntity)
      return false;
    return true;
  }

  private static <T extends Entity> T findNearest(ArrayList<T> list,
                                                  double maxD, Predicate<T> f,
                                                  Predicate<T> s) {
    // if(list.size() == 0) return null;
    var m = maxD * maxD;
    double d;
    T t = null;
    T e = null;
    for (int i = 0; i < list.size(); i++) {
      e = list.get(i);
      if (!f.test(e))
        continue;
      d = mc.player.squaredDistanceTo(e);
      if (d < m && s.test(e)) {
        m = d;
        t = e;
      }
    }
    return t;
  }
}
