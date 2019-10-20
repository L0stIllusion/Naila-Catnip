package bot.naila.discordbot.commands

import bot.naila.discordbot.utils.EmbedMessage
import bot.naila.discordbot.utils.PermissionHandler
import com.github.matfax.klassindex.IndexSubclasses
import com.mewna.catnip.entity.channel.MessageChannel
import com.mewna.catnip.entity.message.Message


@IndexSubclasses
abstract class Command {
    abstract val keys: List<String>
    abstract val descriptionKey: String
    abstract val commandCategory: CommandCategory
    //overrideable to have custom permission handling
    open val permissionHandler: PermissionHandler.() -> Boolean = { handle(customCheck({ it.guild() != null })) }

    abstract fun execute(message: Message)

    fun respond(content: String, target: MessageChannel) {
        target.sendMessage(content)
    }
    fun respond(target: MessageChannel, embed: EmbedMessage.Companion.() -> EmbedMessage) {
        embed(EmbedMessage).sendMessage(target)
    }
}

enum class CommandCategory(val printFriendly: String, val descKey: String) {
    MISC("Miscellaneous", "categories.misc.desc"),
    CONFIG("Configuration", "categories.config.desc")
}
