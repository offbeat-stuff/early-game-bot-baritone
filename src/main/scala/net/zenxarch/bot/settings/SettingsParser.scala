package net.zenxarch.bot.settings

import scala.collection.mutable.StringBuilder
import scala.collection.mutable.ListBuffer

object SettingsParser:
  def filterArg(input: String): String =
    var buffer = StringBuilder()
    for
      i <- input
      if i.isLetterOrDigit || "[](){},.".contains(i)
    do buffer += i
    return buffer.toString

  def filterIdentifier(input: String): String =
    input.filter(x => x.isLetter || x == '.').map(_.toLower)

  def parse(input: String): (List[String], String) =
    var res = ListBuffer[String]()

    val inp = split(input, '=')
    val identifier = filterIdentifier(inp(0))

    var buffer = StringBuilder()

    for i <- identifier
    do
      if i == '.' then
        res += buffer.toString()
        buffer.clear()
      else buffer += i

    res += buffer.toString

    return (res.toList, filterArg(inp(1)))

  private def split(input: String, delimiter: Char): (String, String) =
    val idx = input.indexOf(delimiter)
    return if idx == -1 then (input, "")
    else (input.substring(0, idx), input.substring(idx + 1))
