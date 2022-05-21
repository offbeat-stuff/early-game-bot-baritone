package net.zenxarch.bot.settings

import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture
import scala.collection.mutable.HashMap
import java.util.function.Predicate
import net.minecraft.util.math.MathHelper

object Settings:
  import SettingsParser._
  private val settingsMap = new HashMap[String, Setting[?]]()

  def registerSetting[T <: Setting[?]](settingName: String, value: T) =
    val s = filterIdentifier(settingName)
    if !settingsMap.contains(s) then settingsMap.put(s, value)

  def registerSetting(settingName: String, value: Boolean): Unit =
    registerSetting(settingName, new BoolSetting(value))

  def registerSetting(
      settingName: String,
      value: Double
  ): Unit =
    registerSetting(
      settingName,
      new DoubleSetting(value)
    )

  def getBoolean(identifier: String): Boolean =
    val n = filterIdentifier(identifier)
    if settingsMap.contains(n) && settingsMap.get(n).get.kind == Kind.Bool
    then return (settingsMap.get(n).get.asInstanceOf[BoolSetting]).value
    return false

  def getDouble(identifier: String): Double =
    val n = filterIdentifier(identifier)
    if settingsMap.contains(n) && settingsMap.get(n).get.kind == Kind.Double
    then return (settingsMap.get(n).get.asInstanceOf[DoubleSetting]).value
    return 0

  def execute(str: String) =
    val s = parse(str)
    val n = s(0).reduce(_ + "." + _)
    if settingsMap.contains(n) then settingsMap.get(n).get.accept(s(1))

  def exec(str: String): List[String] =
    var s = parse(str)

    val n = s(0).reduce(_ + "." + _)

    return if settingsMap.contains(n) then
      val sg = settingsMap.get(n).get
      if s(1) == "" then List(n + " = " + settingsMap.get(n).get.toString)
      else
        sg.accept(s(1))
        List(n + " = " + sg.value)
    else
      (for s <- settingsMap.keys.filter(_.startsWith(n))
      yield s + " = " + settingsMap.get(s).get.value).toList

  def suggest(builder: SuggestionsBuilder): CompletableFuture[Suggestions] =
    val s = parse(builder.getRemaining())
    val n = s(0).reduce(_ + "." + _)

    if settingsMap.contains(n) then
      settingsMap
        .get(n)
        .get
        .suggest(s(1))
        .map(n + " = " + _)
        .foreach(builder.suggest(_))
    else settingsMap.keys.filter(_.startsWith(n)).foreach(builder.suggest(_))

    return builder.buildFuture()
