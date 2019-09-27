package bot.naila.discordbot.commands

import bot.naila.discordbot.commands
import bot.naila.discordbot.translator.toTranslatedText
import com.mewna.catnip.entity.message.Message
import java.awt.Color

class HelpCommand: Command() {
    override val keys: List<String> = listOf("help")
    override val descriptionKey: String = "help.description"
    override val commandCategory: CommandCategory = CommandCategory.MISC

    private val keyPattern = "help(\\s(?<key>\\w*))?".toPattern()
    private fun findKey(content: String): String = keyPattern.matcher(content).also { it.find() }.group("key") ?: ""

    override fun execute(message: Message) {
        val key = findKey(message.content())
        val command = commands
            .find { it.keys.contains(key.toLowerCase()) }
        val category = CommandCategory.values().find { it.printFriendly.toLowerCase() == key }
        when {
            key.isEmpty() -> respond(message.channel()) {
                baseEmbedWithFooter(message)
                    .updateEmbed {
                        title("Help Command")
                        translatedDescription("help.defaultDesc", message)
                        addFields(
                            CommandCategory.values().map {
                                it.printFriendly to it.descKey.toTranslatedText(message)
                            }.toMap(), false)
                    }
            }
            command == null && category == null -> respond(message.channel()) {
                baseEmbedWithFooter(message)
                    .translatedDescription("help.unknown") { format("\$key", key) }
                    .updateEmbed { color(Color.RED) }
            }
            category != null -> respond(message.channel()) {
                baseEmbedWithFooter(message)
                    .updateEmbed {
                        val fields = commands
                            .filter { it.commandCategory == category }
                            .map { it.keys[0].capitalize() to it.descriptionKey.toTranslatedText(message) }
                            .toMap()
                        title("Commands in ${category.printFriendly} category!")
                        addFields(fields, false)
                    }
            }
            command != null -> respond(message.channel()) {
                baseEmbedWithFooter(message)
                    .addFields(mapOf(
                        "Description" to command.descriptionKey.toTranslatedText(message),
                        "Aliases" to command.keys.joinToString(", ")
                    ), false)
                    .updateEmbed {
                        title("${command.keys[0].capitalize()} Command")
                    }
            }
        }
    }
}
