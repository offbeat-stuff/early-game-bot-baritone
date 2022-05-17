package net.zenxarch.bot.settings

class BoolSetting(value: Boolean) extends Setting[Boolean](Kind.Bool, value) {
  override def accept(input: String): Boolean = {
    val inp = input.filter(_.isLetter).map(_.toLower)
    inp match {
      case "true" =>
        value = true
      case "false" =>
        value = false
      case "toggle" =>
        value = !value
      case _ => return false
    }
    return true
  }

  override def suggest(input: String): List[String] = {
    val inp = input.filter(_.isLetter).map(_.toLower)
    return List("true", "false", "toggle").filter(_.startsWith(inp))
  }
}
