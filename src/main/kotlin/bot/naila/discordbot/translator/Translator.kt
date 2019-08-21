
package bot.naila.discordbot.translator

import bot.naila.discordbot.database.NailaDatabase
import bot.naila.discordbot.database.executeAndPositionQuery
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.mewna.catnip.entity.message.Message
import java.net.URL

val availableLanguages = Locale.values().map { it.name.toLowerCase().capitalize() }.joinToString("") { "$it\n" }

open class Translator(locale: Locale) {
    internal open val file: URL = javaClass.getResource("/${locale.localeName}.json")
    internal open val cache: MutableMap<String, String> = mutableMapOf()

    open fun getTranslationByKey(translationKey: String): String? {
        return cache.getOrPut(translationKey, {
            val keys = translationKey.split(".")
            var node = ObjectMapper().readTree<JsonNode>(JsonFactory().createParser(file))
            for((i, key) in keys.withIndex()) {
                node = node.get(key) ?: NullNode.instance
                if(node is NullNode) return null
                if(keys.size == i+1) return node.textValue().ifEmpty { null }
            }
            return null
        })
        }
}

class Formatter(private var message: String) {
    fun <T> format(key: String, value: T) = apply {
        message = message.replace(key, value.toString())
    }

    fun finish(): String = message
}

object SpanishTranslator: Translator(Locale.SPANISH)
object EnglishTranslator: Translator(Locale.ENGLISH)

@Suppress("unused")
enum class Locale(val localeName: String) {
    SPANISH("es_ES"),
    ENGLISH("en_US");

    fun getTranslator(): Translator {
        return when(localeName) {
            "en_US" -> EnglishTranslator
            "es_ES" -> SpanishTranslator
            else -> EnglishTranslator
        }
    }

    companion object {
        fun getLocaleByName(name: String): Locale? =
            values().find {
                it.localeName == name
            }
        fun getLocaleByReadableName(name: String): Locale? =
            values().find {
                it.name.toLowerCase() == name.toLowerCase()
            }
    }
}

inline fun String.toTranslatedText(forUser: Long? = null, forServer: Long? = null, formatter: Formatter.() -> Formatter = { this }, onFail: () -> Unit = {}): String {
    val langPref = NailaDatabase
        .makeRequest(request = {
            prepareStatement("select langpref from users where id = $forUser")
                .executeAndPositionQuery {
                    it.getString(1)
                }
        }) ?: NailaDatabase.makeRequest (request = {
        prepareStatement("select langpref from servers where id = $forServer")
            .executeAndPositionQuery {
                it.getString(1)
            }
    }) ?: "en_US"

    val language = Locale.getLocaleByName(langPref)!!

    return language.getTranslator()
        .getTranslationByKey(this)
        ?.let { formatter(Formatter(it)).finish() }
        ?: "Error translating message! Unknown response key ``$this``".also { onFail() }
}

inline fun String.toTranslatedText(message: Message, formatter: Formatter.() -> Formatter = { this }, onFail: () -> Unit = {}): String =
    toTranslatedText(message.author().idAsLong(), message.guildIdAsLong(), formatter, onFail)
