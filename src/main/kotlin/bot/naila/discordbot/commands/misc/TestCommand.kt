package bot.naila.discordbot.commands.misc

import bot.naila.discordbot.commands.Command
import bot.naila.discordbot.commands.CommandCategory
import bot.naila.discordbot.utils.EmbedMessage
import com.mewna.catnip.entity.message.Message

class TestCommand: Command() {
    override val keys = listOf("test")
    override val descriptionKey: String = "misc.ping.description"
    override val commandCategory: CommandCategory = CommandCategory.MISC

    override fun execute(message: Message) {
        EmbedMessage.baseEmbedWithFooter(message)
            .translatedDescription("misc.ping.response", message.author().idAsLong(), message.guildIdAsLong())
            .sendMessage(message.channel())
    }
}
