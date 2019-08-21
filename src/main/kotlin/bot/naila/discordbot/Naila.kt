package bot.naila.discordbot

import bot.naila.discordbot.utils.EmbedMessage

import bot.naila.discordbot.commands.Command
import com.github.matfax.klassindex.KlassIndex
import com.mewna.catnip.Catnip
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.shard.DiscordEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Color
import java.lang.reflect.Modifier
import kotlin.reflect.full.createInstance
import kotlin.system.exitProcess

const val PREFIX = "!"

val mainLogger: Logger = LogManager.getLogger("Naila [MAIN]")
val parsingLogger: Logger = LogManager.getLogger("Naila [MESSAGE PARSER]")

lateinit var DATABASE_PASSWORD: String
    private set

val commands =
    KlassIndex.getSubclasses(Command::class)
        .withoutModifiers(Modifier.ABSTRACT)
        .map { it.objectInstance ?: it.createInstance() }

lateinit var api: Catnip
    private set

/*
args-
    0- bot token
    1- postgres db password
 */
fun main(args: Array<String>) {
    DATABASE_PASSWORD = args[1]
    val token = args.getOrNull(0) ?: throw IllegalArgumentException("Args[0] must be a token!")
    Catnip.catnipAsync(token).subscribe({
        mainLogger.info("you did it cunt, congratulations")
        api = it
        it.observable(DiscordEvent.MESSAGE_CREATE)
            .subscribe(::parseMessage)
        it.connect()
    }, {
        mainLogger.error("you fucking twat that token is wrong")
        exitProcess(-1)
    })
}

fun parseMessage(message: Message) {
    parsingLogger.debug("MESSAGE RECEIVED(CHANNEL: ${message.channelId()}, AUTHOR: ${message.author().id()}): ${message.content()}")
    val command = commands.firstOrNull { it.keys.any { message.content().startsWith(PREFIX + it, ignoreCase = true) } }
    if(command?.permissionHandler?.invoke(message) == true) return command.execute(message)
    if(message.author().bot()) return
    if(command != null)
        if(command.permissionHandler(message)) return command.execute(message)
        else return
    if(message.content().startsWith(PREFIX))
        EmbedMessage.baseEmbed().updateEmbed {
            color(Color.RED)
            description("Unknown command!")
        }.sendMessage(message.channel())
}

