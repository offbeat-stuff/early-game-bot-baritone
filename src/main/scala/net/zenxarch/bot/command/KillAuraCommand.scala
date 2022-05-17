package net.zenxarch.bot.command

import com.mojang.brigadier.arguments.StringArgumentType._
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager._
import net.minecraft.command.CommandSource.suggestMatching

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import java.util.ArrayList
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource
import net.minecraft.text.LiteralText
import net.zenxarch.bot.defense.DefenseStateManager
import net.zenxarch.bot.defense.Settings
import net.zenxarch.bot.util.TargetUtil
import scala.jdk.CollectionConverters._

class KillAuraCommand extends ZenCommand {
  private var active = false
  def register() = literal("zaura")
    .`then`(generatePlayerCommand())
    .`then`(generateSettings())
    .executes(this.toggleDefense)

  def generateSettings() = literal("setting")
    .`then`(
      argument("setting", greedyString())
        .suggests((c, b) => Settings.suggest(b))
        .executes(ctx => {
          Settings
            .exec(getString(ctx, "setting"))
            .foreach(sendMessage(ctx, _))
          0
        })
    )
    .executes(ctx => {
      Settings.exec("").foreach(sendMessage(ctx, _))
      0
    })

  def generatePlayerCommand() = literal("player").`then`(
    argument("PlayerName", string())
      .suggests((c, b) => suggestMatching(getPlayers(c.getSource()), b))
      .executes(ctx => {
        var username = getString(ctx, "PlayerName")
        TargetUtil.handleUsername(username)
        if (TargetUtil.getUsernames().contains(username)) {
          sendMessage(ctx, "Currently targeting " + username + "")
        }
        0
      })
  )

  private def getPlayers(source: FabricClientCommandSource) = (for (
    player <- source.getWorld().getPlayers().asScala
    if !player.equals(source.getPlayer())
  ) yield player.getEntityName()).toArray

  private def toggleDefense(
      ctx: CommandContext[FabricClientCommandSource]
  ): Int = {
    active = !active
    val text = if (active) "Defense Activated" else "Defense Deactived"
    sendMessage(ctx, text)
    DefenseStateManager.setActiveStatus(active)
    return 0
  }

  private def sendMessage(
      ctx: CommandContext[FabricClientCommandSource],
      message: String
  ) = {
    ctx.getSource().sendFeedback(new LiteralText(message))
  }
}
