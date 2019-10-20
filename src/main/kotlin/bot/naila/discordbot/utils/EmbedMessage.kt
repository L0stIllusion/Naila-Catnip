package bot.naila.discordbot.utils

import bot.naila.discordbot.translator.Formatter
import bot.naila.discordbot.translator.toTranslatedText
import com.mewna.catnip.entity.builder.EmbedBuilder
import com.mewna.catnip.entity.channel.MessageChannel
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.user.User
import java.awt.Color
import java.time.LocalDateTime

@Suppress("unused")
class EmbedMessage: EmbedBuilder(), Cloneable {
    companion object {
        @JvmStatic
        fun baseEmbed() = EmbedMessage().updateEmbed { color(11533055) }

        @JvmStatic
        fun baseEmbedWithFooter(user: User) =
            baseEmbed().updateEmbed {
                footer(user.discordTag(), user.effectiveAvatarUrl())
                timestamp(LocalDateTime.now())
            }
        @JvmStatic
        fun baseEmbedWithFooter(message: Message) =
            baseEmbed().updateEmbed {
                val user = message.author()
                footer(user.discordTag(), user.effectiveAvatarUrl())
                timestamp(message.timestamp())
            }
    }

    fun updateEmbed(updater: EmbedMessage.() -> Unit): EmbedMessage = apply { updater(this) }

    @Suppress("MemberVisibilityCanBePrivate")
    fun <K, V> addFields(map: Map<K, V>, keyDecomposer: (key: K) -> String, valueDecomposer: (value: V) -> String, inline: Boolean) =
        updateEmbed {
            map
                .mapKeys { keyDecomposer(it.key) }
                .mapValues { valueDecomposer(it.value) }
                .forEach { field -> field(field.key, field.value, inline) }
        }

    fun addFields(map: Map<String, String>, inline: Boolean) = addFields(map, {it}, {it}, inline)

    fun translatedDescription(key: String, forUser: Long? = null, forServer: Long? = null, formatter: Formatter.() -> Formatter = { this }): EmbedMessage = apply {
        description(key.toTranslatedText(forUser, forServer, formatter, onFail = { color(Color.RED) }))
    }

    fun translatedDescription(key: String, message: Message, formatter: Formatter.() -> Formatter = { this }): EmbedMessage = translatedDescription(key, message.author().idAsLong(), message.guildIdAsLong(), formatter)

    fun sendMessage(channel: MessageChannel) = channel.sendMessage(this.build())

    override fun clone(): EmbedBuilder {
        return EmbedBuilder(this.build())
    }
}
