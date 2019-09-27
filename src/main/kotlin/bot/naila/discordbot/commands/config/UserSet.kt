package bot.naila.discordbot.commands.config

import bot.naila.discordbot.database.NailaDatabase.makeRequest
import bot.naila.discordbot.utils.EmbedMessage
import com.mewna.catnip.entity.message.Message
import java.awt.Color

class UserSet: ConfigSet<UserSet>(UserSet::class.java) {
    override val keys: List<String> = listOf("userset", "uset")
    override val descriptionKey: String = "config.set.user.description"

    @ConfigType("language")
    private fun setLangauge(message: Message) {
        val selectedLanguage = getLanguage(message, ::handleNullLanguage) ?: return
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
