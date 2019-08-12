package bot.naila.discordbot.commands

import com.github.matfax.klassindex.IndexSubclasses
import com.mewna.catnip.entity.message.Message

typealias PermissionHandler = (message: Message) -> Boolean

@IndexSubclasses
abstract class Command {
    abstract val key: String
    //overrideable to have custom permission handling
    val permissionHandler: PermissionHandler = { true }

    abstract fun execute(message: Message)
}
