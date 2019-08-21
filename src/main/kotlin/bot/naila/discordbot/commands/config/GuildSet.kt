@file:Suppress("DuplicatedCode")

package bot.naila.discordbot.commands.config

import bot.naila.discordbot.commands.PermissionHandler
import bot.naila.discordbot.database.NailaDatabase.makeRequest
import bot.naila.discordbot.utils.EmbedMessage
import com.mewna.catnip.entity.message.Message
import com.mewna.catnip.entity.util.Permission
import java.awt.Color

class GuildSet: ConfigSet<GuildSet>(GuildSet::class.java) {
    override val keys: List<String> = listOf("guildset", "gset")

    override val permissionHandler: PermissionHandler = Handler@{
        return@Handler when {
            it.guild() == null -> {
                EmbedMessage.baseEmbed()
                    .updateEmbed {
                        color(Color.RED)
                        description("You must be in a guild to set guild-related configurations!")
                    }.sendMessage(it.channel())
                false
            }
            it.member()!!.hasPermissions(Permission.MANAGE_GUILD) || it.guild()!!.owned() -> true
            else -> {
                EmbedMessage.baseEmbed()
                    .updateEmbed {
                        color(Color.RED)
                        description("You must have MANAGE_GUILD permissions to update guild configurations.")
                    }.sendMessage(it.channel())
                false
            }
        }
    }


    @ConfigType("language")
    private fun setLangauge(message: Message) {
        val selectedLanguage = getLanguage(message, ::handleNullLanguage) ?: return
        val languageName = selectedLanguage.name.toLowerCase().capitalize()
        makeRequest({
            prepareStatement(
                "insert into servers(id, langpref) values(${message.guildIdAsLong()}, '${selectedLanguage.localeName}') " +
                        "on conflict (id) do update set langpref = '${selectedLanguage.localeName}' where servers.id = ${message.guildIdAsLong()}")
                .execute()
        }, onSuccess = {
            EmbedMessage.baseEmbedWithFooter(message)
                .translatedDescription("config.set.language.success", message) { format("\$language", languageName).format("\$type", "guild") }
                .sendMessage(message.channel())
        }, onError = {
            EmbedMessage.baseEmbed()
                .updateEmbed {
                    color(Color.RED)
                    translatedDescription("config.set.language.fail", message) { format("\$language", languageName).format("\$type", "guild") }
                }.sendMessage(message.channel())
        })
    }
}
