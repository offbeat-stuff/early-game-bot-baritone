package net.zenxarch.bot.util

import baritone.api.BaritoneAPI
import baritone.api.IBaritone
import net.minecraft.block.Block

object BaritoneUtils:
  private var wasPathing = false

  private def getPrimaryBaritone() =
    BaritoneAPI.getProvider().getPrimaryBaritone()

  private def execute(arg0: String) =
    getPrimaryBaritone().getCommandManager().execute(arg0)

  def pausePathing() =
    if !wasPathing && getPrimaryBaritone().getPathingBehavior().isPathing()
    then
      wasPathing = true
      execute("pause")

  def resumePathing() =
    if wasPathing && !getPrimaryBaritone().getPathingBehavior().isPathing()
    then
      execute("resume")
      wasPathing = false

  def stopPathing() =
    if getPrimaryBaritone().getPathingBehavior().hasPath() then execute("stop")

  def mine(target: Block) =
    stopPathing()
    getPrimaryBaritone().getMineProcess().mine(target)
