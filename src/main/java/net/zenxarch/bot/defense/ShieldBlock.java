package net.zenxarch.bot.defense;

import static net.zenxarch.bot.ZenBot.mc;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.zenxarch.bot.util.ClientPlayerHelper;

public class ShieldBlock extends EntityDefenseModule {
  private static boolean wasBlocking;

  @Override
  public void handleNone() {
    unblockShield();
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
    unblockShield();
  }

  private boolean tryBlock() {
    if (!DefenseStateManager.canPerformAction())
      return false;
    if (!mc.player.getOffHandStack().getItem().equals(Items.SHIELD))
      return false;
    mc.options.useKey.setPressed(true);
    wasBlocking = true;
    return true;
  }

  private void unblockShield() {
    if (!wasBlocking)
      return;
    mc.options.useKey.setPressed(false);
    wasBlocking = false;
  }
}
