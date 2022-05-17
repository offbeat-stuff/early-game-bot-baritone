package net.zenxarch.bot.defense

import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture
import scala.collection.mutable.HashMap
import java.util.function.Predicate
import net.minecraft.util.math.MathHelper

object Settings {
  private val settingsMap = new HashMap[String, Setting[?]]()

  private var modules : List[String] = Nil

  def registerModule(module: String) = {
    modules = module :: modules
  }

  def registerSetting(settingName: String, value: Boolean): Unit = {
    val s = parse(settingName)
    if !modules.contains(s(0)) then return
    settingsMap.put(s(0) + "." + s(1), new BoolSetting(value))
  }

  def registerSetting(
      settingName: String,
      value: Double,
      min: Double,
      max: Double,
      suggestions: List[Double]
  ): Unit = {
    val s = parse(settingName)
    if !modules.contains(s(0)) then return
    settingsMap.put(
      s(0) + "." + s(1),
      new DoubleSetting(value, min, max, suggestions.map(_.toString()))
    )
  }

  def getBoolean(identifier: String): Boolean = {
    var s = parse(identifier)
    var n = s(0) + "." + s(1)
    if (settingsMap.contains(n) && settingsMap.get(n).get.`type` == Type.Bool) {
      return (settingsMap.get(n).get.asInstanceOf[BoolSetting]).value
    }
    return false
  }

  def getDouble(identifier: String): Double = {
    val s = parse(identifier)
    val n = s(0) + "." + s(1)
    if (
      settingsMap.contains(n) && settingsMap.get(n).get.`type` == Type.Double
    ) {
      return (settingsMap.get(n).get.asInstanceOf[DoubleSetting]).value
    }
    return 0
  }

  def execute(str: String) = {
    val s = parse(str)
    val n = s(0) + "." + s(1)
    if settingsMap.contains(n) then settingsMap.get(n).get.accept(s(2))
  }

  def exec(str: String): List[String] = {
    var s = parse(str)
    if (s(0).isEmpty()) {
      return (for ((k, v) <- settingsMap) yield k + " = " + v.value).toList
    } else if (s(1).isEmpty()) {
      return (
        for
          (k, v) <- settingsMap
          if k.startsWith(s(0))
        yield k + " = " + v.value
      ).toList
    } else {
      val n = s(0) + "." + s(1)
      if (!s(2).isEmpty()) {
        execute(str)
      }
      return List(n + " = " + settingsMap.get(n).get.value)
    }
  }

  def suggest(builder: SuggestionsBuilder): CompletableFuture[Suggestions] = {
    val strings = parse(builder.getRemaining())
    val mod = strings(0)
    val set = strings(1)
    val value = strings(2)

    var perfectMatch = false

    if (modules.contains(mod)) {
      if (set.isEmpty()) {
        settingsMap.keys
          .foreach(ss => {
            if (ss.startsWith(mod + ".")) {
              builder.suggest(ss)
            }
          })
        perfectMatch = true
      } else if (settingsMap.contains(mod + "." + set)) {
        settingsMap
          .get(mod + "." + set)
          .get
          .suggest(value)
          .foreach(s => {
            builder.suggest(mod + "." + set + "." + s)
          })
        perfectMatch = true
      }
    }

    if (!perfectMatch) {
      if (set.isEmpty()) {
        modules.foreach(m => {
          if (m.startsWith(mod)) {
            builder.suggest(m)
            builder.suggest(m + ".")
          }
        })
      } else {
        settingsMap.keys
          .foreach(s => {
            if (s.startsWith(mod + "." + set)) {
              builder.suggest(s)
              builder.suggest(s + ".")
            }
          })
      }
    }
    return builder.buildFuture()
  }

  private def cleanup(s: String, test: (Char) => Boolean): String = {
    return (for (
      c <- s
      if (test.apply(c))
    ) yield c.toLower)
  }

  private def cleanup(s: String): String = cleanup(s, m => m.isLetter)

  private def parse(s: String): List[String] = {
    var a = split(s, '.')
    var b = split(a(1), '.')
    return List(
      cleanup(a(0)),
      cleanup(b(0)),
      cleanup(b(1), _.isLetterOrDigit)
    )
  }

  private def split(s: String, delimiter: Char): List[String] = {
    val a = s.indexOf(delimiter)
    return if (a == -1) List(s, "")
    else List(s.substring(0, a), s.substring(a + 1))
  }

  abstract class Setting[T](val `type`: Type, var value: T) {
    def accept(s: String): Boolean
    def suggest(input: String): List[String]
  }

  class BoolSetting(value: Boolean)
      extends Setting[Boolean](Type.Bool, value) {
    override def accept(s: String): Boolean = {
      s match {
        case "true" =>
          value = true
        case "false" =>
          value = false
        case "toggle" =>
          value = !value
        case _ =>
          return false
      }
      return true
    }

    override def suggest(input: String): List[String] = {
      return List("true", "false", "toggle").filter(s => s.startsWith(input))
    }
  }

  class DoubleSetting(
      value: Double,
      private val min: Double,
      private val max: Double,
      private val suggestions: List[String]
  ) extends Setting[Double](Type.Double, value) {

    override def accept(s: String): Boolean = {
      try {
        value = MathHelper.clamp(s.toDouble, min, max)
        return true
      } catch
        case e: Exception => {
          return false
        }
    }

    override def suggest(input: String): List[String] = {
      return suggestions.filter(_.startsWith(input)).toList
    }
  }

  enum Type:
    case Bool, Double
}
