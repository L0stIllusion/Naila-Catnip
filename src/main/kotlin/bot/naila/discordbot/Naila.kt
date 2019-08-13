package bot.naila.discordbot

import bot.naila.discordbot.EmbedMessage.Companion.baseEmbed
import bot.naila.discordbot.commands.Command
import com.github.matfax.klassindex.KlassIndex
import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.shard.DiscordEvent
import org.apache.logging.log4j.LogManager
import java.awt.Color
import java.lang.reflect.Modifier
import kotlin.reflect.full.createInstance

const val PREFIX = "!"

val mainLogger = LogManager.getLogger("Naila [MAIN]")!!
val parsingLogger = LogManager.getLogger("Naila [MESSAGE PARSER]")!!

val commands: List<Command> = run {
    KlassIndex.getSubclasses(Command::class)
        .withoutModifiers(Modifier.ABSTRACT)
        .map { it.createInstance() }
}

lateinit var api: Catnip
    private set

fun main(args: Array<String>) {
    val token = args.getOrNull(0) ?: throw IllegalArgumentException("Args[0] must be a token!")
    Catnip.catnipAsync(token).subscribe({
        mainLogger.info("you did it cunt, congratulations")
        api = it
        it.observable(DiscordEvent.MESSAGE_CREATE)
            .subscribe(::parseMessage)
        it.connect()
    }, {
        mainLogger.error("you fucking twat that token is wrong")
    })
}

fun parseMessage(message: Message) {
    parsingLogger.debug("MESSAGE RECEIVED(CHANNEL: ${message.channelId()}, AUTHOR: ${message.author().id()}): ${message.content()}")
    val command = commands.firstOrNull { message.content().startsWith(PREFIX + it.key, ignoreCase = true) }
    if(command?.permissionHandler?.invoke(message) == true) return command.execute(message)
    if(message.content().startsWith(PREFIX))
        baseEmbed.updateEmbed {
            color(Color.RED)
            description("Unknown command!")
        }.sendMessage(message.channel())
}

