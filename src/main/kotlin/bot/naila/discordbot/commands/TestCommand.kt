package bot.naila.discordbot.commands

import bot.naila.discordbot.utils.EmbedMessage
import com.mewna.catnip.entity.message.Message
import java.time.LocalDateTime

class TestCommand: Command() {
    override val keys = listOf("test")

    override fun execute(message: Message) {
        EmbedMessage.baseEmbedWithFooter(message)
            .updateEmbed {
                translatedDescription("misc.ping.response") { format("\$date", LocalDateTime.now()) }
            }.sendMessage(message.channel())
    }
}
