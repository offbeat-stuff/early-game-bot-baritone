package net.zenxarch.bot.settings

import scala.collection.mutable.StringBuilder
import scala.collection.mutable.ListBuffer

object SettingsParser:
  def parse(input: String): (List[String], String) =
    var res = ListBuffer[String]()

    val inp = split(input, '=')
    var buffer: StringBuilder = new StringBuilder()

    for i <- inp(0)
    do
      if i.isLetter then buffer += i.toLower
      else if i == '.' then
        res += buffer.toString()
        buffer.clear()

    res += buffer.toString
    buffer.clear

    for
      i <- inp(1)
      if i.isLetterOrDigit || "[]{}(),.".contains(i)
    do buffer += i

    return (res.toList, buffer.toString)

  private def split(input: String, delimiter: Char): (String, String) =
    val idx = input.indexOf(delimiter)
    return if idx == -1 then (input, "")
    else (input.substring(0, idx), input.substring(idx + 1))
