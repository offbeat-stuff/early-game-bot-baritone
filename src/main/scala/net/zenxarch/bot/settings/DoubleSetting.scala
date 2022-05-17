package net.zenxarch.bot.settings

class DoubleSetting(value: Double) extends Setting[Double](Kind.Double, value) {
  override def accept(input: String): Boolean = {
    val inp = input.filter(it => it.isDigit || it == '.')
    try {
      value = inp.toDouble
      return true
    } catch {
      case e: Exception => return false
    }
  }

  override def suggest(input: String): List[String] = Nil
}
