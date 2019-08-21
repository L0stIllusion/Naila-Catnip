package bot.naila.discordbot.commands.config

import bot.naila.discordbot.commands
import bot.naila.discordbot.commands.Command
import bot.naila.discordbot.translator.Locale
import bot.naila.discordbot.translator.availableLanguages
import bot.naila.discordbot.translator.toTranslatedText
import bot.naila.discordbot.utils.EmbedMessage
import com.mewna.catnip.entity.message.Message
import java.awt.Color
import java.lang.reflect.Method

abstract class ConfigSet<C: ConfigSet<C>>(private val klass: Class<C>): Command() {
    private val instance by lazy { commands.filterIsInstance(klass).first() }

    private val configOptions: Map<ConfigType, Method> =
        klass
            .declaredMethods
            .filter { it.getDeclaredAnnotation(ConfigType::class.java) != null }
            .map { it.getDeclaredAnnotation(ConfigType::class.java) to  it }
            .toMap()

    override fun execute(message: Message) {
        val key= message.content().split(" ").getOrNull(1)

        configOptions
            .filterKeys { it.type == key }
            .values.firstOrNull()
            ?.also {
                it.isAccessible = true
                it.invoke(instance, message)
            } ?: EmbedMessage.baseEmbedWithFooter(message)
                    .updateEmbed {
                        description("Here you can change configurations! Here is a list of configurable configurations!")
                        addFields(configOptions.map { it.key.type.capitalize() to "``!${instance.keys[0]} ${it.key.type}``" }.toMap(), true)
                    }.sendMessage(message.channel())
    }
}

fun getLanguage(message: Message, onNull: (message: Message) -> Unit): Locale?  {
    return Locale.getLocaleByReadableName(message.content().split(" ").getOrElse(2) {""}) ?: onNull(message).let { null }
}

fun handleNullLanguage(message: Message) {
    EmbedMessage.baseEmbed()
        .updateEmbed {
            color(Color.RED)
            val list = "config.set.language.languageList".toTranslatedText(message, formatter = { format("\$languages", "```\n$availableLanguages\n```") })
            description("config.set.language.unknown".toTranslatedText(message))
            field("config.set.language.available".toTranslatedText(message), list, false)
        }.sendMessage(message.channel())
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigType(val type: String)
