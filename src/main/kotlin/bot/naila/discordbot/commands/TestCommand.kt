package bot.naila.discordbot.commands

import bot.naila.discordbot.EmbedMessage
import com.mewna.catnip.entity.message.Message

class TestCommand: Command() {
    override val key: String = "test"

    override fun execute(message: Message) {
        EmbedMessage.baseEmbed
            .updateEmbed {
                description("OwO hewwo...")
            }.sendMessage(message.channel())
    }
}
