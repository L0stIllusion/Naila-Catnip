package bot.naila.discordbot.commands.config

import bot.naila.discordbot.commands
import bot.naila.discordbot.commands.Command
import bot.naila.discordbot.translator.Locale
import bot.naila.discordbot.utils.EmbedMessage
import com.mewna.catnip.entity.message.Message
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

fun getLanguage(message: Message, onNull: () -> Unit): Locale?  {
    return Locale.getLocaleByReadableName(message.content().split(" ").getOrElse(2) {""}) ?: onNull().let { null }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigType(val type: String)
