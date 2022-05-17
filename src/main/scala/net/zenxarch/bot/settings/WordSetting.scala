package net.zenxarch.bot.settings

class WordSetting(value: String) extends Setting[String](Kind.Word, value) {
  override def accept(input: String): Boolean = {
    value = input.filter(_.isLetter)
    return true
  }

  override def suggest(input: String): List[String] = Nil
}
