package bot.naila.discordbot

import com.mewna.catnip.entity.builder.EmbedBuilder
import com.mewna.catnip.entity.channel.MessageChannel

class EmbedMessage: EmbedBuilder() {
    companion object {
        @JvmField
        val baseEmbed =
            EmbedMessage()
                .updateEmbed {
                    color(11533055)
                }
    }

    fun updateEmbed(updater: EmbedMessage.() -> Unit): EmbedMessage = apply { updater(this) }

    fun <K, V> addFields(map: Map<K, V>, keyDecomposer: (key: K) -> String, valueDecomposer: (value: V) -> String, inline: Boolean) =
        updateEmbed {
            map
                .mapKeys { keyDecomposer(it.key) }
                .mapValues { valueDecomposer(it.value) }
                .forEach { field -> field(field.key, field.value, inline) }
        }

    fun addFields(map: Map<String, String>, inline: Boolean) = addFields(map, {it}, {it}, inline)

    fun sendMessage(channel: MessageChannel) = channel.asTextChannel().sendMessage(this.build())
}
