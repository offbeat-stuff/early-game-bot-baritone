package net.zenxarch.bot.defense;

import static net.zenxarch.bot.ZenBot.mc;
import static net.zenxarch.bot.defense.DefenseStateManager.performAction;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.zenxarch.bot.util.ClientPlayerHelper;

public class ShieldBlock extends EntityDefenseModule {
  private static boolean wasBlocking;

  public ShieldBlock() { super("ShieldBlock"); }

  @Override
  public void handleNone() {
    setBlocking(false);
  }

  @Override
  public void handleProjectile(ProjectileEntity pe) {
    if (tryBlock())
      ClientPlayerHelper.lookAt(pe);
  }

  @Override
  public void handleHostile(MobEntity me) {
    tryBlock();
  }

  @Override
  public void handlePlayer(AbstractClientPlayerEntity pe) {
    tryBlock();
  }

  @Override
  public void handlePassive(MobEntity me) {
    setBlocking(false);
  }

  private boolean tryBlock() {
    return setBlocking(shieldCheck() && performAction(() -> true));
  }

  private boolean shieldCheck() {
    if (mc.player.getOffHandStack().getItem() != Items.SHIELD)
      return false;
    if (mc.player.getMainHandStack().isEmpty())
      return true;
    var item = mc.player.getMainHandStack().getItem();
    if (item instanceof BlockItem)
      return mc.crosshairTarget.getType() != HitResult.Type.BLOCK;
    return !(item.isFood());
  }

  private boolean setBlocking(boolean blocking) {
    if (wasBlocking = !blocking) {
      mc.options.useKey.setPressed(blocking);
      wasBlocking = blocking;
    }
    return wasBlocking;
  }
}
