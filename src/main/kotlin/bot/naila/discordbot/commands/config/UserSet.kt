@file:Suppress("DuplicatedCode")

package bot.naila.discordbot.commands.config

import bot.naila.discordbot.database.NailaDatabase.makeRequest
import bot.naila.discordbot.translator.Locale
import bot.naila.discordbot.translator.toTranslatedText
import bot.naila.discordbot.utils.EmbedMessage
import com.mewna.catnip.entity.message.Message
import java.awt.Color

class UserSet: ConfigSet<UserSet>(UserSet::class.java) {
    override val keys: List<String> = listOf("userset", "uset")

    private val availableLanguages = Locale.values().map { it.name }.map { it.toLowerCase().capitalize() }.joinToString("") { "$it\n" }

    @ConfigType("language")
    private fun setLangauge(message: Message) {
        val selectedLanguage = getLanguage(message) {
            EmbedMessage.baseEmbed()
                .updateEmbed {
                    color(Color.RED)
                    val list = "config.set.language.languageList".toTranslatedText(message, formatter = { format("\$languages", "```\n$availableLanguages\n```") })
                    description("config.set.language.unknown".toTranslatedText(message))
                    field("config.set.language.available".toTranslatedText(message), list, false)
                }.sendMessage(message.channel())
        } ?: return
        val languageName = selectedLanguage.name.toLowerCase().capitalize()
        makeRequest({
            prepareStatement(
                "insert into users(id, langpref) values(${message.author().idAsLong()}, '${selectedLanguage.localeName}') " +
                        "on conflict (id) do update set langpref = '${selectedLanguage.localeName}' where users.id = ${message.author().idAsLong()}")
                .execute()
        }, onSuccess = {
            EmbedMessage.baseEmbedWithFooter(message)
                .translatedDescription("config.set.language.success", message) { format("\$language", languageName).format("\$type", "user") }
                .sendMessage(message.channel())
        }, onError = {
            EmbedMessage.baseEmbed()
                .updateEmbed {
                    color(Color.RED)
                    translatedDescription("config.set.language.fail", message) { format("\$language", languageName).format("\$type", "user") }
                }.sendMessage(message.channel())
        })
    }
}
