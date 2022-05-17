package net.zenxarch.bot.settings

abstract class Setting[T](val kind: Kind, var value: T):
  def accept(input: String): Boolean
  def suggest(input: String): List[String]

enum Kind:
  case Bool, Double, Word
